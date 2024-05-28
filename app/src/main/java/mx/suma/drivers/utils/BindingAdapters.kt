package mx.suma.drivers.utils

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("goneUnless")
fun goneUnless(view: View, visible: Boolean) {
    view.visibility = if(visible) View.VISIBLE else View.GONE
}

@BindingAdapter("notGoneUnless")
fun notGoneUnless(view: View, value: Boolean) {
    view.visibility = if(value) View.GONE else View.VISIBLE
}

@BindingAdapter("goneUnlessStatusLoading")
fun goneUnlessStatusLoading(view: View, estatusLCE: EstatusLCE) {
    view.visibility = if(estatusLCE == EstatusLCE.LOADING) View.VISIBLE else View.GONE
}

@BindingAdapter("goneUnlessStatusContent")
fun goneUnlessStatusContent(view: View, estatusLCE: EstatusLCE) {
    view.visibility = if(estatusLCE == EstatusLCE.CONTENT) View.VISIBLE else View.GONE
}

@BindingAdapter("goneUnlessStatusNoContent")
fun goneUnlessStatusNoContent(view: View, estatusLCE: EstatusLCE) {
    view.visibility = if(estatusLCE == EstatusLCE.NO_CONTENT) View.VISIBLE else View.GONE
}

@BindingAdapter("goneUnlessStatusError")
fun goneUnlessStatusError(view: View, estatusLCE: EstatusLCE) {
    view.visibility = if(estatusLCE == EstatusLCE.ERROR) View.VISIBLE else View.GONE
}
