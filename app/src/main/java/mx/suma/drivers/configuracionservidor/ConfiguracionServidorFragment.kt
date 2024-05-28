package mx.suma.drivers.configuracionservidor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import mx.suma.drivers.MainActivity
import mx.suma.drivers.R
import mx.suma.drivers.databinding.ConfiguracionServidorFragmentBinding
import mx.suma.drivers.session.AppStateImpl
import mx.suma.drivers.utils.configuration
import org.koin.android.ext.android.inject

class ConfiguracionServidorFragment : Fragment() {
    private val appState: AppStateImpl by inject()

    private lateinit var viewModel: ConfiguracionServidorViewModel

    private lateinit var binding: ConfiguracionServidorFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity).supportActionBar?.show()
        binding = ConfiguracionServidorFragmentBinding.inflate(inflater, container, false)
        val factory =ConfiguracionServidorFactory(appState, configuration.baseURL)

        viewModel = ViewModelProvider(this, factory).get(ConfiguracionServidorViewModel::class.java)
        binding.viewModel = viewModel

        initialInfomation()
        onListenerSuscription()

        return binding.root
    }


    override fun onResume() {
        super.onResume()
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Configuracion de servidor")
            param(
                FirebaseAnalytics.Param.SCREEN_CLASS,
                ConfiguracionServidorFragment::class.java.toString()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as MainActivity).supportActionBar?.hide()
    }

    fun initialInfomation() {
        binding.tietHostServer.setText(viewModel.host_server)
    }

    fun onLoadingButton(isLoading:Boolean) {
        if(isLoading){
            binding.btnSaveHost.isEnabled = false
            binding.tietHostServer.isEnabled = false
        }else {
            binding.btnSaveHost.isEnabled = true
            binding.tietHostServer.isEnabled = true
        }
    }

    fun resultLayoutOk(isOk: Boolean, view: View) {
        binding.tilHostServer.endIconMode = TextInputLayout.END_ICON_CUSTOM
        binding.tietHostServer.isEnabled = true
        if(isOk) {
            binding.tilHostServer.setEndIconDrawable(ContextCompat.getDrawable(view.context, R.drawable.ic_check_circle))
        }else {
            binding.tilHostServer.endIconMode = TextInputLayout.END_ICON_NONE
        }
    }

    fun onActionSaveHostServer(view: View) {
        val new_host = binding.tietHostServer.text;
        if(URLUtil.isValidUrl(new_host.toString())){
            onLoadingButton(true)
            var host = binding.tietHostServer.text.toString()
            viewModel.guardarHostServer(host)
            configuration.baseURL = host
            (activity as MainActivity).myApplication.refreshScope()

            resultLayoutOk(true, view)
            onLoadingButton(false)
            (activity as MainActivity).reiniciarInstancia()
        } else {
            binding.tilHostServer.error = "formato de host invalido"
        }
    }
    fun onListenerSuscription() {
        binding.btnSaveHost.setOnClickListener{onActionSaveHostServer(it)}

        binding.tietHostServer.addTextChangedListener {
            it?.let {
                if(it.isNotEmpty()){
                    if(URLUtil.isValidUrl(it.toString())){
                        binding.btnSaveHost.isEnabled = true
                        binding.tilHostServer.error = ""
                    }else {
                        binding.tilHostServer.error = "formato de host invalido"
                    }
                }else {
                    binding.btnSaveHost.isEnabled = false
                    binding.tilHostServer.error = "Campo de host vacio"
                }
            }
        }
    }
}