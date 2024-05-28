package mx.suma.drivers.mantenimientos.listado

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import mx.suma.drivers.R
import mx.suma.drivers.SumaDriversViewModel
import mx.suma.drivers.databinding.ListadoMantenimientosFragmentBinding
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.network.isOnline
import mx.suma.drivers.repositories.mantenimientos.MantenimientosRepositoryImpl
import mx.suma.drivers.repositories.mantenimientos.remote.MantenimientosRemoteDataSourceImpl
import mx.suma.drivers.utils.EstatusLCE
import mx.suma.drivers.utils.TypeError
import org.koin.android.ext.android.inject

class ListadoMantenimientosFragment : Fragment() {

    private val apiSuma: ApiSuma by inject()

    private lateinit var viewModel: ListadoMantenimientosViewModel
    private val sharedViewModel: SumaDriversViewModel by activityViewModels()

    private lateinit var binding: ListadoMantenimientosFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = ListadoMantenimientosFragmentBinding.inflate(inflater, container, false)

        val remoteDataSource = MantenimientosRemoteDataSourceImpl(apiSuma)
        val repository = MantenimientosRepositoryImpl(remoteDataSource)
        val factory = ListadoMantenimientosViewModelFactory(repository)

        val adapter = ListadoMantenimientosAdapter()

        viewModel = ViewModelProvider(this, factory)
            .get(ListadoMantenimientosViewModel::class.java)


        binding.apply {
            binding.localViewModel = viewModel
            binding.lifecycleOwner = viewLifecycleOwner
            binding.listadoMantenimientos.adapter = adapter
            binding.listadoMantenimientos.layoutManager = LinearLayoutManager(activity)
        }

        setupObservers(adapter)

        return binding.root
    }

    private fun setupObservers(adapter: ListadoMantenimientosAdapter) {
        viewModel.error.observe(viewLifecycleOwner){
            var btFlat = binding.root.findViewById<FloatingActionButton>(R.id.fab_agregar_mantenimiento)
            when(it) {
                TypeError.SERVICE -> {
                    btFlat.isEnabled = false
                    sendMessageError("Ocurrio un error interno, intente más tarde")
                }
                TypeError.NETWORK -> {
                    btFlat.isEnabled = false
                    sendMessageError("Sin conexión a internet")
                }
                TypeError.EMPTY -> {
                    btFlat.isEnabled = true
                }
                TypeError.OTHER -> {
                    btFlat.isEnabled = false
                    sendMessageError("Ocurrio un problema, favor de reportarlo")
                }
                else -> {
                    TODO("No hace ninguna acción")
                }
            }
        }
        sharedViewModel.usuario.observe(viewLifecycleOwner) {
            it?.let {
                try {
                    detectInternet()
                    viewModel.buscarPendientes(it)
                }catch (e: Exception) {
                    viewModel.estatus.value = EstatusLCE.ERROR
                }
            }
        }

        viewModel.mantenimientos.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                adapter.submitList(it)
            }
        })

        viewModel.navigateCapturaMantenimiento.observe(viewLifecycleOwner, {
            if (it) {
                sharedViewModel.usuario.value?.let { usuario ->
                    this.findNavController().navigate(
                        ListadoMantenimientosFragmentDirections
                            .actionListadoMantenimientosFragmentToCapturaPendienteMantenimientoFragment(
                                usuario
                            )
                    )
                }

                viewModel.onNavigationComplete()
            }
        })
    }

    override fun onResume() {
        super.onResume()

        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Listar Mantenimientos")
            param(
                FirebaseAnalytics.Param.SCREEN_CLASS,
                ListadoMantenimientosFragment::class.java.toString()
            )
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
