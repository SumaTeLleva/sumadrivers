package mx.suma.drivers

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.armcha.debugBanner.Banner
import io.armcha.debugBanner.BannerGravity
import io.armcha.debugBanner.BannerView
import mx.suma.drivers.database.SumaDriversDatabase
import mx.suma.drivers.database.UsuarioDao
import mx.suma.drivers.session.AppStateImpl
import mx.suma.drivers.utils.configuration
import mx.suma.drivers.work.ActualizarDispositivoWorker
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), BannerView {

    private val appState: AppStateImpl by inject()
    private val database: SumaDriversDatabase by inject()

    object variabledeinicio {
        var anuncios = true
    }

    private lateinit var usuarioDao: UsuarioDao

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    lateinit var myApplication: SumaDriversApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myApplication = (application as SumaDriversApp)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.main_toolbar))
        //supportActionBar?.hide();
        navHostFragment = supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        usuarioDao = database.usuarioDao

        findViewById<Toolbar>(R.id.main_toolbar)
            .setupWithNavController(navController, appBarConfiguration)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        supportActionBar?.hide()
        appState.settingsName()
        val pathUrl = appState.getPathServer()
        if(pathUrl.isNotEmpty()) {
            configuration.baseURL = pathUrl
        }
        myApplication.loadNetworkModule()
        appState.setIsProducction(configuration.isProduction)
        setupRecurringWork()
    }

    override fun createBanner(): Banner {
        return Banner(
            bannerText = "T-API",
            bannerColorRes = R.color.md_red,
            textColorRes = android.R.color.white,
            bannerGravity = BannerGravity.END
        )
    }

    fun reiniciarInstancia () {
        val intent = Intent(this@MainActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        finishAffinity()
        startActivity(intent)
        finish()
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                try {
                    if(location != null) {
                        Timber.i("Got location data")

                        location?.apply {
                            Timber.i("Location -> Latitude: ${this.latitude} Longitude: ${this.longitude}")
                            appState.setLastKnownLocation(
                                this.latitude,
                                this.longitude,
                                this.time.toDouble()
                            )
                        }
                    }
                    else{
                        Toast.makeText(this, "No se logro obtener la ubicación", Toast.LENGTH_SHORT).show()
                    }

                }catch (e:Exception) {
                    Timber.i(e)
                }
            }
    }

    private fun setupRecurringWork() {
        val repeatingRequest =
            PeriodicWorkRequestBuilder<ActualizarDispositivoWorker>(1, TimeUnit.HOURS)
                .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            ActualizarDispositivoWorker.WORKER_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            repeatingRequest
        )
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return SumaDriversViewModelFactory(usuarioDao)
    }
}
