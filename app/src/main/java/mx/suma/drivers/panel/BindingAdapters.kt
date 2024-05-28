package mx.suma.drivers.panel

import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.bumptech.glide.request.RequestOptions
import mx.suma.drivers.GlideApp
import mx.suma.drivers.R

@BindingAdapter("fotografiaOperador")
fun bindImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        val imgUri = it.toUri().buildUpon().scheme("https").build()

        GlideApp.with(imgView.context)
            .load(imgUri)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_broken_image))
            .into(imgView)
    }
}