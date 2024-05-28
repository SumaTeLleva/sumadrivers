package mx.suma.drivers.mantenimientos.listado

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import mx.suma.drivers.databinding.ListItemMantenimientoBinding
import mx.suma.drivers.models.db.MantenimientoModel

class ListadoMantenimientosAdapter : ListAdapter<MantenimientoModel, ListadoMantenimientosAdapter.ViewHolder>(
    MantenimientosDiffCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(
            parent
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(item)
    }

    class ViewHolder private constructor(val binding: ListItemMantenimientoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MantenimientoModel) {
            binding.mantenimiento = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val view = ListItemMantenimientoBinding.inflate(inflater, parent, false)

                return ViewHolder(
                    view
                )
            }
        }
    }
}

class MantenimientosDiffCallback : DiffUtil.ItemCallback<MantenimientoModel>() {
    override fun areItemsTheSame(oldItem: MantenimientoModel, newItem: MantenimientoModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MantenimientoModel, newItem: MantenimientoModel): Boolean {
        return oldItem == newItem
    }
}