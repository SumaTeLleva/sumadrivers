package mx.suma.drivers.appstarter

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import mx.suma.drivers.MainActivity
import mx.suma.drivers.R
import mx.suma.drivers.databinding.StarterAppFragmentBinding
import mx.suma.drivers.session.AppStateImpl
import org.koin.android.ext.android.inject
import timber.log.Timber

class StarterAppFragment : Fragment(), MultiplePermissionsListener {

    private lateinit var viewModel: StarterAppViewModel
    private val appState: AppStateImpl by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as MainActivity).supportActionBar?.hide();
        Timber.i("onCreateView Fragment")
        val binding: StarterAppFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.starter_app_fragment, container, false)

        val viewModelFactory = StarterAppViewModelFactory(appState)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(StarterAppViewModel::class.java)

        Timber.i("Autenticado: ${appState.autenticado()}")

        Timber.i("version de android ${Build.VERSION.SDK_INT}")
        var permissionList = when(Build.VERSION.SDK_INT) {
            34 -> listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE
            )
            33 -> listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.FOREGROUND_SERVICE,
            )
            31,32 -> listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.FOREGROUND_SERVICE,
//                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            )
            in 0..30 -> listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.FOREGROUND_SERVICE,
            )
            else -> listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.BLUETOOTH,
            )
        }

//        permissionLaucherMultiple.launch(permissionList)


        Dexter.withContext(activity)
            .withPermissions(permissionList)
            .withListener(this)
            .check()

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupObservers()

        return binding.root
    }

    private val permissionLaucherMultiple = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){ result ->

        var areAllGranted = true
        for (isGranted in result.values){
            Timber.i("permissionLaucherMultiple: isGranted: $isGranted")
            areAllGranted = areAllGranted && isGranted
        }

        if(areAllGranted){
            Timber.i("Check play services")
            isGooglePlayServicesAvailable()
        } else {
            Timber.i("permissionLaucherMultiple: todos o algunos permisos denegados")
            Toast.makeText(activity, "All o algunos permisos denegados", Toast.LENGTH_SHORT).show();
            viewModel.onPermissionsDenied()
//            viewModel.onMissingPermissions()
        }
    }
    private fun setupObservers() {
        viewModel.finishApp.observe(viewLifecycleOwner) {
            if (it) {
                this.activity?.finish()
            }
        }

        viewModel.navigateToLogin.observe(viewLifecycleOwner) {
            if (it) {
                try {
                    this.findNavController().navigate(
                        R.id.action_starterAppFragment_to_loginFragment
                    )

                    viewModel.onNavigationComplete()
                }
                catch(e:Exception) {
                    Timber.e("ocurrio un error: ${e.message}")
                }
            }
        }

        viewModel.navigateToPanel.observe(viewLifecycleOwner) {
            if (it) {
                this.findNavController().navigate(
                    R.id.action_starterAppFragment_to_panelFragment
                )

                viewModel.onNavigationComplete()
            }
        }

        viewModel.openAppSettings.observe(viewLifecycleOwner) {
            if (it) {
                Intent(
                    ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:${requireActivity().packageName}")
                ).apply {
                    addCategory(Intent.CATEGORY_DEFAULT)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(this)
                }

                this.activity?.finish()
            }
        }
    }

    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
        Timber.i("Permissions has been checked")

        viewModel.onHideProgressBar()
        report?.deniedPermissionResponses?.forEach {
            println(it.permissionName)
        }
        report?.let {
            when {
                it.isAnyPermissionPermanentlyDenied -> {
                    viewModel.onPermissionsDenied()
                }
                it.deniedPermissionResponses.isNotEmpty() -> {
                    viewModel.onMissingPermissions()
                }
                else -> {
                    Timber.i("Check play services")
                    isGooglePlayServicesAvailable()
                }
            }
        }
    }

    override fun onPermissionRationaleShouldBeShown(
        permissions: MutableList<PermissionRequest>?,
        token: PermissionToken?
    ) {
        Timber.d("onPermissionRationaleShouldBeShown")
        token?.continuePermissionRequest()
    }

    private fun isGooglePlayServicesAvailable() {
        val getGoogleapiAvailability = GoogleApiAvailability.getInstance()
        val code = getGoogleapiAvailability.isGooglePlayServicesAvailable(requireContext())

        if (code != ConnectionResult.SUCCESS) {
            if (getGoogleapiAvailability.isUserResolvableError(code)) {
                getGoogleapiAvailability.getErrorDialog(requireActivity(), code, 9000)?.show()
            } else {
                Timber.i("Google Play Services no está disponible, dispositivo no compatible")
                viewModel.onPlayServicesUnavailable()
            }
        } else {
            Timber.i("Play Services OK")
            viewModel.isConnected()
        }
    }
}
