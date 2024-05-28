package mx.suma.drivers.bitacoras.captura

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mx.suma.drivers.MainActivity
import mx.suma.drivers.R
import mx.suma.drivers.SumaDriversViewModel
import mx.suma.drivers.bluetooth.BluetoothIntentFactory
import mx.suma.drivers.database.SumaDriversDatabase
import mx.suma.drivers.databinding.CapturaBitacoraFragmentBinding
import mx.suma.drivers.models.db.ScannerModel
import mx.suma.drivers.models.utils.EstatusServicio
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.repositories.bitacoras.BitacorasRepositoryImpl
import mx.suma.drivers.repositories.bitacoras.remote.BitacorasRemoteDataSourceImpl
import mx.suma.drivers.repositories.scanner.ScannerRepositoryImpl
import mx.suma.drivers.repositories.scanner.remote.ScannerRemoteDataSourceImpl
import mx.suma.drivers.repositories.unidades.UnidadesRepositoryImpl
import mx.suma.drivers.repositories.unidades.remote.UnidadesRemoteDataSourceImpl
import mx.suma.drivers.session.AppStateImpl
import mx.suma.drivers.utils.ConnectivityObserver
import mx.suma.drivers.utils.NetworkConnectivityObserver
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.lang.NumberFormatException


class CapturaBitacoraFragment : Fragment() {

    private val apiSuma: ApiSuma by inject()
    private val appState: AppStateImpl by inject()
    private val database: SumaDriversDatabase by inject()

    private lateinit var viewModel: CapturaBitacoraViewModel
    private val sharedViewModel: SumaDriversViewModel by activityViewModels()
    private lateinit var connectivityObserver: ConnectivityObserver

    private lateinit var binding: CapturaBitacoraFragmentBinding
    private lateinit var args:  CapturaBitacoraFragmentArgs

