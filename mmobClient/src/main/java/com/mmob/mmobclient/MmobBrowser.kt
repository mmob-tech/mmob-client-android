package com.mmob.mmobclient

import android.annotation.TargetApi
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MmobBrowser : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mmob_browser)

        val webView = findViewById<WebView>(R.id.browser_web_view)
        val headerTitle = findViewById<TextView>(R.id.browser_title)
        val headerSubtitle = findViewById<TextView>(R.id.browser_subtitle)
        val padlockIcon = findViewById<ImageView>(R.id.padlock_icon)
        val closeButton = findViewById<ImageButton>(R.id.browser_close_button)
        val backButton = findViewById<ImageButton>(R.id.browser_back_button)
        val forwardButton = findViewById<ImageButton>(R.id.browser_forward_button)
        val intentUri = intent.getStringExtra("uri")

        val webSettings = webView.settings
        webSettings.domStorageEnabled = true
        webSettings.javaScriptEnabled = true
        padlockIcon.visibility = View.GONE

        if (intentUri != null) {
            webView.loadUrl(intentUri)
        }

        webView.webViewClient = object : WebViewClient() {
            @Deprecated("shouldOverrideUrlLoading is deprecated, providing support for older versions of Android")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return handleOverrideUrlLoading(view, url)
            }

            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest
            ): Boolean {
                return handleOverrideUrlLoading(view, request.url.toString())
            }

            private fun handleOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                    view?.loadUrl(url)
                }
                return true
            }

            // Update title, subtitle text when page finishes loading
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                val helper = MmobClientHelper()
                val title = view?.title
                val subtitle = helper.getHost(Uri.parse(url))

                if (title != null) {
                    headerTitle.text = title
                }

                if (subtitle != null) {
                    headerSubtitle.text = subtitle
                }

                if (url?.startsWith("https://") == true) {
                    padlockIcon.visibility = View.VISIBLE
                }

                if (!webView.canGoBack()) {
                    backButton.isClickable = false
                    backButton.setImageResource(R.drawable.ic_arrow_back_disabled)
                } else {
                    backButton.isClickable = true
                    backButton.setImageResource(R.drawable.ic_arrow_back)
                }

                if (!webView.canGoForward()) {
                    forwardButton.setImageResource(R.drawable.ic_arrow_forward_disabled)
                    forwardButton.isClickable = false
                } else {
                    forwardButton.isClickable = true
                    forwardButton.setImageResource(R.drawable.ic_arrow_forward)
                }
            }
        }

        // Action handlers
        closeButton.setOnClickListener {
            finish()
        }

        backButton.setOnClickListener {
            if (webView.canGoBack()) {
                webView.goBack()
            }
        }

        forwardButton.setOnClickListener {
            if (webView.canGoForward()) {
                webView.goForward()
            }
        }
    }
}