package mx.suma.drivers.carousel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import mx.suma.drivers.R
import mx.suma.drivers.databinding.CarouselFragmentBinding
import mx.suma.drivers.session.AppState
import mx.suma.drivers.session.AppStateImpl
import org.koin.android.ext.android.inject

class CarouselFragment: Fragment() {
    private lateinit var viewPager: ViewPager2
    private val appState: AppStateImpl by inject()
    private lateinit var binding: CarouselFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.carousel_fragment, container, false)
    }
}