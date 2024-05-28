package mx.suma.drivers.othersKit.list

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import mx.suma.drivers.R
import mx.suma.drivers.SumaDriversViewModel
import mx.suma.drivers.databinding.ListOtherKitBinding
import mx.suma.drivers.session.AppStateImpl
import mx.suma.drivers.utils.SumadriversFirebaseEvents
import org.koin.android.ext.android.inject

class ListOtherKitFragment() : Fragment() {
    private val appState: AppStateImpl by inject()
    private lateinit var viewModel: ListOtherKitViewModel
    lateinit var binding: ListOtherKitBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val sharedViewModel: SumaDriversViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.list_other_kit, container, false)
        firebaseAnalytics = Firebase.analytics

        val factory = ListOtherKitViewModelFactory(appState)
        viewModel = ViewModelProvider(this, factory).get(ListOtherKitViewModel::class.java)
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
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Solicitud")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, ListOtherKitFragment::class.java.toString())
        }
    }

    private fun setupObservers() {

        viewModel.navigateToSparePart.observe(viewLifecycleOwner) {
            if(it) {
                if(viewModel.esActivoEnTiempo()) {
                    sharedViewModel.usuario.value?.let { usuario ->
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://sumaenlinea.mx/cgi-bin/e_web.EXE/Agrega_Registro?Modulo=TR_REFACCI&Indice=NUEVO&ID_UNIDAD=${usuario.idUnidad}&Operador=${usuario.idOperadorSuma}")
                        )
                        startActivity(intent)
                    }
                    firebaseAnalytics.logEvent(SumadriversFirebaseEvents.REFACCIONES_LAUNCH) {
                        param("timestamp", System.currentTimeMillis())
                    }
                } else {
                    firebaseAnalytics.logEvent(SumadriversFirebaseEvents.REFACCIONES_FAILED) {
                        param("timestamp", System.currentTimeMillis())
                    }

                    Toast.makeText(
                        context,
                        getString(R.string.aviso_kit_limpieza),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        viewModel.navigateToADBLUE.observe(viewLifecycleOwner) {
            if(it) {
                if(viewModel.esActivoEnTiempo()) {
                    sharedViewModel.usuario.value?.let { usuario ->
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://sumaenlinea.mx/cgi-bin/e_web.EXE/Agrega_Registro?Modulo=TR_ADBLUE&Indice=NUEVO&ID_UNIDAD=${usuario.idUnidad}&Operador=${usuario.idOperadorSuma}")
                        )
                        startActivity(intent)
                    }
                    firebaseAnalytics.logEvent(SumadriversFirebaseEvents.ADBLUE_LAUNCH) {
                        param("timestamp", System.currentTimeMillis())
                    }
                } else {
                    firebaseAnalytics.logEvent(SumadriversFirebaseEvents.ADBLUE_FAILED) {
                        param("timestamp", System.currentTimeMillis())
                    }

                    Toast.makeText(
                        context,
                        getString(R.string.aviso_kit_limpieza),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        viewModel.navigateToKitLimpieza.observe(viewLifecycleOwner) {
            if(it) {
                if(viewModel.esActivoEnTiempo()){
                    sharedViewModel.usuario.value?.let { usuario ->
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://sumaenlinea.mx/cgi-bin/e_web.EXE/Agrega_Registro?Modulo=TR_KITL&Indice=NUEVO&ID_UNIDAD=${usuario.idUnidad}&Operador=${usuario.idOperadorSuma}")
                        )
                        startActivity(intent)
                    }
                    firebaseAnalytics.logEvent(SumadriversFirebaseEvents.KIT_LIMPIEZA_LAUNCH) {
                        param("timestamp", System.currentTimeMillis())
                    }
                } else {
                    firebaseAnalytics.logEvent(SumadriversFirebaseEvents.KIT_LIMPIEZA_FAILED) {
                        param("timestamp", System.currentTimeMillis())
                    }

                    Toast.makeText(
                        context,
                        getString(R.string.aviso_kit_limpieza),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}