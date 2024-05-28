package mx.suma.drivers.sanitizaciones.listado

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
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import mx.suma.drivers.R
import mx.suma.drivers.SumaDriversViewModel
import mx.suma.drivers.databinding.ListadoSanitizacionesFragmentBinding
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.network.isOnline
import mx.suma.drivers.repositories.sanitizaciones.SanitizacionesRepositoryImpl
import mx.suma.drivers.repositories.sanitizaciones.remote.SanitizacionesRemoteDataSourceImpl
import mx.suma.drivers.utils.EstatusLCE
import mx.suma.drivers.utils.TypeError
import mx.suma.drivers.work.EnviarEvidenciaSanitizacionWorker
import org.koin.android.ext.android.inject

class ListadoSanitizacionesFragment : Fragment() {

    private val apiSuma: ApiSuma by inject()

    private lateinit var viewModel: ListadoSanitizacionesViewModel
    private val sharedViewModel: SumaDriversViewModel by activityViewModels()

    private lateinit var binding: ListadoSanitizacionesFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = ListadoSanitizacionesFragmentBinding.inflate(inflater, container, false)

        val remoteDataSource = SanitizacionesRemoteDataSourceImpl(apiSuma)
        val repository = SanitizacionesRepositoryImpl(remoteDataSource)

        val factory = ListadoSanitizacionesViewModelFactory(repository)
        val adapter = ListadoSanitizacionesAdapter()

        viewModel = ViewModelProvider(this, factory)
            .get(ListadoSanitizacionesViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.sanitizacionesList.adapter = adapter
        binding.sanitizacionesList.layoutManager = LinearLayoutManager(activity)

        requireActivity().title = "Sanitizaciones"

        setupObservers(adapter)

        val workerConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .build()

        val theWorker = OneTimeWorkRequestBuilder<EnviarEvidenciaSanitizacionWorker>()
            .setConstraints(workerConstraints)
            .build()

        WorkManager.getInstance(requireContext()).enqueue(theWorker)

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Listar Sanitizaciones")
            param(
                FirebaseAnalytics.Param.SCREEN_CLASS,
                ListadoSanitizacionesFragment::class.java.toString()
            )
        }
    }

    private fun setupObservers(adapter: ListadoSanitizacionesAdapter) {
        viewModel.error.observe(viewLifecycleOwner) {
            var btnFab : FloatingActionButton = binding.root.findViewById<FloatingActionButton>(R.id.fab)
            when(it) {
                TypeError.NETWORK -> {
                    sendMessageError("Sin conexión a internet")
                    btnFab.isEnabled = false
                }
                TypeError.SERVICE -> {
                    sendMessageError("Ocurrio un error interno, intente más tarde")
                    btnFab.isEnabled = false
                }
                TypeError.EMPTY -> {
                    btnFab.isEnabled = true
                }
                TypeError.OTHER -> {
                    btnFab.isEnabled = false
                    sendMessageError("Ocurrio un problema, favor de reportarlo")
                }
                else -> {
                    TODO("No se realiza ninguna acción")
                }
            }
        }
        sharedViewModel.usuario.observe(viewLifecycleOwner) {
            it?.let { usuario ->
                try {
                    viewModel.usuario.value = usuario
                    detectInternet()
                    viewModel.buscarSanitizaciones()
                } catch (e: Exception) {
                    viewModel.estatus.value = EstatusLCE.ERROR
                }
            }
        }

        viewModel.sanitizaciones.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.navigateToCapturaSanitizacion.observe(viewLifecycleOwner, {
            if (it) {
                sharedViewModel.usuario.value?.let {
                    this.findNavController().navigate(
                        R.id.action_listadoSanitizaciones_to_capturaSanitizacion
                    )
                }

                viewModel.onNavigationComplete()
            }
        })
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