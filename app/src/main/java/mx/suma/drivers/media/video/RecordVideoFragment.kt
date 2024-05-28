package mx.suma.drivers.media.video

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.VideoCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import mx.suma.drivers.R
import mx.suma.drivers.databinding.RecordVideoFragmentBinding
import timber.log.Timber
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SuppressLint("RestrictedApi", "UnsafeExperimentalUsageError")
class RecordVideoFragment : Fragment() {

    private lateinit var viewModel: RecordVideoViewModel

    private lateinit var modulo: String
    private var id: Long = -1

    private var preview: Preview? = null
    private var camera: Camera? = null

    private lateinit var videoCapture: VideoCapture

    private lateinit var binding: RecordVideoFragmentBinding

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = RecordVideoFragmentBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(RecordVideoViewModel::class.java)
        // TODO: Use the ViewModel

        modulo = RecordVideoFragmentArgs.fromBundle(requireArguments()).modulo
        id = RecordVideoFragmentArgs.fromBundle(requireArguments()).id
        outputDirectory = getOutputDirectory()

        startCamera()
        setupListeners()

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun setupListeners() {
        val fileName = "%s_%s_VIDEO.mp4".format(modulo, id)
        File(outputDirectory, fileName)

        val builder = AlertDialog.Builder(requireContext())

        builder.setMessage("Presiona el boton Finalizar para terminar la grabación o Volver para reintentarlo")
            .setPositiveButton("Finalizar") { _, _ ->
                this.findNavController().navigate(
                    R.id.action_recordVideoFragment_to_listadoSanitizaciones
                )
            }.setNegativeButton("Volver") {_, _ ->
                Timber.d("Se reintenta la grbación")
            }

        binding.btnCapture.setOnTouchListener(fun(_: View, event: MotionEvent): Boolean {
//            if (event.action == MotionEvent.ACTION_DOWN) {
//                binding.btnCapture.setImageDrawable(
//                    ContextCompat.getDrawable(
//                        requireContext(),
//                        R.drawable.ic_lens
//                    )
//                )
//                binding.ivVideoCamera.setImageDrawable(
//                    ContextCompat.getDrawable(
//                        requireContext(),
//                        R.drawable.ic_videocam_red
//                    )
//                )
//
//                val outputFileOptions = VideoCapture.OutputFileOptions.Builder(file).build()
//
//
//                videoCapture.startRecording(
//                    outputFileOptions,
//                    ContextCompat.getMainExecutor(requireContext()),
//                    object : VideoCapture.OnVideoSavedCallback {
//                        override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
//                            Timber.d("on video saved")
//                        }
//
//                        override fun onError(
//                            videoCaptureError: Int,
//                            message: String,
//                            cause: Throwable?
//                        ) {
//                            Timber.i("Video Error: $message")
//                        }
//
//                    })
//            } else if (event.action == MotionEvent.ACTION_UP) {
//                binding.btnCapture.setImageDrawable(
//                    ContextCompat.getDrawable(
//                        requireContext(),
//                        R.drawable.ic_panorama_fish_eye
//                    )
//                )
//                binding.ivVideoCamera.setImageDrawable(
//                    ContextCompat.getDrawable(
//                        requireContext(),
//                        R.drawable.ic_videocam_white
//                    )
//                )
//
//                videoCapture.stopRecording()
//
//                Timber.i("Video File stopped")
//
//                builder.create().show()
//            }

            return false
        })
    }

/*    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create timestamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Setup image capture listener which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Timber.e("Photo capture failed: ${exc.message}")
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    Timber.d(msg)
                }
            })
    }*/

    fun getOutputDirectory(): File {
        val mediaDir = requireActivity().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else requireActivity().filesDir
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            preview = Preview.Builder().build()

            //imageCapture = ImageCapture.Builder().build()

            val videoSize = Size(352, 640)

            videoCapture = VideoCapture.Builder()
                .setTargetResolution(videoSize)
                .setMaxResolution(videoSize)
                .build()

            // Select back camera
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, videoCapture)
                preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            } catch(exc: Exception) {
                Timber.e("Use case binding failed")
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }
}