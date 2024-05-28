package mx.suma.drivers.carousel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import androidx.core.net.toUri
import com.bumptech.glide.request.RequestOptions
import mx.suma.drivers.GlideApp
import mx.suma.drivers.R

class ViewPagerAdapter(val images: ArrayList<String>) : RecyclerView.Adapter<ViewPagerAdapter.Pager2ViewHolder>() {
    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemimage = itemView.findViewById<ImageView>(R.id.ivCarousel)

        /*init {
            itemimage.setOnClickListener { v:View ->
                val position = adapterPosition
                Toast.makeText(itemView.context, "You click image", Toast.LENGTH_SHORT).show()
            }
        }*/
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Pager2ViewHolder {
        return Pager2ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_carousel, parent, false))
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ViewPagerAdapter.Pager2ViewHolder, position: Int) {
        images[position].let {
            val imgUri = it.toUri().buildUpon().scheme("https").build()
            GlideApp.with(holder.itemimage.context)
                .load(imgUri)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image))
                .into(holder.itemimage)
        }
        /*holder.itemimage.setImageResource(images[position])*/
    }
}