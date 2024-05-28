package mx.suma.drivers.mapas

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.launch
import mx.suma.drivers.BuildConfig
import mx.suma.drivers.R
import mx.suma.drivers.databinding.ActivityMapsBinding
import mx.suma.drivers.models.api.utils.DirectionResponses
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.repositories.mapas.MapasRepository
import mx.suma.drivers.repositories.mapas.MapasRepositoryImpl
import mx.suma.drivers.repositories.mapas.remote.MapasRemoteDataSourceImpl
import mx.suma.drivers.utils.*
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.core.logger.KOIN_TAG
import retrofit2.Response
import timber.log.Timber


class MapsActivity : Fragment(), OnMapReadyCallback {
//    val args: MapsActivityArgs by navArgs()
    private lateinit var args: MapsActivityArgs

    private val apiSuma: ApiSuma by inject()
    private lateinit var mMap: GoogleMap
    private lateinit var googleMapsFragment: SupportMapFragment
    private lateinit var repository: MapasRepository
    private lateinit var viewModel: MapsViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMapsBinding
    private var destinations: String = ""
    private var source: String = ""
    private var viewAlert: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args = MapsActivityArgs.fromBundle(requireArguments())

        val remoteDataSource = MapasRemoteDataSourceImpl(apiSuma)
        repository = MapasRepositoryImpl(remoteDataSource)

