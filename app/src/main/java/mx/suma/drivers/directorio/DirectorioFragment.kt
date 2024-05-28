package mx.suma.drivers.directorio

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import mx.suma.drivers.R
import mx.suma.drivers.databinding.DirectorioFragmentBinding
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.network.isOnline
import mx.suma.drivers.repositories.directorio.DirectorioTelefonicoRepositoryImpl
import mx.suma.drivers.repositories.directorio.remote.DirectorioRemoteDataSourceImpl
import mx.suma.drivers.utils.EstatusLCE
import mx.suma.drivers.utils.TypeError
import org.koin.android.ext.android.inject
import timber.log.Timber

class DirectorioFragment : Fragment() {

    private lateinit var viewModel: DirectorioViewModel
    private val service: ApiSuma by inject()
    lateinit var binding: DirectorioFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
         binding =
            DataBindingUtil.inflate(inflater, R.layout.directorio_fragment, container, false)

//        val application = requireNotNull(activity).application
//        val database = SumaDriversDatabase.getInstance(application)

        val dataSource = DirectorioRemoteDataSourceImpl(service)
//        val repository = DirectorioTelefonicoRepositoryImpl(appState, dataSource, database.directorioDao)
        val directorioRepositoryImpl = DirectorioTelefonicoRepositoryImpl(dataSource)
        val factory = DirectorioViewModelFactory(directorioRepositoryImpl)

        val adapter = DirectorioAdapter(ContactoClickListener {
            viewModel.onContactoClicked(it)
        })

        val layoutManager = LinearLayoutManager(activity)

        viewModel = ViewModelProvider(this, factory).get(DirectorioViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.directorioList.adapter = adapter
        binding.directorioList.layoutManager = layoutManager

        viewModel.directorio.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.error.observe(viewLifecycleOwner) {
            when(it) {
                TypeError.NETWORK -> {
                    Toast.makeText(context, "Sin conexión a internet", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // TODO no muestra toast
                }
            }
        }

        viewModel.phoneNumber.observe(viewLifecycleOwner) {
            if (viewModel.hacerLlamada.value!!) {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$it")
                }
                if (intent.resolveActivity(requireActivity().packageManager) != null) {
                    startActivity(intent)

                    viewModel.onLlamadaRealizada()
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) {
            when(it) {
                TypeError.SERVICE -> {
                    sendMessageError("Ocurrio un error interno, intente más tarde")
                }
                TypeError.NETWORK -> {
                    sendMessageError("Sin conexión a internet")
                }
                TypeError.EMPTY -> {
                    sendMessageError("")
                }
                else -> {}
            }
        }
        setupListeners();

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        try {
            detectInternet()
            viewModel.getDirectorio()
        }catch (e:Exception) {
            viewModel.estatus.value = EstatusLCE.ERROR;
        }
    }

    override fun onResume() {
        super.onResume()

        try {
            Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, "Directorio")
                param(FirebaseAnalytics.Param.SCREEN_CLASS, DirectorioFragment::class.java.toString())
            }
        }catch (e: Exception) {
            Timber.d(e)
        }
    }

    fun setupListeners() {
        binding.buscadorSearch.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.directorio.value
                return false;
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterDirectory(newText);
                return false;
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
