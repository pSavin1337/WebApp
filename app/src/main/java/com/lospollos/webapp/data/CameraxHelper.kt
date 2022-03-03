package com.lospollos.webapp.data

import android.app.Activity
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Insets
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.WindowInsets
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.lospollos.webapp.App.Companion.context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


private const val RATIO_4_3_VALUE = 4.0 / 3.0
private const val RATIO_16_9_VALUE = 16.0 / 9.0
private const val QUALITY = 100
private const val OFFSET = 0

class CameraxHelper(
    private val caller: Any,
    private val previewView: PreviewView,
    private val imageAnalizer: ImageAnalysis.Analyzer? = null,
    private val onPictureTaken: ((String?) -> Unit)? = null,
    private val builderPreview: Preview.Builder? = null,
    private val builderImageCapture: ImageCapture.Builder? = null,
    private val onError: ((Throwable) -> Unit)? = null
) {

    private lateinit var imagePreview: Preview
    private lateinit var imageCapture: ImageCapture
    private var imageAnalysis: ImageAnalysis? = null

    private var lensFacing = CameraSelector.LENS_FACING_FRONT
    private val executor = Executors.newSingleThreadExecutor()
    private val activity = if (caller is Activity) caller else (caller as Fragment).activity!!

    fun start() {
        previewView.post { startCamera() }
    }

    private fun createImagePreview() =
        (builderPreview ?: Preview.Builder()
            .setTargetAspectRatio(aspectRatio()))
            .setTargetRotation(previewView.display.rotation)
            .build()
            .apply { setSurfaceProvider(previewView.surfaceProvider) }

    private fun createImageAnalysis() =
        ImageAnalysis.Builder()
            .setImageQueueDepth(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply { imageAnalizer?.let { setAnalyzer(executor, imageAnalizer) } }

    private fun createImageCapture() =
        (builderImageCapture ?: ImageCapture.Builder()
            .setTargetAspectRatio(aspectRatio()))
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()

    fun changeCamera() {
        lensFacing =
            if (lensFacing == CameraSelector.LENS_FACING_FRONT) CameraSelector.LENS_FACING_BACK
            else CameraSelector.LENS_FACING_FRONT
        startCamera()
    }

    private fun startCamera() {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        val cameraProviderFuture =
            ProcessCameraProvider
                .getInstance(activity)
        cameraProviderFuture.addListener(
            {
                try {
                    val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                    imagePreview = createImagePreview()
                    imagePreview.setSurfaceProvider(previewView.surfaceProvider)

                    imageCapture = createImageCapture()
                    imageAnalysis = createImageAnalysis()

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        caller as LifecycleOwner,
                        cameraSelector,
                        imagePreview,
                        imageCapture,
                        imageAnalysis
                    )
                } catch (exc: Exception) {
                    onError?.invoke(exc)
                }

            },
            ContextCompat
                .getMainExecutor(activity)
        )
    }

    private fun getScreenWidth(activity: Activity): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }

    private fun getScreenHeight(activity: Activity): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.height() - insets.bottom - insets.top
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
    }

    private fun aspectRatio(): Int {
        val width = getScreenWidth(activity)
        val height = getScreenHeight(activity)

        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    fun takePicture() {

        imageCapture.takePicture(
            executor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    onPictureTaken?.invoke(
                        saveImage(image)
                    )
                }

                override fun onError(exception: ImageCaptureException) {
                    onError?.invoke(exception)
                }
            })
    }

    private fun saveImage(image: ImageProxy): String? {
        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        val bitmapImage = BitmapFactory.decodeByteArray(bytes, OFFSET, bytes.size, null)
        var savedImagePath: String? = null
        val imageFileName = "PNG_weather_icon_${UUID.randomUUID()}.png"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
            }

            val resolver = context.contentResolver
            var uri: Uri? = null

            try {
                uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                resolver.openOutputStream(uri!!)?.use {
                    bitmapImage.compress(Bitmap.CompressFormat.PNG, QUALITY, it)
                }

                uri.toString()

            } catch (e: IOException) {

                uri?.let { orphanUri ->
                    resolver.delete(orphanUri, null, null)
                }

                throw e
            }
        } else {
            val storageDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + "/Photos"
            )
            var success = true
            if (!storageDir.exists()) {
                success = storageDir.mkdirs()
            }
            if (success) {
                val imageFile = File(storageDir, imageFileName)
                savedImagePath = imageFile.absolutePath
                try {
                    val fOut: OutputStream = FileOutputStream(imageFile)
                    bitmapImage.compress(Bitmap.CompressFormat.PNG, QUALITY, fOut)
                    fOut.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            savedImagePath
        }

    }

}