package mx.suma.drivers.auditorias

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.R

class ListadoAuditoriasFragment : Fragment() {

    private lateinit var viewModel: ListadoAuditoriasViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(ListadoAuditoriasViewModel::class.java)

        return inflater.inflate(R.layout.listado_auditorias_fragment, container, false)
    }
}
