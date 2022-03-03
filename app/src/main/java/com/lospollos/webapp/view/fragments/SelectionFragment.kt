package com.lospollos.webapp.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.lospollos.webapp.R
import com.lospollos.webapp.view.activity.MainActivity

class SelectionFragment : Fragment() {

    lateinit var listButton: Button
    lateinit var webButton: Button
    private lateinit var cameraButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listButton = view.findViewById(R.id.list_button)
        webButton = view.findViewById(R.id.web_button)
        cameraButton = view.findViewById(R.id.camera_button)
        cameraButton.setOnClickListener {
            (activity as MainActivity)
                .navController.navigate(R.id.action_selectionFragment_to_cameraFragment)
        }
        listButton.setOnClickListener {
            (activity as MainActivity)
                .navController.navigate(R.id.action_selectionFragment_to_listFragment)
        }
        webButton.setOnClickListener {
            (activity as MainActivity)
                .navController.navigate(R.id.action_selectionFragment_to_webFragment)
        }
    }

}