package com.lospollos.webapp.view.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lospollos.webapp.App
import com.lospollos.webapp.R
import com.lospollos.webapp.viewmodel.CameraViewModel

class CameraFragment : Fragment() {

    private lateinit var takePhotoButton: Button
    private lateinit var changeCameraButton: Button

    private lateinit var cameraViewModel: CameraViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraViewModel = ViewModelProvider(this)[CameraViewModel::class.java]
        cameraViewModel.onViewCreated(this, view.findViewById(R.id.preview_view))

        takePhotoButton = view.findViewById(R.id.take_photo_button)
        changeCameraButton = view.findViewById(R.id.change_camera_button)

        takePhotoButton.setOnClickListener { cameraViewModel.onTakePictureButtonClick() }
        changeCameraButton.setOnClickListener { cameraViewModel.onChangeCameraButtonClick() }

        cameraViewModel.uriLD.observe(viewLifecycleOwner) {
            Toast.makeText(
                App.context,
                App.context.getString(R.string.toast_success_txt) + it,
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        cameraViewModel.onChangeConfiguration()
    }

}