package mx.suma.drivers.bitacoras.testing

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButton.ICON_GRAVITY_TEXT_START
import com.google.android.material.card.MaterialCardView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.shuhart.stepview.StepView
import kotlinx.coroutines.launch
import mx.suma.drivers.GlideApp
import mx.suma.drivers.MainActivity
import mx.suma.drivers.R
import mx.suma.drivers.SumaDriversViewModel
import mx.suma.drivers.databinding.TestBitacoraFragmentBinding
import mx.suma.drivers.models.api.Unidad
import mx.suma.drivers.models.api.encuestas.EncuestaUnidad
import mx.suma.drivers.models.api.encuestas.RespuestaUnidad
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.repositories.archivos.ArchivosRepositoryImpl
import mx.suma.drivers.repositories.archivos.remote.ArchivosRemoteDataSourceImpl
import mx.suma.drivers.repositories.encuestas.EncuestasRepositoryImpl
import mx.suma.drivers.repositories.encuestas.remote.EncuestasRemoteDataSourceImpl
import mx.suma.drivers.repositories.unidades.UnidadesRepositoryImpl
import mx.suma.drivers.repositories.unidades.remote.UnidadesRemoteDataSourceImpl
import mx.suma.drivers.session.AppStateImpl
import mx.suma.drivers.utils.TypeError
import org.koin.android.ext.android.inject
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.UUID


class TestBitacoraFragment: Fragment() {
    private val apiSuma: ApiSuma by inject()
    private val appState: AppStateImpl by inject()
    private lateinit var args: TestBitacoraFragmentArgs

    private lateinit var viewModel: TestBitacoraViewModel
    private val sharedViewModel: SumaDriversViewModel by activityViewModels()
    private lateinit var binding: TestBitacoraFragmentBinding

    private lateinit var stepView: StepView
    private var currentStep:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        args = TestBitacoraFragmentArgs.fromBundle(requireArguments())
        val remoteDataSource = EncuestasRemoteDataSourceImpl(apiSuma)
        val repository = EncuestasRepositoryImpl(remoteDataSource)

        val unidadesRemoteDataSource = UnidadesRemoteDataSourceImpl(apiSuma)
        val repositoryUnidad = UnidadesRepositoryImpl(appState,unidadesRemoteDataSource)

        val archivoRemoteDataSource = ArchivosRemoteDataSourceImpl(apiSuma)
        val repositoryArchivo = ArchivosRepositoryImpl(archivoRemoteDataSource)

