package mx.suma.drivers.media.audio

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import mx.suma.drivers.R
import mx.suma.drivers.databinding.RecordAudioFragmentBinding
import timber.log.Timber
import java.io.File
import java.io.IOException


class RecordAudioFragment : Fragment() {

    private lateinit var viewModel: RecordAudioViewModel

    private lateinit var modulo: String
    private var id: Long = -1

    private var fileName: String = ""
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    var mStartRecording = true
    var mStartPlaying = true

    private lateinit var outputDirectory: File

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding: RecordAudioFragmentBinding = DataBindingUtil
            .inflate(inflater, R.layout.record_audio_fragment, container, false)

        viewModel = ViewModelProvider(this).get(RecordAudioViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        modulo = RecordAudioFragmentArgs.fromBundle(requireArguments()).modulo
        id = RecordAudioFragmentArgs.fromBundle(requireArguments()).id

        outputDirectory = getOutputDirectory()
        fileName = getFileName()

        setupListeners(binding)
        setupObservers()

        return binding.root
    }

    private fun getFileName(): String {
        return "${outputDirectory.absolutePath}/%s_%s_AUDIO.mp4".format(modulo, id)
    }

    fun getOutputDirectory(): File {
        val mediaDir = requireActivity().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }

        return if (mediaDir != null && mediaDir.exists())
            mediaDir else requireActivity().filesDir
    }

    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            try {
                prepare()
            } catch (e: IOException) {
                Timber.e("prepare() failed")
            }

            start()
        }
    }

    private fun stopRecording() {
        Timber.d(fileName)

        recorder?.apply {
            stop()
            release()
        }

        recorder = null
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
                start()
            } catch (e: IOException) {
                Timber.e("prepare() failed")
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    private fun onRecord(start: Boolean) = if (start) {
        startRecording()
    } else {
        stopRecording()
    }

    private fun onPlay(start: Boolean) = if (start) {
        startPlaying()
    } else {
        stopPlaying()
    }

    private fun setupListeners(binding: RecordAudioFragmentBinding) {
         binding.btnGrabar.setOnClickListener {
            onRecord(mStartRecording)

            binding.btnGrabar.text = when (mStartRecording) {
                true -> "Grabando"
                false -> "Grabar"
            }

            when(mStartRecording) {
                true -> binding.ivRecordLed.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_mic))
                false -> binding.ivRecordLed.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_mic_none))
            }

            mStartRecording = !mStartRecording
        }

        binding.btnReproducir.setOnClickListener {
            onPlay(mStartPlaying)

            binding.btnReproducir.text = when(mStartPlaying) {
                true -> "Reproduciendo"
                false -> "Reproducir"
            }

            mStartPlaying = !mStartPlaying
        }
    }

    private fun setupObservers() {
        viewModel.navigateToCapturaSanitizacion.observe(viewLifecycleOwner, {
            if(it) {
                this.findNavController().navigate(
                    R.id.action_recordAudioFragment_to_listadoSanitizaciones
                )

                viewModel.onNavigationComplete()
            }
        })
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
        player?.release()
        player = null
    }
}