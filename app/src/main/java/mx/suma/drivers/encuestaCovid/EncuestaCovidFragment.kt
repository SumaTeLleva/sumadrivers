package mx.suma.drivers.encuestaCovid

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
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
import mx.suma.drivers.databinding.EncuestaCovidFragmentBinding
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.repositories.encuestas.EncuestasRepositoryImpl
import mx.suma.drivers.repositories.encuestas.remote.EncuestasRemoteDataSourceImpl
import mx.suma.drivers.session.AppStateImpl
import org.koin.android.ext.android.inject
import timber.log.Timber

class EncuestaCovidFragment : Fragment() {

    private val apiSuma: ApiSuma by inject()
    private val appState: AppStateImpl by inject()
    private val database: SumaDriversDatabase by inject()
    private val sharedViewModel: SumaDriversViewModel by activityViewModels()

    private lateinit var viewModel: EncuestaCovidViewModel
    private lateinit var binding: EncuestaCovidFragmentBinding

    // Hard coded value! ;( no time, no time!
    private val encuestaID = 1L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.encuesta_covid_fragment, container, false)

        val remoteDataSource = EncuestasRemoteDataSourceImpl(apiSuma)
        val repository = EncuestasRepositoryImpl(remoteDataSource)

        val factory = EncuestaCovidViewModelFactory(encuestaID, repository, appState, database.usuarioDao)

        viewModel = ViewModelProvider(this, factory).get(EncuestaCovidViewModel::class.java)

        binding.apply {
            localViewModel = viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        setupObservers()

        return binding.root
    }

    private fun setupObservers() {

        sharedViewModel.usuario.observe(viewLifecycleOwner) {
            viewModel.obtenerEncuesta(it)
        }

        viewModel.navigateToPanel.observe(viewLifecycleOwner) {
            if (it) {
                Timber.d("Navegar al Panel de Control")

                this.findNavController().navigate(
                    EncuestaCovidFragmentDirections.actionEncuestaCovidToPanelFragment2()
                )

                viewModel.onNavigationComplete()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Encuesta COVID")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, EncuestaCovidFragment::class.java.toString())
        }
    }
}