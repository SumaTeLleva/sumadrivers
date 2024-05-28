package mx.suma.drivers.encuestaCovid

import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import mx.suma.drivers.R

@BindingAdapter("preguntaImage")
fun setPreguntaImage(imageView: ImageView, imageName: String?) {
    val images = HashMap<String, Int>()
    images["encuesta_covid_q01"] = R.drawable.ic_encuesta_covid_q01
    images["encuesta_covid_q02"] = R.drawable.ic_encuesta_covid_q02
    images["encuesta_covid_q03"] = R.drawable.ic_encuesta_covid_q03
    images["encuesta_covid_q04"] = R.drawable.ic_encuesta_covid_q04
    images["encuesta_covid_q05"] = R.drawable.ic_encuesta_covid_q05
    images["encuesta_covid_q06"] = R.drawable.ic_encuesta_covid_q06
    images["encuesta_covid_q07"] = R.drawable.ic_encuesta_covid_q07
    images["encuesta_covid_q08"] = R.drawable.ic_encuesta_covid_q08
    images["encuesta_covid_q09"] = R.drawable.ic_encuesta_covid_q09
    images["encuesta_covid_q10"] = R.drawable.ic_encuesta_covid_q10
    images["encuesta_covid_q11"] = R.drawable.ic_encuesta_covid_q11
    images["Invalid"] = R.drawable.logo_suma

    imageName?.let {
        imageView.setImageDrawable(ContextCompat.getDrawable(imageView.context, images[imageName] as Int))
    }
}

@BindingAdapter("preguntaContestada")
fun setPreguntaContestada(imageView: ImageView, respuesta: Int?) {
    respuesta?.let {
        when(it) {
            1 -> {
                imageView.setColorFilter(imageView.context.getColor(R.color.blue))
                imageView.visibility = View.VISIBLE
            }
            0 -> {
                imageView.setColorFilter(imageView.context.getColor(R.color.md_red))
                imageView.visibility = View.VISIBLE
            }
            else -> imageView.visibility = View.GONE
        }
    }
}