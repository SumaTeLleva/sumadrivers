package mx.suma.drivers.tickets.listado

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import mx.suma.drivers.R
import mx.suma.drivers.SumaDriversViewModel
import mx.suma.drivers.databinding.ListadoTicketsFragmentBinding
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.network.isOnline
import mx.suma.drivers.repositories.tickets.TicketsRepositoryImpl
import mx.suma.drivers.repositories.tickets.remote.TicketsRemoteDataSourceImpl
import mx.suma.drivers.utils.EstatusLCE
import mx.suma.drivers.utils.TypeError
import org.koin.android.ext.android.inject

class ListadoTicketsFragment : Fragment() {

    private val service: ApiSuma by inject()

    private val fromBotton: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.from_bottom_anim) }
    private val toBotton: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.to_bottom_anim) }

    private var clicked = false

    private lateinit var viewModel: ListadoTicketsViewModel
    private val sharedViewModel: SumaDriversViewModel by activityViewModels()

    private lateinit var binding: ListadoTicketsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = ListadoTicketsFragmentBinding.inflate(inflater, container, false)

        val dataSource = TicketsRemoteDataSourceImpl(service)
        val repository = TicketsRepositoryImpl(dataSource)
        val factory = ListadoTicketsViewModelFactory(repository)
        val adapter = ListadoTicketsAdapter()

        viewModel = ViewModelProvider(this, factory).get(ListadoTicketsViewModel::class.java)

        binding.apply {
            localViewModel = viewModel
            lifecycleOwner = this@ListadoTicketsFragment
            ticketsList.adapter = adapter
            ticketsList.layoutManager = LinearLayoutManager(activity)
        }

        setupObservers(adapter)

        binding.fab.setOnClickListener {
            onOpenButtonClicked()
        }

        requireActivity().title = "Tickets del mes"

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Listar Tickets")
            param(
                FirebaseAnalytics.Param.SCREEN_CLASS,
                ListadoTicketsFragment::class.java.toString()
            )
        }
    }

    private fun onOpenButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        setClickeable(clicked)
        clicked = !clicked
    }

    private fun setVisibility(clicked:Boolean) {
        if(!clicked){
            binding.fabAdd.visibility = View.VISIBLE
            binding.lyAdd.visibility = View.VISIBLE
            binding.fabgas.visibility = View.VISIBLE
            binding.lyMapGas.visibility = View.VISIBLE
        }else {
            binding.fabAdd.visibility = View.INVISIBLE
            binding.lyAdd.visibility = View.INVISIBLE
            binding.fabgas.visibility = View.INVISIBLE
            binding.lyMapGas.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked:Boolean) {
        if(!clicked) {
            binding.fabAdd.startAnimation(fromBotton)
            binding.textAdd.startAnimation(fromBotton)
            binding.fabgas.startAnimation(fromBotton)
            binding.textMapGas.startAnimation(fromBotton)
            binding.fab.setIconResource(R.drawable.ic_close)
        } else {
            binding.fabAdd.startAnimation(toBotton)
            binding.textAdd.startAnimation(toBotton)
            binding.fabgas.startAnimation(toBotton)
            binding.textMapGas.startAnimation(toBotton)
            binding.fab.setIconResource(R.drawable.ic_more_option)
        }
    }

    private fun setClickeable(clicked: Boolean) {
        if(!clicked) {
            binding.fabAdd.isClickable = true
            binding.fabgas.isClickable = true
        } else {
            binding.fabAdd.isClickable = false
            binding.fabgas.isClickable = false
        }
    }

    private fun setupObservers(adapter: ListadoTicketsAdapter) {

        viewModel.error.observe(viewLifecycleOwner){
            val fbNew:FloatingActionButton = binding.root.findViewById<FloatingActionButton>(R.id.fabAdd)
            when(it) {
                TypeError.SERVICE -> {
                    fbNew?.isEnabled = false
                    sendMessageError("Ocurrio un error interno, intente más tarde")
                }
                TypeError.NETWORK -> {
                    fbNew?.isEnabled = false
                    sendMessageError("Sin conexión a internet")
                }
                TypeError.EMPTY -> {
                    fbNew?.isEnabled = true
                }
                TypeError.OTHER -> {
                    fbNew?.isEnabled = false
                    sendMessageError("Ocurrio un problema, favor de reportarlo")
                }
                else -> {
                    TODO("No realiza ninguna acción")
                }
            }
        }

        sharedViewModel.usuario.observe(viewLifecycleOwner) {
            it?.let {
                viewModel.usuario.value = it
                try {
                    detectInternet()
                    viewModel.getTickets()
                }catch (e:Exception) {
                    viewModel.estatus.value = EstatusLCE.ERROR
                }
            }
        }

        viewModel.tickets.observe(viewLifecycleOwner) { adapter.submitList(it) }

        viewModel.navigateToCapturaTicketsFragment.observe(viewLifecycleOwner) {
            if (it) {
                onOpenButtonClicked()
                this.findNavController().navigate(
                    R.id.action_ticketsFragment_to_capturaTicketFragment
                )

                viewModel.onNavigationComplete()
            }
        }

        viewModel.navigateToMapaGasActivity.observe(viewLifecycleOwner) {
            if(it) {
                onOpenButtonClicked()
                this.findNavController().navigate(
                    R.id.action_ticketsFragment_to_mapaGasActivity
                )

                viewModel.onNavigationComplete()
            }
        }
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
        println(tvMensaje.text)
        tvMensaje.text = message
    }
}