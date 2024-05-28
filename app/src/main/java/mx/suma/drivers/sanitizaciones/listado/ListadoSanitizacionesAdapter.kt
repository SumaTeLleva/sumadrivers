package mx.suma.drivers.sanitizaciones.listado

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import mx.suma.drivers.databinding.ListItemSanitizacionBinding
import mx.suma.drivers.models.db.SanitizacionModel

class ListadoSanitizacionesAdapter :
    ListAdapter<SanitizacionModel, ListadoSanitizacionesAdapter.ViewHolder>(
        SanitizacionesDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(item)
    }

    class ViewHolder private constructor(val binding: ListItemSanitizacionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SanitizacionModel) {
            binding.sanitizacion = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val view = ListItemSanitizacionBinding.inflate(inflater, parent, false)

                return ViewHolder(view)
            }
        }
    }
}

class SanitizacionesDiffCallback : DiffUtil.ItemCallback<SanitizacionModel>() {
    override fun areItemsTheSame(
        oldItem: SanitizacionModel,
        newItem: SanitizacionModel
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: SanitizacionModel,
        newItem: SanitizacionModel
    ): Boolean {
        return oldItem == newItem
    }
}