package mx.suma.drivers.tickets.listado

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import mx.suma.drivers.databinding.ListItemTicketBinding
import mx.suma.drivers.models.db.TicketModel

class ListadoTicketsAdapter : ListAdapter<TicketModel, ListadoTicketsAdapter.ViewHolder>(
    TicketsDiffCallback()
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

    class ViewHolder private constructor(val binding: ListItemTicketBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TicketModel) {
            binding.ticket = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val view = ListItemTicketBinding
                    .inflate(inflater, parent, false)

                return ViewHolder(
                    view
                )
            }
        }
    }
}

class TicketsDiffCallback : DiffUtil.ItemCallback<TicketModel>() {
    override fun areItemsTheSame(oldItem: TicketModel, newItem: TicketModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TicketModel, newItem: TicketModel): Boolean {
        return oldItem == newItem
    }
}