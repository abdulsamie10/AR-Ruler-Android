package com.example.ar_ruler_abdulsamie.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PointF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.eddystudio.scrollpicker.OnItemSelectedListener
import com.eddystudio.scrollpicker.OnItemUnselectedListener
import com.eddystudio.scrollpicker.ScrollPickerAdapter
import com.eddystudio.scrollpicker.ScrollPickerView
import com.example.ar_ruler_abdulsamie.R
import com.example.ar_ruler_abdulsamie.databinding.FragmentARRulerBinding
import com.google.android.material.snackbar.Snackbar
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import travel.ithaka.android.horizontalpickerlib.PickerLayoutManager
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class RenderedLine(
    val anchorNode: AnchorNode,
    var node1: AnchorNode?,
    var node2: AnchorNode?
)

class ARRulerFragment : Fragment(R.layout.fragment_a_r_ruler)  {
    var rv: RecyclerView? = null
    var adapter: PickerAdapter? = null

    private lateinit var binding : FragmentARRulerBinding
    private lateinit var arFragment: ArFragment
    private var widthLineRender: ModelRenderable? = null
    private val currentAnchorNode = ArrayList<AnchorNode>()
    private val labelArray: ArrayList<AnchorNode> = ArrayList()
    private val currentAnchor = ArrayList<Anchor?>()
    private var totalLength = 0f
    private var midCurser: ImageView? = null
    private var dottedLineRender: ModelRenderable? = null
    private var dottedLineBetween: AnchorNode? = null
    private var distanceText: AnchorNode? = null
    private var lastPlacedAnchorNode: AnchorNode? = null
    private val distanceRenderables = HashMap<Int, ViewRenderable>()
    private val renderedLinesMap: HashMap<Int, RenderedLine> = HashMap()
    private var lineIndex = 0
    private var isAnchorPlacementAllowed = true
    private var unitsCheck = 0
    private var selectedPosition = 0
    private lateinit var cardRunnable: Runnable
    private val handler = Handler(Looper.getMainLooper())
    private var manualCardDisplayRequested = false // Add this variable at the top of your class
    private lateinit var manualCardRunnable: Runnable
    private val AUTO_CARD_DELAY_MIN = 100000L // 100 seconds in milliseconds
    private val AUTO_CARD_DELAY_MAX = 120000L // 120 seconds in milliseconds

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){

        binding = FragmentARRulerBinding.bind(view)
        midCurser = binding.midCursor

        val detectedPlaneCount = 0

        arFragment = childFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment

        binding.btnRemoveLast.setOnClickListener {
            removeLastLine()
        }

        binding.btnCapture.setOnClickListener {
            //takePhoto()
        }

        initObjects()

        binding.btnAdd.setOnClickListener {
            addFromAim()
        }

        arFragment.arSceneView.scene.addOnUpdateListener {
            val newPlaneCount = arFragment.arSceneView.session?.getAllTrackables(Plane::class.java)?.size ?: 0

            if (newPlaneCount > detectedPlaneCount) {
                binding.btnAddTemp.visibility = View.GONE
                binding.btnAdd.visibility = View.VISIBLE
                binding.btnCaptureTemp.visibility = View.GONE
                binding.btnCapture.visibility = View.VISIBLE
                binding.startMeasuringTemp.visibility = View.GONE
                binding.polygonImg.visibility = View.GONE
            }
            updateDistance()
        }

        binding.screenHome.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_ARRulerFragment_to_homeFragment)
        }

        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            Toast.makeText(requireContext(), "Plane detected!", Toast.LENGTH_SHORT).show()
        }

        horizontalScrolling()

        binding.viewHelp.setOnClickListener {
            manualCardDisplayRequested = true
            manualCardRunnable.run()
        }

        binding.closeHelp.setOnClickListener {
            hideCard()
        }

        cardRunnable = Runnable {
            if (!manualCardDisplayRequested) {
                showCard()
                startTimer()
                val randomTiming = Random.nextLong(AUTO_CARD_DELAY_MIN, AUTO_CARD_DELAY_MAX)
                handler.postDelayed(cardRunnable, randomTiming)
            }
        }

        manualCardRunnable = Runnable {
            showCard()
            startTimer()
        }

        // Initial card display
        val initialRandomTiming = Random.nextLong(AUTO_CARD_DELAY_MIN, AUTO_CARD_DELAY_MAX)
        handler.postDelayed(cardRunnable, initialRandomTiming)
    }

    private fun showCard() {
        binding.cardView.visibility = View.VISIBLE
        binding.viewHelp.visibility = View.GONE
        binding.closeHelp.visibility = View.VISIBLE

    }

    private fun hideCard() {
        binding.cardView.visibility = View.GONE
        binding.viewHelp.visibility = View.VISIBLE
        binding.closeHelp.visibility = View.GONE
        handler.removeCallbacks(cardRunnable)
        manualCardDisplayRequested = false // Reset the flag when hiding the card
    }

    private fun startTimer() {
        val durationMillis = 10000L // Total duration of the timer in milliseconds
        val updateIntervalMillis = 100L // Update the progress every 100 milliseconds
        val textChangeIntervalMillis = 2500L // Change text every 2.5 seconds
        val steps = (durationMillis / updateIntervalMillis).toInt()
        var step = 0
        var count = 1
        var lastTextChangeTime = System.currentTimeMillis()

        val timerRunnable = object : Runnable {
            override fun run() {
                if (step <= steps) {
                    val progress = cubicEaseInOut(step.toFloat(), 0f, 100f, steps.toFloat()).toInt()
                    binding.progressBar.progress = progress

                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastTextChangeTime >= textChangeIntervalMillis) {
                        count = (count % 4) + 1 // Loop through 1, 2, 3, 4
                        val cardTextResource = when (count) {
                            1 -> R.string.card_help_first_text
                            2 -> R.string.card_help_second_text
                            3 -> R.string.card_help_third_text
                            4 -> R.string.card_help_fourth_text
                            else -> R.string.card_help_first_text
                        }
                        binding.cardText.text = getString(cardTextResource)
                        lastTextChangeTime = currentTime
                    }

                    step++
                    handler.postDelayed(this, updateIntervalMillis)
                } else {
                    binding.progressBar.progress = 0 // Reset progress to zero
                    hideCard()
                }
            }
        }
        handler.post(timerRunnable)
    }

    private fun cubicEaseInOut(t: Float, b: Float, c: Float, d: Float): Float {
        var t = t
        t /= d / 2
        return if (t < 1) {
            c / 2 * t * t * t + b
        } else {
            t -= 2
            c / 2 * (t * t * t + 2) + b
        }
    }

    private fun horizontalScrolling() {
        arFragment.arSceneView.planeRenderer.isEnabled = true

        rv = binding.rv

        val pickerLayoutManager = PickerLayoutManager(requireContext(), PickerLayoutManager.HORIZONTAL, false)
        pickerLayoutManager.isChangeAlpha = true
        pickerLayoutManager.scaleDownBy = 1.0f
        pickerLayoutManager.scaleDownDistance = 1.0f

        adapter = PickerAdapter(requireContext(), getData(), rv)

        // Attach a SnapHelper that snaps to the nearest item
        val snapHelper: SnapHelper = PagerSnapHelper() // Use PagerSnapHelper
        snapHelper.attachToRecyclerView(rv)

        rv!!.layoutManager = pickerLayoutManager
        rv!!.adapter = adapter

        pickerLayoutManager.setOnScrollStopListener { view ->
            val selectedValue = (view as TextView).text.toString()
            Toast.makeText(
                requireContext(),
                "Selected value: $selectedValue",
                Toast.LENGTH_SHORT
            ).show()

            unitsCheck = when (selectedValue) {
                "Centimeter" -> 0
                "Meter" -> 1
                "Milli Meter" -> 2
                "Inch" -> 3
                "Feet" -> 4
                else -> unitsCheck
            }

            selectedPosition = pickerLayoutManager.getPosition(view)
            adapter!!.setSelectedItemPosition(selectedPosition)
        }
    }

    private fun getData(): List<String> {
        val data: MutableList<String> = ArrayList()
        data.add("Centimeter")
        data.add("Meter")
        data.add("Milli Meter")
        data.add("Feet")
        data.add("Inch")

        return data
    }

    private fun generateFilename(): String {
        val date: String = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        return Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        ).toString() + File.separator + "AR-Ruler/" + date + "_ar_ruler.jpg"
    }

    @Throws(IOException::class)
    private fun saveBitmapToDisk(bitmap: Bitmap, filename: String) {
        val out = File(filename)
        if (!out.parentFile?.exists()!!) {
            out.parentFile?.mkdirs()
        }
        try {
            FileOutputStream(filename).use { outputStream ->
                ByteArrayOutputStream().use { outputData ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputData)
                    outputData.writeTo(outputStream)
                    outputStream.flush()
                    outputStream.close()
                }
            }
        } catch (ex: IOException) {
            throw IOException("Failed to save bitmap to disk", ex)
        }
    }

    private fun takePhoto() {
        val filename = generateFilename()
        val view = arFragment.arSceneView
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val handlerThread = HandlerThread("PixelCopier")
        handlerThread.start()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { PixelCopy.request(view, bitmap, { copyResult ->
                if (copyResult === PixelCopy.SUCCESS) {
                    try {
                        saveBitmapToDisk(bitmap, filename)
                        val uri = Uri.parse("file://$filename")
                        val i = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                        i.data = uri
                    } catch (e: IOException) {
                        val toast = Toast.makeText(
                            requireContext(), e.toString(),
                            Toast.LENGTH_LONG
                        )
                        toast.show()
                        return@request
                    }
                    val snackbar = Snackbar.make(view,
                        "The screenshot has been saved.", Snackbar.LENGTH_LONG
                    )
                    snackbar.setAction("View in the gallery.") {
                        val photoFile = File(filename)
                        val photoURI = FileProvider.getUriForFile(
                            requireContext(),
                            requireActivity().packageName + ".ar.ruler",
                            photoFile
                        )
                        val intent = Intent(Intent.ACTION_VIEW, photoURI)
                        intent.setDataAndType(photoURI, "image/*")
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        startActivity(intent)
                    }
                    snackbar.show()
                } else {
                    val toast = Toast.makeText(
                        requireContext(),
                        "Screenshot save failed: $copyResult", Toast.LENGTH_LONG
                    )
                    toast.show()
                }
                handlerThread.quitSafely()
            }, Handler(handlerThread.looper))
        }
    }

    override fun onStart() {
        super.onStart()
        arFragment.onStart()
    }

    override fun onPause() {
        super.onPause()
        arFragment.onPause()
    }

    override fun onResume() {
        super.onResume()
        arFragment.onResume()
        checkPlaneDetected()
    }

    private fun removeLastLine() {
        val lastNode = currentAnchorNode.lastOrNull()
        if (lastNode != null) {
            val lineId = lineIndex - 1
            if (renderedLinesMap.containsKey(lineId)) {
                val renderedLine = renderedLinesMap[lineId]
                renderedLine?.apply {
                    anchorNode.setParent(null)
                    anchorNode.anchor?.detach()
                    if (node1 != null && node1 == lastNode) {
                        renderedLinesMap.remove(node2?.hashCode())
                    } else if (node2 != null && node2 == lastNode) {
                        renderedLinesMap.remove(node1?.hashCode())
                    }
                    renderedLinesMap.remove(lineId)
                }
                if (labelArray.isNotEmpty()) {
                    labelArray.lastOrNull()?.let {
                        it.anchor?.detach()
                        it.setParent(null)
                        labelArray.removeAt(labelArray.size - 1)
                    }
                }
            }
            currentAnchorNode.remove(lastNode)
            lastNode.anchor?.detach()
            lastPlacedAnchorNode = currentAnchorNode.lastOrNull()
            totalLength = calculateTotalLength()
            lineIndex = renderedLinesMap.size
        }
    }

    private fun calculateTotalLength(): Float {
        var total = 0f
        for (i in 0 until currentAnchorNode.size - 1) {
            val node1 = currentAnchorNode[i]
            val node2 = currentAnchorNode[i + 1]
            val distance = Vector3.subtract(node2.worldPosition, node1.worldPosition).length()
            total += distance
        }
        return total
    }

    private fun updateDistance() {
        if (lastPlacedAnchorNode != null) {
            val node1Pos = lastPlacedAnchorNode!!.worldPosition
            val midCursorPosition = getMidCursorPosition()
            var closestHit: HitResult? = null
            var closestDistance = Float.MAX_VALUE
            arFragment.arSceneView.arFrame?.hitTest(midCursorPosition.x, midCursorPosition.y)?.forEach { hitResult ->
                if (hitResult.trackable is Plane && hitResult.distance >= 0 && hitResult.distance < closestDistance) {
                    closestHit = hitResult
                    closestDistance = hitResult.distance
                }
            }
            closestHit?.let { hitResult ->
                val hitPosition = hitResult.hitPose.translation
                val midCursorWorldPos = Vector3(hitPosition[0], hitPosition[1], hitPosition[2])
                val difference = Vector3.subtract(midCursorWorldPos, node1Pos)
                if (dottedLineBetween == null) {
                    dottedLineBetween = AnchorNode().apply {
                        setParent(arFragment.arSceneView.scene)
                        renderable = dottedLineRender
                    }
                }
                dottedLineBetween!!.apply {
                    worldPosition = Vector3.add(node1Pos, difference.scaled(0.5f))
                    worldRotation = Quaternion.lookRotation(difference.normalized(), Vector3.up())
                    localScale = Vector3(1f, 1f, difference.length())
                    isEnabled = true
                }
                val distance = difference.length() / 2
                if (distanceText == null) {
                    distanceText = AnchorNode().apply {
                        setParent(arFragment.arSceneView.scene)
                        initTextBoxes(distance, this)
                    }
                } else {
                    distanceText!!.apply {
                        worldPosition = Vector3.add(node1Pos, difference.scaled(0.5f))
                        val distanceRenderable = distanceRenderables[this.hashCode()]
                        if (distanceRenderable != null) {
                            updateDistanceText(distance, distanceRenderable)
                        }
                    }
                }
                totalLength = calculateTotalLength()
            }
        } else {
            dottedLineBetween?.isEnabled = false
            distanceText?.anchor?.detach()
            distanceText?.setParent(null)
        }
    }

    private fun initObjects() {
        MaterialFactory.makeOpaqueWithColor(requireContext(), Color(0f, 100f, 0f))
            .thenAccept { material: Material? ->
                val widthLineRender = ShapeFactory.makeCube(Vector3(0.002F, 0.002f, 1f), Vector3.zero(), material)
                widthLineRender.isShadowCaster = false
                widthLineRender.isShadowReceiver = false
                this@ARRulerFragment.widthLineRender = widthLineRender
                addFromAim()
            }

        MaterialFactory.makeOpaqueWithColor(requireContext(), Color(0f, 0f, 255f))
            .thenAccept { material: Material? ->
                val dottedLineRender = ShapeFactory.makeCube(Vector3(0.002f, 0.002f, 1f), Vector3.zero(), material)
                dottedLineRender.isShadowCaster = false
                dottedLineRender.isShadowReceiver = false
                this@ARRulerFragment.dottedLineRender = dottedLineRender
            }
    }

    private fun initTextBoxes(distance: Float, transformableNode: AnchorNode) {
        val distanceRenderable = distanceRenderables[transformableNode.hashCode()]
        if (distanceRenderable != null) {
            updateDistanceText(distance / 2, distanceRenderable)
        } else {
            ViewRenderable.builder().setView(requireContext(), R.layout.distance).build().thenAccept { renderable: ViewRenderable ->
                    renderable.apply {
                        isShadowCaster = false
                        isShadowReceiver = false
                        verticalAlignment = ViewRenderable.VerticalAlignment.TOP
                    }
                    distanceRenderables[transformableNode.hashCode()] = renderable
                    updateDistanceText(distance / 2, renderable)
                    transformableNode.renderable = renderable
                    val rotationFromAToB = Quaternion.lookRotation(Vector3.up(), Vector3.up())
                    transformableNode.worldRotation = Quaternion.multiply(rotationFromAToB, Quaternion.axisAngle(
                        Vector3.forward(), 180f))
                }
        }
    }

    private fun updateDistanceText(distance: Float, renderable: ViewRenderable) {
        val scaleFactor = 2f
        val scaledDistance = distance * scaleFactor
        val distanceTextView = renderable.view.findViewById<TextView>(R.id.planetInfoCard)

        val displayDistance: String = when (unitsCheck) {
            0 -> String.format(Locale.ENGLISH, "%.2f", scaledDistance * 100) + " cm"
            1 -> String.format(Locale.ENGLISH, "%.2f", scaledDistance) + " m"
            2 -> String.format(Locale.ENGLISH, "%.2f", scaledDistance * 1000) + " mm"
            3 -> String.format(Locale.ENGLISH, "%.2f", scaledDistance / 0.0254) + " inch"
            4 -> String.format(Locale.ENGLISH, "%.2f", scaledDistance / 0.3048) + " feet"
            else -> String.format(Locale.ENGLISH, "%.2f", scaledDistance) + " m"
        }

        distanceTextView.text = displayDistance
    }

    private fun drawLineBetweenNodes(node1: AnchorNode, node2: AnchorNode, distance: Float) {
        val lineId = lineIndex++
        val lineAnchorNode = AnchorNode().apply {
            setParent(arFragment.arSceneView.scene)
            worldPosition = Vector3.add(node1.worldPosition, node2.worldPosition).scaled(.5f)
            worldRotation = Quaternion.lookRotation(Vector3.subtract(node2.worldPosition, node1.worldPosition).normalized(), Vector3.up())
            localScale = Vector3(1f, 1f, distance)
            renderable = widthLineRender
        }
        renderedLinesMap[lineId] = RenderedLine(lineAnchorNode, node1, node2)
        val labelAnchorNode = AnchorNode().apply {
            setParent(arFragment.arSceneView.scene)
            worldPosition = Vector3.add(node1.worldPosition, node2.worldPosition).scaled(.5f)
            initTextBoxes(distance, this)
        }
        labelArray.add(labelAnchorNode)
        node1.isEnabled = false
        node2.isEnabled = false
    }

    private fun addFromAim() {
        val midCursorPosition = getMidCursorPosition()
        val hits = arFragment.arSceneView.arFrame?.hitTest(midCursorPosition.x, midCursorPosition.y)
        var closestHit: HitResult? = null
        var closestDistance = Float.MAX_VALUE
        hits?.forEach { hitResult ->
            if (hitResult.trackable is Plane && hitResult.distance >= 0 && hitResult.distance < closestDistance) {
                closestHit = hitResult
                closestDistance = hitResult.distance
            }
        }
        closestHit?.let { hitResult ->
            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)
            TransformableNode(arFragment.transformationSystem).apply {
                setParent(anchorNode)
                translationController.isEnabled = false
                rotationController.isEnabled = false
                scaleController.isEnabled = false
            }
            currentAnchor.add(anchor)
            currentAnchorNode.add(anchorNode)
            lastPlacedAnchorNode = anchorNode
            isAnchorPlacementAllowed = false
            if (currentAnchorNode.size >= 2) {
                val node1 = currentAnchorNode[currentAnchorNode.size - 2]
                val node2 = currentAnchorNode[currentAnchorNode.size - 1]
                val distance = Vector3.subtract(node2.worldPosition, node1.worldPosition).length()
                drawLineBetweenNodes(node1, node2, distance)
                totalLength += distance
                if (distanceText == null) {
                    distanceText = AnchorNode().apply {
                        setParent(arFragment.arSceneView.scene)
                        initTextBoxes(distance, this)
                    }
                } else {
                    distanceText!!.apply {
                        worldPosition = Vector3.add(node1.worldPosition, node2.worldPosition).scaled(0.5f)
                        val distanceRenderable = distanceRenderables[this.hashCode()]
                        if (distanceRenderable != null) {
                            updateDistanceText(distance, distanceRenderable)
                        }
                    }
                }
                val rotationFromAToB = Quaternion.lookRotation(Vector3.up(), Vector3.up())
                distanceText?.worldRotation = Quaternion.multiply(rotationFromAToB, Quaternion.axisAngle(
                    Vector3.forward(), 180f))
            }
        }
    }

    private fun getMidCursorPosition(): PointF {
        val midCursorView = binding.midCursor
        val location = IntArray(2)
        midCursorView.getLocationInWindow(location)
        val centerX = location[0] + midCursorView.width / 2f
        val centerY = location[1] + midCursorView.height / 2f
        return PointF(centerX, centerY)
    }

    private fun checkPlaneDetected() {
        val updatedPlanes = arFragment.arSceneView.arFrame?.getUpdatedTrackables(Plane::class.java)
        isAnchorPlacementAllowed = updatedPlanes?.isNotEmpty() ?: false
    }
}