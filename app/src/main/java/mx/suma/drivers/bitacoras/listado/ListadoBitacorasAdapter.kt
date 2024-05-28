package mx.suma.drivers.bitacoras.listado

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import mx.suma.drivers.models.db.BitacoraModel
import mx.suma.drivers.databinding.ListItemBitacoraBinding

class ListadoBitacorasAdapter(val clickListener: BitacorasClickListener) : ListAdapter<BitacoraModel, ListadoBitacorasAdapter.ViewHolder>(
    BitacorasDiffCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(
            parent
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    class ViewHolder private constructor(val binding: ListItemBitacoraBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BitacoraModel, clickListener: BitacorasClickListener) {
            binding.bitacora = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val view = ListItemBitacoraBinding
                    .inflate(inflater, parent, false)

                return ViewHolder(
                    view
                )
            }
        }
    }
}

class BitacorasClickListener(val clickListener: (bitacoraId: Long) -> Unit) {
    fun onClick(bitacoraDb: BitacoraModel) = clickListener(bitacoraDb.id)
}

class BitacorasDiffCallback : DiffUtil.ItemCallback<BitacoraModel>() {
    override fun areItemsTheSame(oldItem: BitacoraModel, newItem: BitacoraModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: BitacoraModel, newItem: BitacoraModel): Boolean {
        return oldItem == newItem
    }
}