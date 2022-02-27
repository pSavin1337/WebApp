package com.lospollos.webapp.view.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.lospollos.webapp.R
import android.webkit.WebSettings
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest

import android.os.Build

import android.annotation.TargetApi
import com.lospollos.webapp.view.activity.MainActivity


class WebFragment : Fragment() {

    private lateinit var webView: WebView
    val bundle = Bundle()

    private fun getPageName(url: String): String {
        val urlArr = url.split('/')
        return urlArr[urlArr.size - 1].split('.')[0]
    }

    private var webViewClient: WebViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            bundle.putString("page_name", getPageName(url))
            (activity as MainActivity).navController.navigate(
                R.id.action_webFragment_to_webInfoFragment,
                bundle
            )
            return true
        }

        @TargetApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            bundle.putString("page_name", getPageName(request.url.toString()))
            (activity as MainActivity).navController.navigate(
                R.id.action_webFragment_to_webInfoFragment,
                bundle
            )
            return true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_web, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView = view.findViewById(R.id.web_view)
        webView.loadUrl("https://startandroid.ru/ru/uroki/vse-uroki-spiskom.html")
        webView.webViewClient = webViewClient
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
    }

}