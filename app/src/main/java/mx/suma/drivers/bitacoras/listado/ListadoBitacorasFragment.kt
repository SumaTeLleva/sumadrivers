package mx.suma.drivers.bitacoras.listado

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import mx.suma.drivers.R
import mx.suma.drivers.SumaDriversViewModel
import mx.suma.drivers.database.SumaDriversDatabase
import mx.suma.drivers.databinding.ListadoBitacorasFragmentBinding
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.network.isOnline
import mx.suma.drivers.repositories.bitacoras.BitacorasRepositoryImpl
import mx.suma.drivers.repositories.bitacoras.remote.BitacorasRemoteDataSourceImpl
import mx.suma.drivers.repositories.operadores.OperadoresRepositoryImpl
import mx.suma.drivers.repositories.operadores.remote.OperadoresRemoteDataSourceImpl
import mx.suma.drivers.utils.EstatusLCE
import mx.suma.drivers.utils.TypeError
import mx.suma.drivers.work.EnviarDatosAforosWorker
import org.koin.android.ext.android.inject
import timber.log.Timber

class ListadoBitacorasFragment : Fragment() {

    private lateinit var viewModel: ListadoBitacorasViewModel
    private val sharedViewModel: SumaDriversViewModel by activityViewModels()
    private val apiSuma: ApiSuma by inject()
    private val database: SumaDriversDatabase by inject()
    lateinit var binding: ListadoBitacorasFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.listado_bitacoras_fragment, container, false
        )
        val bitacoraDao = database.bitacoraDao
        val dataSource = BitacorasRemoteDataSourceImpl(apiSuma)
        val operadoresDataSource = OperadoresRemoteDataSourceImpl(apiSuma)

        val bitacorasRepositoryImpl = BitacorasRepositoryImpl(dataSource, bitacoraDao)
        val operadoresRepository = OperadoresRepositoryImpl(operadoresDataSource)

        val factory =
            ListadoBitacorasViewModelFactory(bitacorasRepositoryImpl, operadoresRepository)

        viewModel = ViewModelProvider(this, factory)
            .get(ListadoBitacorasViewModel::class.java)

        val adapter = ListadoBitacorasAdapter(BitacorasClickListener { bitacoraId ->
            viewModel.onBitacoraClicked(bitacoraId)
        })
        binding.apply {
            localViewModel = viewModel
            lifecycleOwner = viewLifecycleOwner
            bitacorasList.adapter = adapter
            bitacorasList.layoutManager = LinearLayoutManager(activity)
        }
        setupObservers(adapter)
        setHasOptionsMenu(true)
        return binding.root
    }

    private fun setupObservers(adapter: ListadoBitacorasAdapter) {
        viewModel.error.observe(viewLifecycleOwner) {
            val btnBack : Button = binding.root.findViewById(R.id.bt_back_b)
            val btnNow : Button = binding.root.findViewById(R.id.bt_now_b)
            val btnNext : Button = binding.root.findViewById(R.id.bt_next_b)
            when(it) {
                TypeError.SERVICE -> {
                    sendMessageError("Ocurrio un error interno, intente más tarde")
                    btnBack.isEnabled = false
                    btnNext.isEnabled = false
                    btnNow.isEnabled = false
                    setHasOptionsMenu(false)
                }
                TypeError.NETWORK -> {
                    sendMessageError("Sin conexión a internet")
                    btnBack.isEnabled = false
                    btnNext.isEnabled = false
                    btnNow.isEnabled = false
                    setHasOptionsMenu(false)
                }
                TypeError.EMPTY -> {
                    sendMessageError("")
                    btnBack.isEnabled = true
                    btnNext.isEnabled = true
                    btnNow.isEnabled = true
                    setHasOptionsMenu(true)
                }
                else -> {}
            }
        }

        sharedViewModel.usuario.observe(viewLifecycleOwner) {
            it?.let {
                try {
                    viewModel.usuario.value = it
                    if(detectInternet()){
                        viewModel.getBitacoraDb()
                    }else{
                        viewModel.getBitacoras()
                    }
                }catch (e: Exception) {
                    viewModel.estatus.value = EstatusLCE.ERROR
                }
            }
        }

        viewModel.errorVisit.observe(viewLifecycleOwner) {
            it?.let {
                if(it.isNotEmpty()) {
                    val builder = AlertDialog.Builder(requireContext())

                    builder.setMessage(it)
                        .setTitle("Ocurrio un problema")
                        .setPositiveButton("OK") {
                                _, _ ->
                            Timber.d("Ocurrio un problema en el servicio")
                            viewModel.errorVisit.value = ""
                        }
                    builder.create().show()
                }
            }
        }

        viewModel.bitacoras.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.onNavigateToCapturaBitacora.observe(viewLifecycleOwner) { bitacoraId ->
            if (bitacoraId != -1L) {
                try {
                    this.findNavController().navigate(
                        ListadoBitacorasFragmentDirections
                            .actionListadoBitacorasFragmentToCapturaBitacoraFragment(bitacoraId)
                    )

                    viewModel.onNavigationComplete()
                }catch (e:Exception) {
                    Timber.e("ocurrio un error: ${e.message}")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Servicios")
            param(
                FirebaseAnalytics.Param.SCREEN_CLASS,
                ListadoBitacorasFragment::class.java.toString()
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_servicios, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
//            R.id.accion_traslado_de_operacion -> {
//                val builder = AlertDialog.Builder(requireContext())
//                builder.setMessage("Se generará un registro de Visita a oficina, ¿Continuar?")
//                    .setPositiveButton("Sí") { _, _ ->
//                        Timber.d("Generar visita a oficina")
//                        viewModel.onGenerarVisitaOficina()
//                    }.setNegativeButton("No") { _, _ ->
//                        Timber.d("Se cancela")
//                    }
//
//                builder.create().show()
//            }
//            R.id.accion_traslado_taller -> {
//                val builder = AlertDialog.Builder(requireContext())
//                builder.setMessage("Se generará un registro de Visita al taller, ¿Continuar?")
//                    .setPositiveButton("Sí") { _, _ ->
//                        Timber.d("Generar visita a taller")
//                        viewModel.onGenerarVisitaTaller()
//                    }.setNegativeButton("No") { _, _ ->
//                        Timber.d("Se cancela")
//                    }
//                builder.create().show()
//            }
            R.id.accion_enviar_datos_aforos -> {
                val theWorker = OneTimeWorkRequestBuilder<EnviarDatosAforosWorker>().build()
                WorkManager.getInstance(requireContext()).enqueue(theWorker)
            }
        }

        return true
    }

    private fun detectInternet():Boolean {
        return this.context?.let { isOnline(it) } == false
    }

    private fun sendMessageError(message:String) {
        val clMensaje: View = binding.root.findViewById(R.id.cl_mensaje_error)
        val tvMensaje:TextView = clMensaje.findViewById(R.id.tv_mensaje)
        tvMensaje.text = message
    }
}
