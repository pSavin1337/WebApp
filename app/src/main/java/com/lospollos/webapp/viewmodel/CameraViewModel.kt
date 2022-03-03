package com.lospollos.webapp.viewmodel

import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lospollos.webapp.R
import com.lospollos.webapp.data.CameraxHelper
import com.lospollos.webapp.App.Companion.context
import kotlinx.coroutines.*

private const val WIDTH = 200
private const val HEIGHT = 200

class CameraViewModel: ViewModel() {

    private var cameraxHelper: CameraxHelper? = null

    private val job = Job()
    private val vmScope = CoroutineScope(job + Dispatchers.Main)

    val uriLD = MutableLiveData<String>()

    override fun onCleared() {
        super.onCleared()
        vmScope.cancel()
    }

    fun onViewCreated(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        cameraxHelper = CameraxHelper(
            caller = lifecycleOwner,
            previewView = previewView,
            onPictureTaken = { uri ->
                vmScope.launch {
                    uriLD.value = uri
                }
            },
            onError = { Log.e("CAMERA", it.localizedMessage!!) },
            builderPreview = Preview.Builder().setTargetResolution(Size(WIDTH, HEIGHT)),
            builderImageCapture = ImageCapture.Builder().setTargetResolution(Size(WIDTH, HEIGHT)),
        )
        cameraxHelper?.start()
    }

    fun onTakePictureButtonClick() {
        cameraxHelper?.takePicture()
    }

    fun onChangeCameraButtonClick() {
        cameraxHelper?.changeCamera()
    }

    fun onChangeConfiguration() {
        cameraxHelper?.start()
    }

}