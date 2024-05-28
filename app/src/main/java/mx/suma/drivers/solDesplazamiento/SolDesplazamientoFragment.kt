package mx.suma.drivers.solDesplazamiento

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import mx.suma.drivers.R
import mx.suma.drivers.SumaDriversViewModel
import mx.suma.drivers.databinding.SolDesplazamientoFragmentBinding
import mx.suma.drivers.session.AppStateImpl
import mx.suma.drivers.utils.SumadriversFirebaseEvents
import org.koin.android.ext.android.inject

class SolDesplazamientoFragment : Fragment() {
    private val appState: AppStateImpl by inject()
    private lateinit var viewModel: SolDesplazamientoViewModel
    lateinit var binding: SolDesplazamientoFragmentBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val sharedViewModel: SumaDriversViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = Firebase.analytics
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.sol_desplazamiento_fragment, container, false)

        val factory = SolDesplazamientoViewModelFactory(appState)
        viewModel = ViewModelProvider(this, factory).get(SolDesplazamientoViewModel::class.java)
        binding.apply {
            localViewModel = viewModel
            mainViewModel = sharedViewModel
            lifecycleOwner = viewLifecycleOwner
        }

        setupObservers()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Solicitud desplazamiento")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, SolDesplazamientoFragment::class.java.toString())
        }
    }

    private fun setupObservers() {
        viewModel.navigateToRequestID.observe(viewLifecycleOwner) {
            if(it) {
                sharedViewModel.usuario.value?.let { usuario ->
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://sumaenlinea.mx/cgi-bin/e_web.EXE/Agrega_Registro?Modulo=TI_SOLI_OP&Indice=NUEVO&ID_UNIDAD=${usuario.idUnidad}&Operador=${usuario.idOperadorSuma}")
                    )
                    startActivity(intent)
                }
                firebaseAnalytics.logEvent(SumadriversFirebaseEvents.SOLICITUD_ID_LAUNCH) {
                    param("timestamp", System.currentTimeMillis())
                }
            }
        }

        viewModel.navigateToRequestTH.observe(viewLifecycleOwner) {
            if(it) {
                sharedViewModel.usuario.value?.let { usuario ->
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://sumaenlinea.mx/cgi-bin/e_web.EXE/Agrega_Registro?Modulo=TH_SOLI_OP&Indice=NUEVO&ID_UNIDAD=${usuario.idUnidad}&Operador=${usuario.idOperadorSuma}")
                    )
                    startActivity(intent)
                }
                firebaseAnalytics.logEvent(SumadriversFirebaseEvents.SOLICITUD_TH_LAUNCH) {
                    param("timestamp", System.currentTimeMillis())
                }
            }
        }

        viewModel.navigateToRequestAdmin.observe(viewLifecycleOwner) {
            if(it) {
                sharedViewModel.usuario.value?.let { usuario ->
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://sumaenlinea.mx/cgi-bin/e_web.EXE/Agrega_Registro?Modulo=ADM_SOL_OP&Indice=NUEVO&ID_UNIDAD=${usuario.idUnidad}&Operador=${usuario.idOperadorSuma}")
                    )
                    startActivity(intent)
                }
                firebaseAnalytics.logEvent(SumadriversFirebaseEvents.SOLICITUD_ADMIN_LAUNCH) {
                    param("timestamp", System.currentTimeMillis())
                }
            }
        }

        viewModel.navigateToRequestStore.observe(viewLifecycleOwner) {
            if(it) {
                sharedViewModel.usuario.value?.let { usuario ->
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://sumaenlinea.mx/cgi-bin/e_web.EXE/Agrega_Registro?Modulo=ALM_SOL_OP&Indice=NUEVO&ID_UNIDAD=${usuario.idUnidad}&Operador=${usuario.idOperadorSuma}")
                    )
                    startActivity(intent)
                }
                firebaseAnalytics.logEvent(SumadriversFirebaseEvents.SOLICITUD_STORE_LAUNCH) {
                    param("timestamp", System.currentTimeMillis())
                }
            }
        }

        viewModel.navigateToRequestMante.observe(viewLifecycleOwner) {
            if(it) {
                sharedViewModel.usuario.value?.let { usuario ->
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://sumaenlinea.mx/cgi-bin/e_web.EXE/Agrega_Registro?Modulo=MAN_SOL_OP&Indice=NUEVO&ID_UNIDAD=${usuario.idUnidad}&Operador=${usuario.idOperadorSuma}")
                    )
                    startActivity(intent)
                }
                firebaseAnalytics.logEvent(SumadriversFirebaseEvents.SOLICITUD_MANTE_LAUNCH) {
                    param("timestamp", System.currentTimeMillis())
                }
            }
        }
    }
}