package com.lospollos.webapp.view.fragments

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.lospollos.webapp.R
import com.lospollos.webapp.view.activity.MainActivity
import android.webkit.ValueCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lospollos.webapp.view.RecyclerViewAdapter


class ListFragment : Fragment() {

    private lateinit var webView: WebView
    private lateinit var recyclerView: RecyclerView
    private val lessonsList = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recycler_view)
        webView = view.findViewById(R.id.list_web_view)
        webView.evaluateJavascript(
            "(function() { return document.getElementsByTagName('tbody')[0].innerHTML; })();"
        ) { html ->

        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = RecyclerViewAdapter(lessonsList)
        recyclerView.adapter = adapter
    }

}