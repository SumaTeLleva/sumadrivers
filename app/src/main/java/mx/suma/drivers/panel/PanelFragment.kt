package mx.suma.drivers.panel

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import me.relex.circleindicator.CircleIndicator3
import mx.suma.drivers.MainActivity
import mx.suma.drivers.R
import mx.suma.drivers.SumaDriversViewModel
import mx.suma.drivers.carousel.ViewPagerAdapter
import mx.suma.drivers.database.SumaDriversDatabase
import mx.suma.drivers.database.UsuarioDao
import mx.suma.drivers.databinding.PanelFragmentBinding
import mx.suma.drivers.models.db.UsuarioModel
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.repositories.archivos.ArchivosRepository
import mx.suma.drivers.repositories.archivos.ArchivosRepositoryImpl
import mx.suma.drivers.repositories.archivos.remote.ArchivosRemoteDataSourceImpl
import mx.suma.drivers.repositories.dispositivos.DispositivosRepository
import mx.suma.drivers.repositories.dispositivos.DispositivosRepositoryImpl
import mx.suma.drivers.repositories.dispositivos.remote.DispositivosRemoteDataSourceImpl
import mx.suma.drivers.session.AppState
import mx.suma.drivers.session.AppStateImpl
import mx.suma.drivers.utils.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class PanelFragment : Fragment() {

    private val appState: AppStateImpl by inject()
    private val apiSuma: ApiSuma by inject()
    private val database: SumaDriversDatabase by inject()
    private lateinit var viewPager: ViewPager2

    private lateinit var dispositivosRepository: DispositivosRepository
    private lateinit var archivosRepository: ArchivosRepository

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val sharedViewModel: SumaDriversViewModel by activityViewModels()

    private lateinit var panelViewModel: PanelViewModel

    private lateinit var usuario: UsuarioDao

    private lateinit var binding: PanelFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as MainActivity).supportActionBar?.title=""
        (activity as MainActivity).supportActionBar?.show()
        binding = DataBindingUtil
            .inflate(inflater, R.layout.panel_fragment, container, false)

        firebaseAnalytics = Firebase.analytics

        usuario = database.usuarioDao

        val remoteDataSource = DispositivosRemoteDataSourceImpl(apiSuma)
        dispositivosRepository = DispositivosRepositoryImpl(remoteDataSource)

        val archivoDataSource = ArchivosRemoteDataSourceImpl(apiSuma)
        archivosRepository = ArchivosRepositoryImpl(archivoDataSource)

        val factory = PanelViewModelFactory(appState, usuario, dispositivosRepository, apiSuma, archivosRepository)

        panelViewModel = ViewModelProvider(this, factory).get(PanelViewModel::class.java)

        binding.apply {
            localViewModel = panelViewModel
            mainViewModel = sharedViewModel
            lifecycleOwner = viewLifecycleOwner
        }
        setupObservers(appState)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Panel")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, PanelFragment::class.java.toString())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.panel_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.configuraciones -> {
                findNavController().navigate(
                    R.id.action_panelFragment_to_settingsFragment
                )
            }
            R.id.salir -> {
                panelViewModel.clearSession()

                firebaseAnalytics.logEvent(SumadriversFirebaseEvents.EXIT_APP) {
                    param("timestamp", System.currentTimeMillis())
                }
            }
            /*R.id.actualizar_app -> {
                val url = "https://api.sumaenlinea.mx/downloads/drivers/latest/app-release.apk"
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)

                firebaseAnalytics.logEvent(SumadriversFirebaseEvents.UPDATE_APP) {
                    param("timestamp", System.currentTimeMillis())
                    // TODO: Send username and id for tracking
                }
            }*/
            /*R.id.actualizar_cache -> {

                val oneTimeWorkRequest = OneTimeWorkRequestBuilder<ActualizarCacheWorker>().build()

                WorkManager.getInstance(requireContext()).enqueueUniqueWork(
                    ActualizarCacheWorker.WORKER_NAME,
                    ExistingWorkPolicy.REPLACE,
                    oneTimeWorkRequest
                )
            }*/
        }

        return true
    }
    private fun verificarSesion() {
        panelViewModel.uiScope.launch {
            val isSession = panelViewModel.esSesionCaducado()
            if(isSession) {
                panelViewModel.clearSession()

                firebaseAnalytics.logEvent(SumadriversFirebaseEvents.EXIT_APP_EXPIRE) {
                    param("timestamp", System.currentTimeMillis())
                }
            }else {
                openModalimage()
            }
        }
    }
    private fun setupObservers(appState: AppState) {
        sharedViewModel.usuario.observe(viewLifecycleOwner) {
            if (it == null) {
                panelViewModel.navigateToStarterFragment.value = true
            } else if (it.acceso && it.idUnidad < 1) {
                Timber.i("Id Unidad: ${it.idUnidad}")
                findNavController().navigate(
                    R.id.action_panelFragment_to_miUnidadFragment
                )
                panelViewModel.onNavigationComplete()
            } else if (appState.getFirebaseToken().isEmpty()) {
                if (!panelViewModel.registrandoDispositivo) {
                    getFirebaseToken()
                    panelViewModel.registrandoDispositivo = true
                }
            }
            if(it !== null && it.idUnidad > 0) {
                verificarSesion()
            }
        }

        panelViewModel.navigateToTickets.observe(viewLifecycleOwner) {
            if (it) {
                try {
                    findNavController().navigate(
                        R.id.action_panelFragment_to_ticketsFragment
                    )

                    panelViewModel.onNavigationComplete()
                }catch (e:Exception) {
                    Timber.e("ocurrio un error: ${e.message}")
                }
            }
        }

        panelViewModel.navigateToBitacoras.observe(viewLifecycleOwner) {
            if (it) {
                try {
                    findNavController().navigate(
                        R.id.action_panelFragment_to_listadoBitacorasFragment
                    )

                    panelViewModel.onNavigationComplete()
                }catch (e:Exception) {
                    Timber.e("ocurrio un error: ${e.message}")
                }
            }
        }

        panelViewModel.navigateToMantenimientos.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(
                    PanelFragmentDirections
                        .actionPanelFragmentToListadoMantenimientosFragment(
                            sharedViewModel.usuario.value as UsuarioModel
                        )
                )

                panelViewModel.onNavigationComplete()
            }
        }

        panelViewModel.navigateToDirectorio.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(
                    PanelFragmentDirections.actionPanelFragmentToDirectorioFragment()
                )

                panelViewModel.onNavigationComplete()
            }
        }

        panelViewModel.navigateToAuditorias.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(
                    PanelFragmentDirections.actionPanelFragmentToListadoAuditoriasFragment()
                )

                panelViewModel.onNavigationComplete()
            }
        }

        panelViewModel.navigateToOtherKit.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(
                    PanelFragmentDirections.actionPanelFragmentToOtherKitFragment()
                )
                panelViewModel.onNavigationComplete()
            }
        }

        panelViewModel.navigateToMiUnidad.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(
                    R.id.action_panelFragment_to_miUnidadFragment
                )

                panelViewModel.onNavigationComplete()
            }
        }

        panelViewModel.navigateToStarterFragment.observe(viewLifecycleOwner) {
            if (it) {
                try {
                    println(findNavController().currentDestination)
                    findNavController().navigate(
                        PanelFragmentDirections.actionPanelFragmentToStarterAppFragment()
                    )

                    panelViewModel.onNavigationComplete()
                }catch (e:Exception) {
                    Timber.e("ocurrio un error: ${e.message}")
                }
            }
        }

        panelViewModel.navigateToSanitizaciones.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(
                    R.id.action_panelFragment_to_listadoSanitizaciones
                )

                panelViewModel.onNavigationComplete()
            }
        }

        panelViewModel.navigateToSolDesplazamiento.observe(viewLifecycleOwner) {
            if(it) {
                findNavController().navigate(
                    R.id.action_panelFragment_to_solDesplazamiento
                )
                panelViewModel.onNavigationComplete()
            }
        }
    }

    private fun openModalimage() {
        try {
            if(MainActivity.variabledeinicio.anuncios)
            {
                val dialogView = layoutInflater.inflate(R.layout.carousel_fragment, null)
                viewPager = dialogView.findViewById(R.id.view_pager2)
                viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
                val indicator = dialogView.findViewById<CircleIndicator3>(R.id.indicator)
                panelViewModel.avisosImages.observe(viewLifecycleOwner) {
                    if(it.isNotEmpty()) {
                        val pageAdapter = ViewPagerAdapter(it)
                        viewPager.adapter = pageAdapter
                        indicator.setViewPager(viewPager)
                        AlertDialog.Builder(context).setView(dialogView).show()
                        MainActivity.variabledeinicio.anuncios = false
                    }
                }
            }
        } catch (e:Exception) {
            Timber.e("Error en panelFragment - Modal: ${e.message}")
        }
    }

    private fun getFirebaseToken() {
        if (appState.getFirebaseToken().isEmpty()) {
            FirebaseInstallations.getInstance().getToken(true)
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Timber.w(task.exception)
                        return@OnCompleteListener
                    }

                    task.result?.token?.let { token ->
                        sharedViewModel.usuario.value?.let { usuario ->
                            panelViewModel.handleToken(token, usuario)
                        }
                    }

                    Toast.makeText(context, "Token obtenido!", Toast.LENGTH_SHORT).show()
                })
        }
    }
}
