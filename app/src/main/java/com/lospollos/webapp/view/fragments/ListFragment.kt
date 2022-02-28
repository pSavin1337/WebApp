package com.lospollos.webapp.view.fragments

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import com.lospollos.webapp.R
import com.lospollos.webapp.view.activity.MainActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lospollos.webapp.view.RecyclerViewAdapter
import org.jsoup.Jsoup
import android.webkit.WebView

import android.webkit.WebViewClient


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

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recycler_view)
        webView = view.findViewById(R.id.list_web_view)
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                webView.evaluateJavascript(
                    "(function() { return (document.getElementsByTagName('tbody')[0].innerHTML); })();"
                ) { html ->
                    html
                        .replace("\\t", "")
                        .replace("\\n", "")
                        .replace("\\u003C", "<")
                        .split("/tr>")
                        .forEach {
                            val row = "$it/tr>"
                            val doc = Jsoup.parse(row)
                            val a = doc.select("a")
                            a.forEach { it1 ->
                                lessonsList.add(it1.text())
                            }
                        }
                    recyclerView.layoutManager = LinearLayoutManager(context)
                    val adapter = RecyclerViewAdapter(lessonsList)
                    recyclerView.adapter = adapter
                }
            }
        }
        webView.loadUrl("https://startandroid.ru/ru/uroki/vse-uroki-spiskom.html")
    }

}