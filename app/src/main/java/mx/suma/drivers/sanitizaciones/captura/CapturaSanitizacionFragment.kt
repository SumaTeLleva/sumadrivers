package mx.suma.drivers.sanitizaciones.captura

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import mx.suma.drivers.R
import mx.suma.drivers.SumaDriversViewModel
import mx.suma.drivers.database.SumaDriversDatabase
import mx.suma.drivers.databinding.CapturaSanitizacionFragmentBinding
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.repositories.bitacoras.BitacorasRepositoryImpl
import mx.suma.drivers.repositories.bitacoras.remote.BitacorasRemoteDataSourceImpl
import mx.suma.drivers.repositories.clientes.ClientesRepositoryImpl
import mx.suma.drivers.repositories.clientes.remote.ClientesRemoteDataSourceImpl
import mx.suma.drivers.repositories.sanitizaciones.SanitizacionesRepositoryImpl
import mx.suma.drivers.repositories.sanitizaciones.remote.SanitizacionesRemoteDataSourceImpl
import org.koin.android.ext.android.inject
import timber.log.Timber

class CapturaSanitizacionFragment : Fragment() {

    private val apiSuma: ApiSuma by inject()
    private val database: SumaDriversDatabase by inject()

    private lateinit var viewModel: CapturaSanitizacionViewModel
    private val shareViewModel: SumaDriversViewModel by activityViewModels()

    private lateinit var binding: CapturaSanitizacionFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = CapturaSanitizacionFragmentBinding.inflate(inflater, container, false)

        val remoteDataSource = BitacorasRemoteDataSourceImpl(apiSuma)
        val bitacoraDao = database.bitacoraDao
        val repository = BitacorasRepositoryImpl(remoteDataSource, bitacoraDao)

        val clientesRemoteDataSource = ClientesRemoteDataSourceImpl(apiSuma)
        val clientesRepository = ClientesRepositoryImpl(clientesRemoteDataSource)

        val sanitizacionesRemoteDataSource = SanitizacionesRemoteDataSourceImpl(apiSuma)
        val sanitizacionesRepository = SanitizacionesRepositoryImpl(sanitizacionesRemoteDataSource)

        val factory = CapturaSanitizacionViewModelFactory(
            repository,
            clientesRepository,
            sanitizacionesRepository
        )

        viewModel = ViewModelProvider(this, factory)
            .get(CapturaSanitizacionViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setupObservers()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Registrar Senitización")
            param(
                FirebaseAnalytics.Param.SCREEN_CLASS,
                CapturaSanitizacionFragment::class.java.toString()
            )
        }
    }

    private fun setupObservers() {
        shareViewModel.usuario.observe(viewLifecycleOwner, {
            it?.let {
                viewModel.usuario.value = it
                viewModel.bajarDatosUltimoServicio()
            }
        })

        viewModel.showConfirmDialogRecordVideo.observe(viewLifecycleOwner, {
            if (it) {
                val builder = AlertDialog.Builder(requireContext())

                builder.setMessage("¿Confirmar datos y grabar evidencia con video?")
                    .setPositiveButton(
                        "Continuar"
                    ) { _, _ ->
                        viewModel.guardarRegistroSanitizacion()
                    }
                    .setNegativeButton(
                        "Cancelar"
                    ) { _, _ ->
                        // User cancelled the dialog
                        Timber.d("Cancelado")
                        Toast.makeText(
                            requireContext(), "Se cancela registro", Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNeutralButton(
                        "Audio"
                    ) { _, _ ->
                        viewModel.guardarRegistroSanitizacion(false)
                    }

                builder.create().show()

                viewModel.onShowConfirmDialogComplete()
            }
        })

        viewModel.showConfirmDialogRecordAudio.observe(viewLifecycleOwner, {
            if (it) {

                val builder = AlertDialog.Builder(requireContext())

                builder.setMessage("¿Confirmar datos y grabar evidencia con audio?")
                    .setPositiveButton(
                        "Continuar"
                    ) { _, _ ->
                        viewModel.guardarRegistroSanitizacion(false)
                    }
                    .setNegativeButton(
                        "Cancelar"
                    ) { _, _ ->
                        // User cancelled the dialog
                        Timber.d("Cancelado")
                        Toast.makeText(
                            requireContext(), "Se cancela registro", Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNeutralButton(
                        "Video"
                    ) { _, _ ->
                        viewModel.guardarRegistroSanitizacion()
                    }

                builder.create().show()

                viewModel.onShowConfirmDialogComplete()
            }
        })

        viewModel.navigateToRecordVideoFragment.observe(viewLifecycleOwner, {
            if (it) {
                this.findNavController().navigate(
                    CapturaSanitizacionFragmentDirections
                        .actionCapturaSanitizacionToRecordVideoFragment(
                            "SANITIZACION",
                            viewModel.idSanitizacion
                        )
                )

                viewModel.onNavigationComplete()
            }
        })

        viewModel.navigateToRecordAudioFragment.observe(viewLifecycleOwner, {
            if (it) {
                this.findNavController().navigate(
                    CapturaSanitizacionFragmentDirections
                        .actionCapturaSanitizacionToRecordAudioFragment(
                            "SANITIZACION",
                            viewModel.idSanitizacion
                        )
                )

                viewModel.onNavigationComplete()
            }
        })

        viewModel.navigateToListadoSanitizaciones.observe(viewLifecycleOwner, {
            if (it) {
                this.findNavController().navigate(
                    R.id.action_capturaSanitizacion_to_listadoSanitizaciones
                )

                viewModel.onNavigationComplete()
            }
        })
    }
}