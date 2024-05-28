package mx.suma.drivers.directorio

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import mx.suma.drivers.databinding.ListItemContactoBinding
import mx.suma.drivers.models.db.ContactoModel

class DirectorioAdapter(val clickListener: ContactoClickListener): ListAdapter<ContactoModel, DirectorioAdapter.ViewHolder>(DirectorioDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ViewHolder private constructor(val binding: ListItemContactoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ContactoModel, clickListener: ContactoClickListener) {
            binding.contacto = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val view = ListItemContactoBinding
                    .inflate(inflater, parent, false)

                return ViewHolder(view)
            }
        }
    }
}

class ContactoClickListener(val listener: (telefono: String) -> Unit) {
    fun onClick(contactoModel: ContactoModel) = listener(contactoModel.telefono)
}

class DirectorioDiffCallback : DiffUtil.ItemCallback<ContactoModel>() {
    override fun areItemsTheSame(oldItem: ContactoModel, newItem: ContactoModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ContactoModel, newItem: ContactoModel): Boolean {
        return oldItem == newItem
    }
}