        val factory = MapsViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(MapsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= DataBindingUtil.inflate(inflater, R.layout.activity_maps, container, false)

        if(args.idMapa < 1 && args.idRuta < 1) {
            Toast.makeText(
                binding.root.context, "Esta ruta no cuenta con mapa asignado", Toast.LENGTH_SHORT).show()
        } else {

            googleMapsFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            googleMapsFragment.getMapAsync(this)


            binding.viewModel = viewModel
            binding.lifecycleOwner =viewLifecycleOwner

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(binding.root.context)

        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Ver Mapa")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, MapsActivity::class.java.toString())
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        try {
            val success: Boolean = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(binding.root.context, R.raw.road_map)
            )
            if(!success) {
                Timber.tag(KOIN_TAG).e("Style parsing failed.");
            }
        }catch (e: Resources.NotFoundException) {
            Timber.tag(KOIN_TAG).e(e, "Can't find style. Error: ");
        }
        when(args.idRuta) {
            18L, 1400L -> {
                viewModel.downloadDestination(args.idBitacora) {
                    Toast.makeText(binding.root.context, "No se encontraron resultados", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                viewModel.downloadMap(args.idMapa)
            }
        }
        setupObservers()
    }

    private fun drawPolyline(response: Response<DirectionResponses>) {
        val shape = response.body()?.routes?.get(0)?.overviewPolyline?.points
        val polyline = PolylineOptions()
            .addAll(PolyUtil.decode(shape))
            .width(8f)
            .color(Color.rgb(251,140,0))
        mMap.addPolyline(polyline)
    }

    private fun alertMessageLimitStop() {
        MaterialAlertDialogBuilder(binding.root.context)
            .setIcon(ContextCompat.getDrawable(binding.root.context,R.drawable.report_problem_orange))
            .setTitle(resources.getString(R.string.title_alert_size_stop))
            .setMessage(resources.getString(R.string.message_alert_size_stop))
            .setNeutralButton("cancelar") { dialog, which -> dialog.cancel() }
            .setPositiveButton("Ir") { dialog, witch ->
                openGoogleMapStops()
                dialog.dismiss()
            }
            .show()
    }

    @SuppressLint("MissingPermission")
    private fun openGoogleMapStops() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                try {
                    if(location != null){
                        viewModel.mapa.observe(this) {
                            source = "${ location?.latitude },${ location?.longitude }/"
                            val pathMap = "https://www.google.com/maps/dir/$source$destinations"
                            val uri = Uri.parse(pathMap)
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            intent.setPackage("com.google.android.apps.maps")
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                    }else {
                        Toast.makeText(binding.root.context, "No se logro obtener la ubicación", Toast.LENGTH_SHORT).show()
                    }
                }catch (e:Exception) {
                    Timber.i(e)
                }
            }
    }

    @SuppressLint("MissingPermission")
    private fun setupObservers() {
        val button = binding.runRouter
        button.setOnClickListener {
            if(viewAlert) {
                alertMessageLimitStop()
            }else {
                openGoogleMapStops()
            }
        }

        viewModel.runMap.observe(this) { isRun ->
            if(isRun) {
                if(viewModel.destino.value !== null) {
                    fusedLocationClient.lastLocation.addOnSuccessListener {location: Location ->
                        try {
                            if(location != null) {
                                viewModel.uiScope.launch {
                                    viewModel.destino.value?.let { destino:LatLng ->
                                        try {
                                            var origen = LatLng(
                                                location.latitude,
                                                location.longitude
                                            )
                                            destinations = "${destino.latitude},${destino.longitude}"
                                            when(args.idRuta) {
                                                18L -> {
                                                    mMap.addMarker(MarkerOptions().position(destino).title("Destino").icon(
                                                        BitmapFromVector(binding.root.context, R.drawable.office_24dp)
                                                    ))
                                                }
                                                1400L -> {
                                                    mMap.addMarker(MarkerOptions().position(destino).title("Destino").icon(
                                                        BitmapFromVector(binding.root.context, R.drawable.car_repair_24dp)
                                                    ))
                                                }
                                            }
                                            mMap.addMarker(MarkerOptions().position(origen).title("Origen").icon(
                                                BitmapFromVector(binding.root.context, R.drawable.location_green_32)
                                            ))
                                            try {
                                                viewModel.uiScope.launch {
                                                    val result = viewModel.getDirectionsMap(
                                                        "${getString(R.string.base_url)}/maps/api/directions/json",
                                                        "${location.latitude},${location.longitude}",
                                                        destinations,
                                                        getString(R.string.google_maps_key)
                                                    )

                                                    drawPolyline(result)
                                                }
                                            }catch (e:Exception) {
                                                println(e)
                                            }

                                            val updateCamera = CameraUpdateFactory.newLatLngBounds(
                                                LatLngBounds(destino, origen), 100)
                                            mMap.animateCamera(updateCamera)
                                        }catch (e:Exception) {
                                            Timber.i(e)
                                        }
                                    }
                                }
                            }
                        }catch (e:Exception) {
                            Timber.i(e)
                        }
                    }
                } else if(viewModel.mapa.value !== null){
                    viewModel.mapa.value?.let { itMapa ->
                        try {
                            if (itMapa.data.id != -1L) {
                                Timber.d("Mostrar mapa")

                                val trazo = JSONObject(itMapa.trazo)

                                val features = getFeatures(trazo)
                                val listobj = getPointFeatures(features)
                                val newList = orderCoord(listobj)
                                val coords = getCoord(newList)

                                // 25
                                if(coords.size > 24){
                                    destinations = coords.slice(0..23).joinToString(separator="/") { "${it.latitude},${it.longitude}" }
                                    viewAlert = true
                                }else {
                                    destinations = coords.joinToString(separator="/") { "${it.latitude},${it.longitude}" }
                                    viewAlert = false
                                }

                                val lineFeatures = getLineStringFeatures(features)

                                addMarkersToMap(mMap, getPointFeatures(features))

                                addPolylineToMap(mMap, polyline(lineFeatures[0]))

                                val updateCamera = CameraUpdateFactory.newLatLngBounds(
                                    featureBounds(positions(lineFeatures[0])).build(), 1
                                )

                                mMap.animateCamera(updateCamera)
                            }
                        }catch (e:Exception) {
                            Timber.i(e)
                        }
                    }
                }
            }
        }
    }
}
