package mx.suma.drivers.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import mx.suma.drivers.MainActivity
import mx.suma.drivers.R
import mx.suma.drivers.database.SumaDriversDatabase
import mx.suma.drivers.databinding.LoginFragmentBinding
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.session.AppStateImpl
import mx.suma.drivers.utils.SumadriversFirebaseEvents
import org.koin.android.ext.android.inject
import timber.log.Timber


class LoginFragment : Fragment() {

    private lateinit var binding: LoginFragmentBinding
    private lateinit var viewModel: LoginViewModel

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val appState: AppStateImpl by inject()
    private val service: ApiSuma by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as MainActivity).supportActionBar?.hide();
        binding = DataBindingUtil
            .inflate(inflater, R.layout.login_fragment, container, false)
        val application = requireNotNull(activity).application
        val datasource = SumaDriversDatabase.getInstance(application).usuarioDao
        val viewModelFactory = LoginViewModelFactory(service, appState, datasource)
        viewModel = ViewModelProvider(this, viewModelFactory).get(LoginViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.loginButton.setOnClickListener { sendLoginData(it) }
        binding.btnOpenConfigServer.setOnClickListener { openConfigServer() }

        viewModel.nvigateToStarterApp.observe(viewLifecycleOwner, Observer {
            if (it) {
                this.findNavController()
                    .navigate(LoginFragmentDirections.actionLoginFragmentToStarterAppFragment())
                viewModel.onNavigationComplete()

                firebaseAnalytics.logEvent(SumadriversFirebaseEvents.LOGIN_SUCCESS) {
                    param("timestamp", System.currentTimeMillis())
                }
            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            if(it.isNotEmpty()) {
                firebaseAnalytics.logEvent(SumadriversFirebaseEvents.LOGIN_FAILED) {
                    param("timestamp", System.currentTimeMillis())
                    param("message", it)
                }
            }
        })

        firebaseAnalytics = Firebase.analytics

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Login")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, LoginFragment::class.java.toString())
        }
    }

    private fun sendLoginData(view: View) {
        binding.apply {
            MainActivity.variabledeinicio.anuncios = true
            val email = etLoginEmail.text.toString()
            val password = etLoginPassword.text.toString()
            val phone = etLoginPhone.text.toString()

            viewModel?.submitLoginData(phone, email, password)
            Timber.d("Login with email and password")

            firebaseAnalytics.logEvent(SumadriversFirebaseEvents.LOGIN_ATTEMPT) {
                param("timestamp", System.currentTimeMillis())
                param("email", email)
                param("phone", phone)
            }
        }

        val imm =
            this.activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun openConfigServer() {
        findNavController().navigate(
            LoginFragmentDirections.actionLoginFragmentToConfiguracionServidorFragement()
        )
    }
}
