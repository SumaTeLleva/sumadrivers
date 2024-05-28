package mx.suma.drivers.gasolinera

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.launch
import mx.suma.drivers.BuildConfig
import mx.suma.drivers.R
import mx.suma.drivers.database.SumaDriversDatabase
import mx.suma.drivers.databinding.ActivityMapsGasBinding
import mx.suma.drivers.models.api.EstacionGas
import mx.suma.drivers.models.api.utils.DirectionResponses
import mx.suma.drivers.models.db.LocationGasStation
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.repositories.mapas.MapasRepository
import mx.suma.drivers.repositories.mapas.MapasRepositoryImpl
import mx.suma.drivers.repositories.mapas.remote.MapasRemoteDataSourceImpl
import mx.suma.drivers.repositories.proveedores.ProveedoresRepository
import mx.suma.drivers.repositories.proveedores.ProveedoresRepositoryImpl
import mx.suma.drivers.repositories.proveedores.remote.ProveedoresRemoteDataSourceImpl
import mx.suma.drivers.session.AppStateImpl
import mx.suma.drivers.utils.BitmapFromVector
import mx.suma.drivers.utils.TypeError
import org.koin.android.ext.android.inject
import org.koin.core.logger.KOIN_TAG
import retrofit2.Response
import timber.log.Timber

class MapsGasActivity : Fragment(), OnMapReadyCallback {
    private val apiSuma: ApiSuma by inject()
    private val appState: AppStateImpl by inject()
    private val database: SumaDriversDatabase by inject()
    private lateinit var repository: ProveedoresRepository
    private lateinit var repositoryMap: MapasRepository
    private lateinit var viewModel: MapGasViewModel
    private lateinit var mapFragment: SupportMapFragment

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMapsGasBinding
    private lateinit var recycleView: RecyclerView
    private lateinit var viewItemAdapter: ViewItemAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var btnViewList: MaterialButton
    private lateinit var btnStartRoute: MaterialButton
    private lateinit var lottieError: LottieAnimationView
    private var poliLineGlobal: Polyline? = null
    private var locationCallback: LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val proveedorDao = database.proveedorDao
        val remoteDataSource = ProveedoresRemoteDataSourceImpl(apiSuma)
        repository = ProveedoresRepositoryImpl(appState,remoteDataSource, proveedorDao)

        val remoteDataSourceMap = MapasRemoteDataSourceImpl(apiSuma)
        repositoryMap = MapasRepositoryImpl(remoteDataSourceMap)

        val factory = MapGasViewModelFactory(repository, repositoryMap)
        viewModel = ViewModelProvider(this, factory).get(MapGasViewModel::class.java)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding= DataBindingUtil.inflate(inflater, R.layout.activity_maps_gas, container, false)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = childFragmentManager.findFragmentById(R.id.map_gas) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.viewModel = viewModel
        binding.lifecycleOwner =viewLifecycleOwner

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(binding.root.context)


        btnStartRoute = binding.btnViewRun
        val bottonSheet = binding.gasBottomSheet
        btnViewList = binding.btnViewList
        lottieError = binding.animationViewError

