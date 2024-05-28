package mx.suma.drivers.miUnidad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import mx.suma.drivers.R
import mx.suma.drivers.SumaDriversViewModel
import mx.suma.drivers.database.SumaDriversDatabase
import mx.suma.drivers.databinding.MiUnidadFragmentBinding
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.network.isOnline
import mx.suma.drivers.repositories.unidades.UnidadesRepositoryImpl
import mx.suma.drivers.repositories.unidades.remote.UnidadesRemoteDataSourceImpl
import mx.suma.drivers.session.AppStateImpl
import mx.suma.drivers.utils.EstatusLCE
import mx.suma.drivers.utils.TypeError
import org.koin.android.ext.android.inject
import timber.log.Timber

class MiUnidadFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private val apiSuma: ApiSuma by inject()
    private val database: SumaDriversDatabase by inject()
    private val appState: AppStateImpl by inject()

    private lateinit var viewModel: MiUnidadViewModel
    private val sharedViewModel: SumaDriversViewModel by activityViewModels()

    private lateinit var binding: MiUnidadFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = MiUnidadFragmentBinding.inflate(inflater, container, false)

        val usuarioDao = database.usuarioDao

        val unidadesRemoteDataSource = UnidadesRemoteDataSourceImpl(apiSuma)
        val unidadesRepository =
            UnidadesRepositoryImpl(appState, unidadesRemoteDataSource)
        val factory = MiUnidadViewModelFactory(unidadesRepository, usuarioDao, appState)

        viewModel = ViewModelProvider(this, factory).get(MiUnidadViewModel::class.java)

        binding.apply {
            localViewModel = viewModel
            lifecycleOwner = viewLifecycleOwner
            spMiunidadUnidades.onItemSelectedListener = this@MiUnidadFragment
        }

        setupObservers()

        return binding.root
    }

    private fun setupObservers() {
        viewModel.error.observe(viewLifecycleOwner) {
            var btTomarUnidad:Button = binding.root.findViewById<Button>(R.id.bt_tomar_unidad)

            when(it) {
                TypeError.SERVICE -> {
                    sendMessageError("Ocurrio un error interno, intente más tarde")
                    btTomarUnidad.isEnabled = false
                }
                TypeError.NETWORK -> {
                    sendMessageError("Sin conexión a internet")
                    btTomarUnidad.isEnabled = false
                }
                TypeError.EMPTY -> {
                    sendMessageError("")
                    btTomarUnidad.isEnabled = true
                }
                else -> {}
            }
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                try {
                    if (viewModel.unidadNoValida.value as Boolean) {
                        Timber.i("Backpressed")
                        Toast.makeText(
                            requireContext(),
                            "Debes seleccionar la unidad para continuar",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }catch (e:Exception) {
                    Timber.d(e)
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        viewModel.unidadValida.observe(viewLifecycleOwner) {
            try {
                if (it) {
                    callback.isEnabled = false
                    callback.remove()
                }
            } catch (e:Exception) {
                Timber.d(e)
            }
        }

        viewModel.unidades.observe(viewLifecycleOwner) {
            try {
                if (it.isNotEmpty()) {
                    val adapter = ArrayAdapter(
                        requireContext(), android.R.layout.simple_spinner_item, it
                    )

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                    binding.spMiunidadUnidades.adapter = adapter
                }
            }catch (e:Exception) {
                Timber.d(e)
            }
        }

        sharedViewModel.usuario.observe(viewLifecycleOwner) {
                try {
                    detectInternet()
                    if (it != null) {
                        Timber.d("Got the user!")
                        viewModel.usuario.value = it
                        viewModel.unidadActual.value = it.idUnidad
                        viewModel.bajarDatosUnidad(false)
                    }
                } catch (e: Exception) {
                    viewModel.estatus.value = EstatusLCE.ERROR;
                }
        }
    }

    override fun onResume() {
        super.onResume()

        try {
            Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, "Mi Unidad")
                param(FirebaseAnalytics.Param.SCREEN_CLASS, MiUnidadFragment::class.java.toString())
            }
        }catch (e:Exception) {
            Timber.d(e)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        when (parent.id) {
            R.id.sp_miunidad_unidades -> {
                val item = parent.getItemAtPosition(position).toString()
                viewModel.setUnidadSeleccionada(item)

                Timber.d("Unidad seleccionada: $item")
            }
        }
    }

    private fun detectInternet() {
        if(this.context?.let { isOnline(it) } == false){
            viewModel.error.value = TypeError.NETWORK
            throw Exception("Sin conexión a internet")
        }
    }

    private fun sendMessageError(message:String) {
        val clMensaje: View = binding.root.findViewById(R.id.cl_mensaje_error)
        var tvMensaje: TextView = clMensaje.findViewById(R.id.tv_mensaje);
        tvMensaje.text = message
    }
}