    private lateinit var mediaplayer: MediaPlayer

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            iniciarScanner()
        } else {
            Toast.makeText(requireContext(), "Permiso de camara denegada", Toast.LENGTH_SHORT).show()
        }
    }

    private val qrScannerLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            Toast.makeText(context, "Escaneo cancelado", Toast.LENGTH_LONG).show()
        } else {
            try {
                val data_scan = result.contents.split("_")
                var scannerModel = ScannerModel()
                if(data_scan.size == 2) {
                    scannerModel = ScannerModel(bitacoraId = args.bitacoraId, clienteId = data_scan[0].toLong(), pasajeroId = data_scan[1].toLong())
                    viewModel.onMostrarEscanerDone()
                }
                if(viewModel.withInternet.value === ConnectivityObserver.Status.Available) {
                    viewModel.guardarRegistroScanner(scannerModel) { isView, message ->
                        if(isView && !mediaplayer.isPlaying) mediaplayer.start()
                        this.showSnachbackMessage(message, isView)
                    }
                }else {
                    viewModel.guardarRegistroScannerDb(scannerModel) { isView, message ->
                        if(isView && !mediaplayer.isPlaying) mediaplayer.start()
                        this.showSnachbackMessage(message, isView)
                    }
                }
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "Formato del QR no valido.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        connectivityObserver = NetworkConnectivityObserver((activity as MainActivity).applicationContext)
        binding = CapturaBitacoraFragmentBinding.inflate(inflater, container, false)
        mediaplayer = MediaPlayer.create(requireContext(), R.raw.success_bell)

        val remoteDataSource = BitacorasRemoteDataSourceImpl(apiSuma)
        val remoteDataSourceUnit = UnidadesRemoteDataSourceImpl(apiSuma)
        val remoteDataSourceScanner = ScannerRemoteDataSourceImpl(apiSuma)
        val bitacoraDao = database.bitacoraDao
        val scannerDao = database.scannerDao

        val repository = BitacorasRepositoryImpl(remoteDataSource, bitacoraDao)
        val repositoryUnit = UnidadesRepositoryImpl(appState, remoteDataSourceUnit)
        val repositoryScanner = ScannerRepositoryImpl(remoteDataSourceScanner, scannerDao)

        args = CapturaBitacoraFragmentArgs.fromBundle(requireArguments())
        val factory = CapturaBitacoraViewModelFactory(appState, connectivityObserver, repository, repositoryUnit, repositoryScanner ,args.bitacoraId)

        viewModel = ViewModelProvider(this, factory).get(CapturaBitacoraViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupOneShotValues()
        setupListeners()
        setupObservers()

        viewModel.observerNetwork()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Servicios")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, CapturaBitacoraFragment::class.java.toString())
        }
    }

    fun checkPermissionAndShowActivity(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            iniciarScanner()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun setupOneShotValues() {
        binding.etFolioBitacora.setText(viewModel.ultimoFolioBitacora.toString())
    }



    fun setupListeners() {
        binding.etFolioBitacora.addTextChangedListener {
            it?.let {
                try {
                    if (it.isNotEmpty()) {
                        if(it.toString().toLong() >= viewModel.ultimoFolioBitacora){
                            binding.tilFolioBitacora.error = null
                            viewModel.setFolioBitacora(it.toString().toLong())
                        }else{
                            binding.tilFolioBitacora.error = getString(R.string.msj_error_folio_min)
                        }
                    } else {
                        binding.tilFolioBitacora.error = getString(R.string.msj_campo_vacio)
                        viewModel.setFolioBitacora(0L)
                    }
                }catch (e:Exception){
                    Timber.e(e)
                }
            }
        }

        binding.etKilometrajeInicial.addTextChangedListener {
            it?.let {
                try {
                    if (it.isNotEmpty()) {
                        var ultimoKilometraje = viewModel.ultimoKilometraje.value
                        if(ultimoKilometraje == null) {
                            ultimoKilometraje = 0L
                        }
                        if(it.toString().toLong() >= ultimoKilometraje) {
                            binding.tilKilometrajeInicial.error = null
                            viewModel.setKilometrajeInicial(it.toString().toLong())
                        }else {
                            binding.tilKilometrajeInicial.error = getString(R.string.msj_error_kilometraje_Min)
                        }
                    } else{
                        binding.tilKilometrajeInicial.error = getString(R.string.msj_campo_vacio)
                        viewModel.setKilometrajeInicial(-1L)
                    }
                }catch (e:Exception) {
                    Timber.e(e)
                }
            }
        }

        binding.etNumeroPersonas.addTextChangedListener {
            it?.let {
                if (it.isNotEmpty()) {
                    binding.tilNumeroPersonas.error = null
                    viewModel.setNumeroPersonas(it.toString().toInt())
                } else {
                    binding.tilNumeroPersonas.error = getString(R.string.msj_campo_vacio)
                    viewModel.setNumeroPersonas(0)
                }
            }
        }

        binding.etKilometrajeFinal.addTextChangedListener {
            it?.let {
                if (it.isNotEmpty()) {
                    var kilInicialTotal = viewModel.data.value?.kilometrajeInicial
                    if(kilInicialTotal == null) {
                       kilInicialTotal = 0L
                    }
                    kilInicialTotal += 10
                    if(it.toString().toLong() > kilInicialTotal) {
                        binding.tilKilometrajeFinal.error = null
                        viewModel.setKilometrajeFinal(it.toString().toLong())
                    }else {
                        binding.tilKilometrajeFinal.error = getString(R.string.msj_error_kilometraje_Min)
                    }
                } else {
                    binding.tilKilometrajeFinal.error = getString(R.string.msj_campo_vacio)
                    viewModel.setKilometrajeFinal(0)
                }
            }
        }

        binding.btnCerrarBitacora.setOnClickListener {
            MaterialAlertDialogBuilder(it.context)
                .setTitle(resources.getString(R.string.title_km_sure))
                .setMessage(resources.getString(R.string.msg_km_sure))
                .setNegativeButton("No es correcto") { dialog, which ->
                    binding.etKilometrajeFinal.requestFocus()
                    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(binding.etKilometrajeFinal, 0)
                }
                .setPositiveButton("Si, correcto") { _,_ ->
                    viewModel.enviarCambios()
                }
                .show()
        }
    }

    private fun setupObservers() {
        viewModel.withInternet.observe(viewLifecycleOwner) {
            when(it) {
                ConnectivityObserver.Status.Available -> {
                    Toast.makeText(requireActivity().applicationContext, "Con conexión a internet", Toast.LENGTH_SHORT).show()
                    lifecycleScope.launch(Dispatchers.Main) {
                        delay(1000)
                        viewModel.buscarBitacora(false)
                        viewModel.fechaActual.value = viewModel.obtenerFechaActual("yyyy-MM-d")
                        viewModel.obtenerScannerDbEnvio(){
                            if(!mediaplayer.isPlaying) mediaplayer.start()
                            showSnachbackMessage("Se enviaron correctamente la asistencias almacenadas", true)
                        }
                    }
                }
                ConnectivityObserver.Status.Lost -> {
                    Toast.makeText(requireActivity().applicationContext, "Se ha perdido la conexión de internet", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    viewModel.buscarBitacora(true)
                }
            }
        }


        viewModel.isLoading.observe(viewLifecycleOwner) {
            val isEnabled:Boolean = !it
            when(viewModel.estatus.value) {
                EstatusServicio.Estatus.ABRIR_BITACORA -> {
                    val btn:MaterialButton = binding.root.findViewById<MaterialButton>(R.id.btn_abrir_bitacora)
                    btn.isEnabled = isEnabled
                }
                EstatusServicio.Estatus.INICIAR_RUTA -> {
                    val btn:MaterialButton = binding.root.findViewById<MaterialButton>(R.id.btn_iniciar_ruta)
                    btn.isEnabled = isEnabled
                }
                EstatusServicio.Estatus.CONFIRMAR_SERVICIO -> {
                    val btn:MaterialButton = binding.root.findViewById<MaterialButton>(R.id.btn_confirmar_servicio)
                    btn.isEnabled = isEnabled
                }
                EstatusServicio.Estatus.TERMINAR_RUTA -> {
                    val btn:MaterialButton = binding.root.findViewById<MaterialButton>(R.id.btn_terminar_ruta)
                    btn.isEnabled = isEnabled
                }
                EstatusServicio.Estatus.CERRAR_BITACORA -> {
                    val btn:MaterialButton = binding.root.findViewById<MaterialButton>(R.id.btn_cerrar_bitacora)
                    btn.isEnabled = isEnabled
                }
                else -> {
                    // TODO no se hace nada
                }
            }
        }
        viewModel.ultimoKilometraje.observe(viewLifecycleOwner) {
            binding.etKilometrajeInicial.setText(it.toString())
        }
        viewModel.mostrarConfirmacion.observe(viewLifecycleOwner) {
            if(it) {
                if(viewModel.mostrarTesting() && (viewModel.withInternet.value == ConnectivityObserver.Status.Available)) {
                    this.findNavController().navigate(CapturaBitacoraFragmentDirections
                        .actionCapturaBitacoraFragmentToTestingFragment(args.bitacoraId, viewModel.data.value!!.idUnidad.toInt()))
                }
            }
        }
        sharedViewModel.usuario.observe(viewLifecycleOwner) {
            it?.let {
                viewModel.usuario.value = it
            }
        }

        viewModel._navigateToMapa.observe(viewLifecycleOwner) {
            try {
                if (it) {
                    viewModel.data.value?.let { bitacora ->
                        this.findNavController().navigate(
                            CapturaBitacoraFragmentDirections
                                .actionCapturaBitacoraFragmentToMapsActivity(bitacora.idMapa, bitacora.idRuta, bitacora.id)
                        )
                    }

                    viewModel.onNavigationComplete()
                }
            }catch (e:Exception) {
                Timber.e("ocurrio un error: ${e.message}")
            }
        }

        viewModel.iniciarCapturaAforo.observe(viewLifecycleOwner) {
            if(it) {
                viewModel.data.value?.let { bitacora ->
                    val readerName = "suma%02d".format(bitacora.idUnidad)

                    Timber.d("Reader name: $readerName")

                    if (bitacora.capturarAforo && bitacora.horaCierreRuta == null && bitacora.horaBanderazo != null) {
                        Timber.d("Start captura aforo")

                        val intent = BluetoothIntentFactory
                            .iniciarCapturaAforo(
                                requireContext().applicationContext as Context,
                                readerName,
                                bitacora
                            )

                        ContextCompat.startForegroundService(
                            requireActivity().applicationContext, intent
                        )
                    }
                }

                viewModel.onActivarLectorAforoDone()
            }
        }

        viewModel.mostrarLetrero.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.data.value?.let { bitacora ->
                    if (bitacora.horaConfirmacion != null && bitacora.horaCierreRuta == null) {
                        val letreroName = "suma%02d_leds".format(bitacora.idUnidad);

                        Timber.d("Activar letrero electrónico ($letreroName)")

                        val intentLetrero = BluetoothIntentFactory
                            .activarLetrero(
                                requireContext().applicationContext,
                                letreroName,
                                bitacora
                            )

                        ContextCompat.startForegroundService(
                            requireActivity().applicationContext as Context, intentLetrero
                        )
                    }else {
                        Toast.makeText(binding.root.context, "No es posible activar letrero en esta bitacora", Toast.LENGTH_SHORT).show()
                    }
                }

                viewModel.onActivarLetreroDone()
            }
        }

        viewModel.mostrarEscaner.observe(viewLifecycleOwner) {
            if(it) {
                Timber.d("Mostrar scaner")
                checkPermissionAndShowActivity(requireContext().applicationContext)
            }
        }
    }

    fun iniciarScanner() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("SumaDriver")
        options.setCameraId(0) // Use a specific camera of the device
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(true)
        options.setOrientationLocked(false)
        qrScannerLauncher.launch(options)
    }

    private fun vibrateDevice(duration: Long) {
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (vibrator.hasVibrator() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val amplitude = VibrationEffect.DEFAULT_AMPLITUDE
            val vibrationEffect = VibrationEffect.createOneShot(duration, amplitude)
            vibrator.vibrate(vibrationEffect)
        } else if (vibrator.hasVibrator()) {
            vibrator.vibrate(duration)
        }
    }
    fun showSnachbackMessage(message: String, status: Boolean) {
        val duration = if(status) Snackbar.LENGTH_LONG else Snackbar.LENGTH_INDEFINITE
        val viewContent = view?.findViewById<View>(R.id.cl_content_bitacora)
        if(viewContent != null) {
            val snackbar = Snackbar.make(viewContent, message, duration)
                .setAction("Aceptar") {

                }
                .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.grey50))
            if(status) {
                snackbar
                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.green))
                    .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.grey50))
            }else {
                snackbar
                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.md_red))
                vibrateDevice(1000)
            }
            snackbar.show()
        }
    }


}