        bottomSheetBehavior = BottomSheetBehavior.from(bottonSheet)
        onHiddenViewList()
        btnViewList.visibility = View.INVISIBLE
        setupObservers()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Mapa de gasolineras")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, MapsGasActivity::class.java.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("ejecuto la funcion onDestroy")
        if(locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback!!)
        }
    }

    @SuppressLint("MissingPermission")
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
        mMap.isMyLocationEnabled = true

    }

    private fun setupObservers() {

        viewModel.listaDeEstacion.observe(viewLifecycleOwner) { gasStations ->
            if(gasStations.data.isNotEmpty()) {
                renderMarksEstaciones(binding.root.context, mMap, gasStations.data)
                loadGastationsList(gasStations)
            }else {
                btnViewList.visibility = View.INVISIBLE
                onHiddenViewList()
            }
        }

        viewModel.locations.observe(viewLifecycleOwner){
            if(it != null) {
                btnStartRoute.visibility = View.VISIBLE
                btnStartRoute.isClickable = true
            }else{
                btnStartRoute.visibility = View.INVISIBLE
                btnStartRoute.isClickable = false
            }
        }
        viewModel.error.observe(viewLifecycleOwner) {
            when(it) {
                TypeError.SERVICE -> {
                    viewProblem(it)
                    sendMessageError("Gasolineras en mantenimiento, intenete más tarde")
                }
                TypeError.NETWORK -> {
                    viewProblem(it)
                    sendMessageError("Servicio suspendido temporalmente")
                }
                TypeError.OTHER -> {
                    sendMessageError("Ocurrio un problema en el proceso")
                }
                else -> {}
            }
        }
    }

    private fun viewProblem(type: TypeError) {
        mapFragment.view?.visibility = View.GONE
        when(type) {
            TypeError.SERVICE ->{
                lottieError.setAnimation(R.raw.animation_mantenance)
            }
            TypeError.NETWORK -> {
                lottieError.setAnimation(R.raw.animation_notfound)
            }
            else -> {
                TODO("no hace nada")
            }
        }
        lottieError.visibility = View.VISIBLE
    }

    private fun onViewList() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun onHiddenViewList() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    @SuppressLint("MissingPermission")
    private fun loadGastationsList(listaEstaciones: EstacionGas) {
        btnViewList.visibility = View.VISIBLE
        btnViewList.setOnClickListener {
            onViewList()
        }
        btnStartRoute.setOnClickListener {
            viewModel.locations.value.let {
                val source = "${ it?.pointA?.latitude },${ it?.pointA?.longitude }/"
                val destinations = "${ it?.pointB?.latitude },${ it?.pointB?.longitude }/"
                val pathMap = "https://www.google.com/maps/dir/$source$destinations"
                val uri = Uri.parse(pathMap)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        onHiddenViewList()

        recycleView = binding.rvHorizontal
        recycleView.setHasFixedSize(true)
        recycleView.layoutManager = LinearLayoutManager(binding.root.context, RecyclerView.VERTICAL, false)
        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recycleView)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            try {
                if(location != null){
                    val position = LatLng(location.latitude, location.longitude)
                    viewItemAdapter = ViewItemAdapter(
                        fusedLocationClient,
                        listaEstaciones.sortByDistance(position)){ actual, destino ->
                        viewModel.locations.value = LocationGasStation(actual, destino)
                        val destinations = "${destino.latitude},${destino.longitude}"
                        val source = "${actual.latitude},${actual.longitude}"
                        viewModel.uiScope.launch {
                            try {
                                val result = viewModel.getDirectionsMap("${getString(R.string.base_url)}/maps/api/directions/json", source,destinations, getString(R.string.google_maps_key))
                                if(result.code() == 200 && result.body()?.status != "REQUEST_DENIED") {
                                    onHiddenViewList()
                                    drawPolyline(mMap, result)
                                }else {
                                    sendMessageError(result.body()?.error_message!!)
                                }
                            }catch (e:Exception) {
                                Timber.e(e)
                            }
                        }
                    }
                    recycleView.adapter = viewItemAdapter

                    val currenPosition = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currenPosition, 15f))
                }else {
                    Toast.makeText(binding.root.context, "No se logro obtener la ubicación", Toast.LENGTH_SHORT).show()
                }

            }catch (e:Exception) {
                Timber.i(e)
            }
        }

        onLocationCurren(listaEstaciones)
    }

    @SuppressLint("MissingPermission")
    private fun onLocationCurren(listaEstaciones:EstacionGas) {

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30000L).apply {
            setMinUpdateIntervalMillis(30000)
            setMaxUpdateDelayMillis(30000)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()

        locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                try {
                    for (location in locationResult.locations) {
                        if(location != null){
                            val position = LatLng(location.latitude, location.longitude)
                            viewItemAdapter = ViewItemAdapter(
                                fusedLocationClient,
                                listaEstaciones.sortByDistance(position)){ actual, destino ->
                                viewModel.locations.value = LocationGasStation(actual, destino)
                                val destinations = "${destino.latitude},${destino.longitude}"
                                val source = "${actual.latitude},${actual.longitude}"
                                viewModel.uiScope.launch {
                                    try {
                                        val result = viewModel.getDirectionsMap("${getString(R.string.base_url)}/maps/api/directions/json", source,destinations, getString(R.string.google_maps_key))
                                        if(result.code() == 200 && result.body()?.status != "REQUEST_DENIED") {
                                            onHiddenViewList()
                                            drawPolyline(mMap, result)
                                        }else {
                                            sendMessageError(result.body()?.error_message!!)
                                        }
                                    }catch (e:Exception) {
                                        Timber.e(e)
                                    }
                                }
                            }
                            recycleView.adapter = viewItemAdapter
                        }
                    }
                } catch (e:Exception) {
                    Timber.i(e)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, Looper.getMainLooper())
    }

    @SuppressLint("MissingPermission", "PotentialBehaviorOverride")
    private fun renderMarksEstaciones(cx:Context, map: GoogleMap, lista:ArrayList<EstacionGas.Estacion>) {
        lista.forEach {
            val position = LatLng(it.LATITUD.toDouble(), it.LONGITUD.toDouble())
            if(it.ACTIVO == 1) {
                map.addMarker(MarkerOptions().position(position)
                    .title(it.NOMBRE).icon(BitmapFromVector(cx, R.drawable.gas_station_point_enable)))
            }else {
                map.addMarker(MarkerOptions().position(position)
                    .title(it.NOMBRE).icon(BitmapFromVector(cx, R.drawable.gas_station_point_disabled)))
            }
        }

        map.setOnMarkerClickListener { marker ->
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                try {
                    if(location != null){
                        val position = LatLng(location.latitude, location.longitude)
                        viewModel.locations.value = LocationGasStation(position, marker.position)
                        val destinations = "${marker.position.latitude},${marker.position.longitude}"
                        val source = "${position.latitude},${position.longitude}"
                        viewModel.uiScope.launch {
                            try {
                                val result = viewModel.getDirectionsMap("${getString(R.string.base_url)}/maps/api/directions/json", source,destinations, getString(R.string.google_maps_key))
                                if(result.code() == 200 && result.body()?.status != "REQUEST_DENIED") {
                                    onHiddenViewList()
                                    drawPolyline(mMap, result)
                                }else {
                                    sendMessageError(result.body()?.error_message!!)
                                }
                            }catch (e:Exception) {
                                Timber.e(e)
                            }
                        }
                    }else {
                        Toast.makeText(binding.root.context, "No se logro obtener la ubicación", Toast.LENGTH_SHORT).show()
                    }
                }catch (e:Exception) {
                    Timber.i(e)
                }
            }
            false
        }
    }

    private fun drawPolyline(map: GoogleMap,response: Response<DirectionResponses>) {
        try {
            if(poliLineGlobal != null) {
                poliLineGlobal?.remove()
            }
            val shape = response.body()?.routes?.get(0)?.overviewPolyline?.points
            val polyline = PolylineOptions()
                .addAll(PolyUtil.decode(shape))
                .width(8f)
                .color(Color.rgb(251,140,0))
            poliLineGlobal = map.addPolyline(polyline)

            val southwest = LatLng(
                response.body()?.routes?.get(0)?.bounds?.southwest?.lat!!,
                response.body()?.routes?.get(0)?.bounds?.southwest?.lng!!
            )

            val northeast = LatLng(
                response.body()?.routes?.get(0)?.bounds?.northeast?.lat!!,
                response.body()?.routes?.get(0)?.bounds?.northeast?.lng!!
            )

            val updateCamera = CameraUpdateFactory.newLatLngBounds(
                LatLngBounds(
                    southwest,
                    northeast
                ), 200)
            map.moveCamera(updateCamera)
        }catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun sendMessageError(message:String) {
        Toast.makeText(binding.root.context, message,Toast.LENGTH_SHORT).show()
    }
}