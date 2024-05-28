package mx.suma.drivers.mantenimientos.captura

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import mx.suma.drivers.R
import mx.suma.drivers.databinding.CapturaPendienteMantenimientoFragmentBinding
import mx.suma.drivers.models.db.UsuarioModel
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.repositories.mantenimientos.MantenimientosRepository
import mx.suma.drivers.repositories.mantenimientos.MantenimientosRepositoryImpl
import mx.suma.drivers.repositories.mantenimientos.remote.MantenimientosRemoteDataSourceImpl
import org.koin.android.ext.android.inject

class CapturaMantenimientoFragment : Fragment() {

    private val apiSuma: ApiSuma by inject()

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var viewModel: CapturaMantenimientoViewModel
    private lateinit var binding: CapturaPendienteMantenimientoFragmentBinding
    private lateinit var usuario: UsuarioModel
    private lateinit var repository: MantenimientosRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.captura_pendiente_mantenimiento_fragment, container, false)
        usuario = CapturaMantenimientoFragmentArgs.fromBundle(requireArguments()).usuario

        firebaseAnalytics = Firebase.analytics

        val dataSource = MantenimientosRemoteDataSourceImpl(apiSuma)
        repository = MantenimientosRepositoryImpl(dataSource)

        val factory = CapturaMantenimientoViewModelFactory(usuario, repository)

        viewModel =
            ViewModelProvider(this, factory).get(CapturaMantenimientoViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setupObservers()
        setupListeners()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Registrar Mantenimiento")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, CapturaMantenimientoFragment::class.java.toString())
        }
    }

    private fun setupObservers() {
        viewModel.mantenimientoGenerado.observe(viewLifecycleOwner, Observer {
            if(it != -1L) {
                this.findNavController().navigate(
                    CapturaMantenimientoFragmentDirections
                        .actionCapturaPendienteMantenimientoFragmentToListadoMantenimientosFragment(usuario)
                )

                viewModel.onNavigationComplete()
            }
        })

        viewModel.failedRequest.observe(viewLifecycleOwner, Observer {
            if(it) {
                binding.btnEnviarMantenimiento.text = getString(R.string.reintentar)
            }
        })
    }

    private fun setupListeners() {
        binding.etMantenimientoTitulo.addTextChangedListener {
            it?.let {
                if (it.isNotEmpty()) {
                    viewModel.setTitulo(it.toString())
                    binding.tilMantenimientoTitulo.error = null
                } else {
                    viewModel.setTitulo("")
                    binding.tilMantenimientoTitulo.error = getString(R.string.msj_campo_vacio)
                }
            }
        }

        binding.etMantenimientoNotas.addTextChangedListener {
            it?.let {
                if (it.isNotEmpty()) {
                    viewModel.setNotas(it.toString())
                    binding.etMantenimientoNotas.error = null
                } else {
                    viewModel.setNotas("")
                    binding.etMantenimientoNotas.error = getString(R.string.msj_campo_vacio)
                }
            }
        }
    }
}
