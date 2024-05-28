package mx.suma.drivers.miUnidad

import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import mx.suma.drivers.R

@BindingAdapter("fotografiaUnidad")
fun bindFotografiaUnidad(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        try {
            val imgUri = it.toUri().buildUpon().scheme("https").build()

            Glide.with(imgView.context)
                .load(imgUri)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image))
                .into(imgView)
        } catch (e: Exception) {
            println(e)
        }
    }
}