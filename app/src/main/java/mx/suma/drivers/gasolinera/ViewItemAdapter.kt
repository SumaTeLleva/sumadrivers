package mx.suma.drivers.gasolinera

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import mx.suma.drivers.R
import mx.suma.drivers.models.api.EstacionGas

class ViewItemAdapter(
    private val fusedLocation:  FusedLocationProviderClient,
    private val listGas: List<EstacionGas.Estacion>,
    private val onClick: (origen: LatLng, destino: LatLng) -> Unit
    ): RecyclerView.Adapter<ViewItemAdapter.ViewItemHolder>() {
    class ViewItemHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val gas_nombre:TextView = itemView.findViewById(R.id.gas_nombre)
        val gas_direccion:TextView = itemView.findViewById(R.id.gas_direccion)
        val gas_status: TextView = itemView.findViewById(R.id.gas_status)
        val gas_btn:MaterialButton = itemView.findViewById(R.id.btn_gas_goToRoute)
        val gas_colonia: TextView = itemView.findViewById(R.id.gas_colonia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewItemHolder {
        return ViewItemHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_maps_gas, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return listGas.size
    }


    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: ViewItemHolder, position: Int) {
        val gasStation = listGas[position]
        holder.gas_nombre.text = gasStation.NOMBRE
        holder.gas_direccion.text = gasStation.DOMICILIO
        holder.gas_colonia.text = gasStation.COLONIA
        if(gasStation.ACTIVO == 1){
            holder.gas_status.text = "Disponible"
            holder.gas_status.setTextColor(ContextCompat.getColor(holder.gas_status.context, R.color.green))
            holder.gas_btn.isEnabled = true
        }else {
            holder.gas_status.text = "No disponible"
            holder.gas_status.setTextColor(ContextCompat.getColor(holder.gas_status.context, R.color.md_red))
            holder.gas_btn.isEnabled = false
        }

        holder.gas_btn.setOnClickListener {
            fusedLocation.lastLocation.addOnSuccessListener { location ->
                if(location != null) {
                    val actual = LatLng(location.latitude, location.longitude)
                    val destino = LatLng(gasStation.LATITUD.toDouble(), gasStation.LONGITUD.toDouble())
                    onClick(actual, destino)
                }
            }
        }
    }
}