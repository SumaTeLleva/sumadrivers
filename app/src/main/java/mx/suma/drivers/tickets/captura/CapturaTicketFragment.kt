package mx.suma.drivers.tickets.captura

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import mx.suma.drivers.R
import mx.suma.drivers.SumaDriversViewModel
import mx.suma.drivers.database.SumaDriversDatabase
import mx.suma.drivers.databinding.CapturaTicketFragmentBinding
import mx.suma.drivers.models.db.UsuarioModel
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.repositories.proveedores.ProveedoresRepositoryImpl
import mx.suma.drivers.repositories.proveedores.remote.ProveedoresRemoteDataSourceImpl
import mx.suma.drivers.repositories.tickets.TicketsRepositoryImpl
import mx.suma.drivers.repositories.tickets.remote.TicketsRemoteDataSourceImpl
import mx.suma.drivers.repositories.unidades.UnidadesRepositoryImpl
import mx.suma.drivers.repositories.unidades.remote.UnidadesRemoteDataSourceImpl
import mx.suma.drivers.session.AppStateImpl
import mx.suma.drivers.utils.formatAsDateTimeString
import mx.suma.drivers.utils.now
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.NumberFormat


class CapturaTicketFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private val apiSuma: ApiSuma by inject()
    private val appState: AppStateImpl by inject()
    private val database: SumaDriversDatabase by inject()

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var viewModel: CapturaTicketViewModel
    private val sharedViewModel: SumaDriversViewModel by activityViewModels()

    private lateinit var binding: CapturaTicketFragmentBinding

    private lateinit var usuario: UsuarioModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CapturaTicketFragmentBinding.inflate(inflater, container, false)
        firebaseAnalytics = Firebase.analytics

        val ticketsRemoteDataSource = TicketsRemoteDataSourceImpl(apiSuma)
        val ticketsRepository = TicketsRepositoryImpl(ticketsRemoteDataSource)
        val proveedorDao = database.proveedorDao

        val proveedoresRemoteDataSource = ProveedoresRemoteDataSourceImpl(apiSuma)
        val proveedoresRepository = ProveedoresRepositoryImpl(appState, proveedoresRemoteDataSource, proveedorDao)

        val unidadesRemoteDataSource = UnidadesRemoteDataSourceImpl(apiSuma)
        val unidadesRepository = UnidadesRepositoryImpl(appState, unidadesRemoteDataSource)

        val factory =
            CapturaTicketViewModelFactory(appState, proveedoresRepository, ticketsRepository, unidadesRepository)

        viewModel = ViewModelProvider(this, factory).get(CapturaTicketViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setupObservers()
        setupListeners()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Registrar Ticket")
            param(
                FirebaseAnalytics.Param.SCREEN_CLASS,
                CapturaTicketFragment::class.java.toString()
            )
        }
    }

    private fun setupListeners() {
        binding.spTicketGasolineras.onItemSelectedListener = this
        viewModel.viewModelScope.launch {
            var response = sharedViewModel.usuario.value?.idUnidad?.let {
                viewModel.getUltimoKilometraje(it)
            }
            viewModel.kilometraje.value = response
            binding.etTicketKilometraje.setText(response?.kilometraje.toString())
        }
        binding.etTicketKilometraje.addTextChangedListener {
            it?.let {
                if (it.isNotEmpty()) {
                    viewModel.setKilometraje(it.toString())
                    if(it.toString().toLong() < viewModel.kilometraje.value!!.kilometraje) {
                        binding.tilTicketKilometraje.error = getString(R.string.msj_error_kilometraje_Min)
                    }else {
                        binding.tilTicketKilometraje.error = null
                    }
                } else {
                    viewModel.setKilometraje("0")
                    binding.tilTicketKilometraje.error = getString(R.string.msj_campo_vacio)
                }
            }
            isEmptyInputs();
        }

        binding.etTicketLitros.addTextChangedListener {
            it?.let {
                if (it.isNotEmpty()) {
                    viewModel.setLitros(it.toString())
                    binding.tilTicketLitros.error = null
                } else {
                    viewModel.setLitros("0")
                    binding.tilTicketLitros.error = getString(R.string.msj_campo_vacio)
                }
            }
            isEmptyInputs();
        }

        binding.etTicketPrecio.addTextChangedListener(object : TextWatcher {
            private var current: String = ""
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (s.toString() != current) {
                    binding.etTicketPrecio.removeTextChangedListener(this)

                    val cleanString: String = s?.replace("""[$,.]""".toRegex(), "") ?: ""

                    val parsed = cleanString.toDouble()
                    val formatted = NumberFormat.getCurrencyInstance().format((parsed / 100))

                    current = formatted
                    binding.etTicketPrecio.setText(formatted)
                    binding.etTicketPrecio.setSelection(formatted.length)

                    binding.etTicketPrecio.addTextChangedListener(this)
                }
            }

            override fun afterTextChanged(it: Editable?) {
                if(!it.toString().isNullOrEmpty()) {
                    val formatted = viewModel.convertCurrencyToDouble(it.toString());
                    if(formatted <= 0) {
                        binding.tilTicketPrecio.error = "El precio tiene que ser mayor"
                    } else if( formatted > 99.99){
                        binding.tilTicketPrecio.error = "El precio tiene que ser menor"
                    } else {
                        binding.tilTicketPrecio.error = null
                    }
                    viewModel.setPrecio(formatted.toString())
                }
                isEmptyInputs();
            }

        })
        /*binding.etTicketPrecio.addTextChangedListener {
            it?.let {
                if (it.isNotEmpty()) {
                    val def = DecimalFormat("##.##")
                    viewModel.setPrecio(def.format(it.toString().toDouble()).toString())
                    binding.tilTicketPrecio.error = null
                } else {
                    viewModel.setPrecio("0")
                    binding.tilTicketPrecio.error = getString(R.string.msj_campo_vacio)
                }
            }
            isEmptyInputs();
        }*/

        binding.etTicketFolio.addTextChangedListener {
            it?.let {
                if (it.isNotEmpty()) {
                    viewModel.setFolio(it.toString())
                    binding.tilTicketFolio.error = null
                } else {
                    viewModel.setFolio("")
                    binding.tilTicketFolio.error = getString(R.string.msj_campo_vacio)
                }
            }
            isEmptyInputs();
        }
    }

    private fun conditionPrice (input: TextInputEditText):Boolean {
        return if(!input.text.isNullOrEmpty()) {
            viewModel.convertCurrencyToDouble(input.text.toString()) > 0 && viewModel.convertCurrencyToDouble(input.text.toString()) < 99.99
        }else {
            false
        }
    }

    private fun isEmptyInputs() {
        binding.btnEnviarTicket.isEnabled =
            (!binding.etTicketKilometraje.text.isNullOrEmpty() &&
                    !binding.etTicketLitros.text.isNullOrEmpty() &&
                    conditionPrice(binding.etTicketPrecio) &&
                    !binding.etTicketFolio.text.isNullOrEmpty() && viewModel.getGasolinera() > 0 &&
                    binding.etTicketKilometraje.error == null)
    }

    private fun setupObservers() {

        sharedViewModel.usuario.observe(viewLifecycleOwner) {
            it?.let {
                viewModel.usuario.value = it
            }
        }

        viewModel.gasolineras.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val adapter = ArrayAdapter(
                    requireContext(), android.R.layout.simple_spinner_item, it
                )
                adapter.insert("0 - SELECCIONE GASOLINERA", 0)

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                binding.spTicketGasolineras.adapter = adapter
            }
        }

        viewModel.ticketGenerado.observe(viewLifecycleOwner) {
            if (it != -1L) {
                Toast.makeText(requireContext(), "Ticket generado con éxito", Toast.LENGTH_SHORT)
                    .show()

                this.findNavController().navigate(
                    R.id.action_capturaTicketFragment_to_ticketsFragment
                )

                viewModel.onNavigationComplete()
            }
        }

        viewModel.failedRequest.observe(viewLifecycleOwner) {
            try {
                if (it) {
                    firebaseAnalytics.logEvent("validation_error_ticket") {
                        param("dt", now().time.formatAsDateTimeString())
                        param("usuario_id", viewModel.usuario.value?.id!!)
                        param("unidad_id", viewModel.usuario.value?.idUnidad!!)
                    }

                    binding.btnEnviarTicket.text = "Reintentar"
                }
            }catch (e:Exception) {
                Timber.d(e)
            }
        }
        viewModel.sendingRequest.observe(viewLifecycleOwner) {
            println("imprimir sendingRequest $it")
            binding.btnEnviarTicket.isEnabled = !it
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        when (parent.id) {
            R.id.sp_ticket_gasolineras -> {
                val item = parent.getItemAtPosition(position).toString()
                viewModel.setGasolinera(item)
                if(viewModel.getGasolinera() <= 0) {
                    binding.tilSpTicketGasolineras.error = getString(R.string.msj_campo_vacio)
                }else {
                    binding.tilSpTicketGasolineras.error = null
                }
                isEmptyInputs()
            }
        }
    }

}