        val operadorId = sharedViewModel.usuario.value?.idOperadorSuma?.toInt()
        val factory = TestBitacoraViewModelFactory(
            appState,
            repository,
            repositoryUnidad,
            repositoryArchivo,
            args.unidadId, operadorId!!
        )
        viewModel = ViewModelProvider(this, factory)[TestBitacoraViewModel::class.java]
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity).supportActionBar?.hide()
        (activity as MainActivity).supportActionBar?.title=""
        binding = TestBitacoraFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupObservers()
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Test de unidad")
            param(
                FirebaseAnalytics.Param.SCREEN_CLASS,
                TestBitacoraFragment::class.java.toString()
            )
        }
    }

    fun setupObservers() {
        viewModel.error.observe(viewLifecycleOwner) {
            when(it) {
                TypeError.NETWORK -> {
                    sendMessageError("Sin conexión a internet")
                }
                TypeError.SERVICE -> {
                    sendMessageError("Ocurrio un error interno, intente más tarde")
                }
                TypeError.EMPTY -> {
                    sendMessageError("")
                }
                else -> {}
            }
        }

        viewModel.datosEncuesta.observe(viewLifecycleOwner) {
            if(it.data.isNotEmpty()){
                renderStepView(it.data)
            }
        }


        binding.btnNext.setOnClickListener {
            val preguntaCount = Integer.sum(viewModel.preguntasRequeridas.value!!, validCountElement("question_required"))
            val imagenCount = Integer.sum(viewModel.imagenesRequeridas.value!!, validCountElement("image_required"))

            if((preguntaCount == viewModel.valueResponse.value?.listRespuesta?.size) && (imagenCount == viewModel.valueRespuestaImage.value?.size)) {
                if(currentStep < (stepView.stepCount -1)) {
                    currentStep++
                    stepView.go(currentStep, true)
                    viewModel.preguntasRequeridas.value = preguntaCount
                    viewModel.imagenesRequeridas.value = imagenCount
                    if(currentStep < viewModel.datosEncuesta.value?.data?.size!!) {
                        viewModel.datosEncuesta.value?.data?.get(currentStep)
                            ?.let { it1 -> renderStepModule(it1) }
                    }else if(currentStep == viewModel.datosEncuesta.value?.data?.size!!){
                        renderSucess()
                        stepView.done(true)
                    }
                }else {
                    findNavController().popBackStack()
//                    this.findNavController().navigate(TestBitacoraFragmentDirections.actionTestingFragmentToCapturaBitacoraFragment(args.bitacoraId))
                }
            } else {
                Toast.makeText(context, "Por favor, complete las preguntas", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun sendMessageError(message:String) {
        val clMensaje: View = binding.root.findViewById(R.id.cl_mensaje_error)
        val tvMensaje: TextView = clMensaje.findViewById(R.id.tv_mensaje)
        tvMensaje.text = message
    }

    private fun renderStepView(steps: ArrayList<EncuestaUnidad.Modulo>) {
        stepView = binding.root.findViewById<StepView>(R.id.step_view)
        stepView.state
            .animationType(StepView.ANIMATION_LINE)
            .stepsNumber(steps.size + 1)
            .animationDuration(resources.getInteger(android.R.integer.config_shortAnimTime))
            .commit()

        renderStepModule(steps[currentStep])
    }

    private fun renderSucess() {
        var content = binding.root.findViewById<LinearLayout>(R.id.cl_content)
        content.removeAllViews()

        val paramsContent = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        paramsContent.setMargins(0,150,0,0)
        val leneaView = LinearLayout(content.context)
        paramsContent.gravity = Gravity.CENTER_VERTICAL
        leneaView.orientation = LinearLayout.VERTICAL
        leneaView.layoutParams = paramsContent
        leneaView.gravity = Gravity.CENTER

        val actionText = TextView(leneaView.context)
        actionText.gravity = Gravity.CENTER
        actionText.text = "Guardando encuesta..."
        actionText.textSize = 18f
        actionText.setTypeface(null ,Typeface.BOLD)
        actionText.setTextColor(ContextCompat.getColor(actionText.context, R.color.primaryColor))

        val progressView = ProgressBar(leneaView.context)
        val paramsProgress = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        paramsProgress.gravity = Gravity.CENTER
        progressView.layoutParams = paramsProgress

        val lottile = LottieAnimationView(leneaView.context)
        val paramsLottlie = LayoutParams(500, 500)
        lottile.layoutParams = paramsLottlie
        lottile.repeatCount = LottieDrawable.INFINITE
        lottile.enableMergePathsForKitKatAndAbove(true)
        lottile.setAnimation(R.raw.test_done)

        leneaView.addView(actionText)
        leneaView.addView(lottile)
        leneaView.addView(progressView)
        content.addView(leneaView)

        viewModel.uiScope.launch {
            try {
                viewModel.enviarRespuesta()
                viewModel.saveRegistrerTesting()
                actionText.text = "Se guardó correctamente"
            }catch (e: HttpException) {
                actionText.text = e.message()
            }
            progressView.visibility = View.GONE
            lottile.playAnimation()
        }

        val buttonNext = binding.btnNext
        buttonNext.text = "Cerrar encuesta"
    }
    private fun renderStepModule(module: EncuestaUnidad.Modulo) {
        var content = binding.root.findViewById<LinearLayout>(R.id.cl_content)
        content.removeAllViews()

        if(currentStep == 0) {
            content.addView(layoutInformation(content.context))
        }

        val textView = TextView(content.context)
        textView.textSize = 22f
        textView.setTypeface(null, Typeface.BOLD)
        textView.text = module.MODULO
        content.addView(textView)

        module.PREGUNTA.forEach{
            val questionView =  renderQuestion(content.context, it, 0)
            content.addView(questionView)
        }
    }

    private fun layoutInformation(cx:Context): LinearLayout {
        val paramsParent = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        paramsParent.setMargins(0,10,0,40)
        val linerParent = LinearLayout(cx)
        linerParent.layoutParams = paramsParent

        val carView = MaterialCardView(linerParent.context)
        val paramsCard = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        paramsCard.setMargins(5,5,5,5)
        carView.cardElevation = 8f
        carView.strokeWidth = 1
        carView.setStrokeColor(resources.getColor(R.color.grey50))
        carView.layoutParams = paramsCard

        val linearContent = LinearLayout(cx)
        val paramsContent = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        linearContent.orientation = LinearLayout.VERTICAL
        linearContent.gravity = Gravity.CENTER
        linearContent.setPadding(8,8,8,8)
        linearContent.layoutParams = paramsContent
        linearContent.elevation = 8f
        viewModel.unidad.observe(viewLifecycleOwner){unidad ->
            sharedViewModel.usuario.value.let {usuario ->
                val viewUser = TextView(linearContent.context)
                viewUser.text = usuario?.nombre
                viewUser.gravity = Gravity.CENTER
                viewUser.textSize = 20f
                viewUser.setTypeface(null, Typeface.BOLD)
                linearContent.addView(viewUser)
            }
            linearContent.addView(renderUnidad(linearContent.context, unidad))
        }
        carView.addView(linearContent)
        linerParent.addView(carView)
        return linerParent
    }

    @SuppressLint("SetTextI18n")
    private fun renderUnidad(context:Context, unidad:Unidad): View {
        val paramsContent = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        val linearContent = LinearLayout(context)
        linearContent.gravity = Gravity.CENTER_HORIZONTAL
        linearContent.layoutParams = paramsContent
        linearContent.orientation = LinearLayout.HORIZONTAL
        linearContent.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE

        // contenido izquierda
        val lineaInfo = LinearLayout(linearContent.context)
        val paramsinfo = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        paramsinfo.gravity = Gravity.CENTER
        paramsinfo.weight = 1f
        lineaInfo.orientation = LinearLayout.VERTICAL
        lineaInfo.layoutParams = paramsinfo
        sharedViewModel.unidadAsignada.value.let { unit->
            val unidadText = TextView(lineaInfo.context)
            unidadText.text = unit
            unidadText.gravity = Gravity.CENTER
            unidadText.setTypeface(null, Typeface.BOLD)
            lineaInfo.addView(unidadText)
        }
        val desc = TextView(lineaInfo.context)
        desc.text = "${unidad.attr.descripcion} - (${unidad.attr.placasFederales})"
        desc.gravity = Gravity.CENTER
        lineaInfo.addView(desc)
        val tipo = TextView(lineaInfo.context)
        tipo.text = "${unidad.attr.tipo} de ${unidad.attr.pasajeros} Pasajeros"
        tipo.gravity = Gravity.CENTER
        lineaInfo.addView(tipo)

        // agrega item de la izquierda
        linearContent.addView(lineaInfo)

        // contenido de la derecha
        val lineaimage = LinearLayout(linearContent.context)
        lineaimage.orientation = LinearLayout.VERTICAL
        val paramslineImage = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        paramslineImage.weight = 1f
        lineaimage.layoutParams = paramslineImage

        val paramsImage = LayoutParams(210, 200)
        paramsImage.gravity = Gravity.CENTER
        val imageUnidad = ImageView(linearContent.context)
        imageUnidad.layoutParams = paramsImage
        // agrega item de la derecha
        lineaimage.addView(imageUnidad)
        linearContent.addView(lineaimage)
        val imgUri = unidad.attr.fotografia.toUri().buildUpon().scheme("https").build()
        GlideApp.with(imageUnidad.context)
            .load(imgUri)
            .apply(RequestOptions().placeholder(R.drawable.loading_animation).error(R.drawable.ic_broken_image))
            .into(imageUnidad)

        return linearContent
    }

    private fun renderQuestion(context: Context, question:EncuestaUnidad.Pregunta, parentIdPregunta:Int): LinearLayout {
        val linearContent = LinearLayout(context)
        val paramsContent = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        paramsContent.setMargins(0,20,0,20)
        linearContent.orientation = LinearLayout.VERTICAL
        linearContent.layoutParams = paramsContent

        val questionText = TextView(linearContent.context)
        questionText.text = question.PREGUNTA
        questionText.textSize = 17f
        linearContent.addView(questionText)

        val radioG = createCheckBox(linearContent, question.OPCIONES, question.ID_PREGUNTA, parentIdPregunta)
        linearContent.addView(radioG)

        val layouChildren = LinearLayout(context)
        val paramsChildren = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        paramsChildren.setMargins(50,0,0,10)
        layouChildren.layoutParams = paramsChildren
        layouChildren.setTag("contentChildren")
        layouChildren.orientation = LinearLayout.VERTICAL
        linearContent.addView(layouChildren)

        return linearContent
    }


    private fun createCheckBox(parent:LinearLayout, option: ArrayList<EncuestaUnidad.Option>, idPregunta:Int, parentIdPregunta: Int): RadioGroup {
        val paramsRG = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        val radioGroup = RadioGroup(parent.context)
        radioGroup.layoutParams = paramsRG
        radioGroup.contentDescription = "question_required"
        radioGroup.orientation = RadioGroup.HORIZONTAL

        option.forEach { option ->
            val paramsRB = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            paramsRB.setMargins(8,0,8,0)
            val radioButton = RadioButton(radioGroup.context)
            radioButton.id = option.ID_RESPUESTA
            radioButton.layoutParams = paramsRB
            radioButton.text = option.RESPUESTA
            radioButton.textSize = 15f
            radioButton.isAllCaps = true
            radioButton.buttonDrawable = AppCompatResources.getDrawable(radioButton.context, R.drawable.custom_box)
            radioButton.setPadding(10,10,10,10)
            radioGroup.addView(radioButton)
        }

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val contentLayout = parent.findViewWithTag<LinearLayout>("contentChildren")
            val response: RespuestaUnidad.Respuesta = RespuestaUnidad.Respuesta(idPregunta, checkedId, parentIdPregunta)
            viewModel.setValueResponse(response)
            val opt = option.find { it.ID_RESPUESTA == checkedId }
            contentLayout.removeAllViews()
            if(opt !== null) {
                if (opt.REQUIERE_FOTO){
                    contentLayout.addView(createPhoto(contentLayout.context, idPregunta))
                }else {
                    val res = viewModel.valueRespuestaImage.value?.find {
                        it.idPregunta == idPregunta
                    }
                    if(res !== null) {
                        val index = viewModel.valueRespuestaImage.value?.indexOf(res)
                        viewModel.valueRespuestaImage.value?.removeAt(index!!)
                    }
                }
                if(opt.PREGUNTA.isNotEmpty()) {
                    opt.PREGUNTA.forEach {
                        contentLayout.addView(renderQuestion(contentLayout.context, it, idPregunta))
                    }
                }
            }
        }

        return radioGroup
    }

    private fun createPhoto(cx:Context, idPregunta:Int): View {
       /* val paramsContent = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        val layoutContent = LinearLayout(cx)
        layoutContent.orientation = LinearLayout.VERTICAL
        layoutContent.layoutParams = paramsContent*/



        val layoutPhoto = LinearLayout(cx)
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        params.setMargins(8,0,8,0)
        layoutPhoto.orientation = LinearLayout.HORIZONTAL
        layoutPhoto.layoutParams = params
        layoutPhoto.setTag("photo")

        val textInfo = TextView(cx)
        textInfo.text = "Capturar evidencia"

        val layouPreview = ImageView(cx)
        val paramsPreview = LayoutParams(120, 120)
        paramsPreview.gravity = Gravity.CENTER
        paramsPreview.marginStart = 20
        layouPreview.contentDescription = "image_required"
        layouPreview.setImageResource(R.drawable.ic_broken_image)
        layouPreview.layoutParams = paramsPreview

        val btn = MaterialButton(cx)
        btn.setIconResource(R.drawable.camera_alt_white)
        val paramsBtn = LayoutParams(90, 120)
        paramsBtn.setMargins(20,0,0,20)
        btn.iconGravity = ICON_GRAVITY_TEXT_START
        btn.cornerRadius = 60
        btn.setPadding(0,0,0,0)
        btn.iconPadding = 0
        btn.iconSize = 50
        btn.layoutParams = paramsBtn
        btn.setOnClickListener {
            viewModel.imageComodin.value = layouPreview
            viewModel.idPreguntaComodin.value = idPregunta
            capturePhoto()
        }

        layoutPhoto.addView(textInfo)
        layoutPhoto.addView(btn)
        layoutPhoto.addView(layouPreview)
        return layoutPhoto
    }

    val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback<ActivityResult> {
        if(it.resultCode == Activity.RESULT_OK){
            val imageBitmap = it.data?.extras?.get("data") as Bitmap
            viewModel.imageComodin.value.let {
                it?.setImageBitmap(imageBitmap)

                viewModel.setValueResponseImage(viewModel.idPreguntaComodin.value!!, convertToFile(it?.context!!, imageBitmap))
            }
        }
    })

    private fun capturePhoto() {
        cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
    }

    private fun convertToFile(cx: Context, image:Bitmap):File {
        val wrapper = ContextWrapper(cx)
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")
        val stream:OutputStream = FileOutputStream(file)
        image.compress(Bitmap.CompressFormat.JPEG, 25, stream)
        stream.flush()
        stream.close()
        return file
    }

    private fun validCountElement(text:String):Int {
        var outRadio = ArrayList<View>()
        binding.stepContent.clContent.findViewsWithText(outRadio, text, RadioGroup.FIND_VIEWS_WITH_CONTENT_DESCRIPTION)
        return outRadio.size
    }
}
