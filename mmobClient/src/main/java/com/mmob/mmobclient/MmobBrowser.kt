package com.mmob.mmobclient

import android.annotation.TargetApi
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MmobBrowser : AppCompatActivity() {
    private var userProceededUnsafely = false

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

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler,
                error: SslError
            ) {
                if (userProceededUnsafely) {
                    return handler.proceed()
                }

                var errorString = ""

                when (error.primaryError) {
                    SslError.SSL_UNTRUSTED -> errorString = "the certificate authority is not trusted."
                    SslError.SSL_EXPIRED -> errorString = "the certificate has expired."
                    SslError.SSL_IDMISMATCH -> errorString = "the certificate has a hostname mismatch."
                    SslError.SSL_NOTYETVALID -> errorString = "the certificate is not valid."
                }

                val builder = AlertDialog.Builder(this@MmobBrowser)
                builder.setTitle("SSL Certificate Error")
                builder.setMessage(
                        "We couldn't establish a secure connection to the website due to an SSL certificate error. " +
                        "This was because $errorString\n\n" +
                        "Your security is important to us. To ensure your safety, we recommend not proceeding to this website unless you trust its source."
                )

                builder.setPositiveButton("Go back") { _, _ ->
                    handler.cancel()

                    // If user can go back in webView, do so, else close the in app browser
                    if (view != null && view.canGoBack()) {
                        view.goBack()
                    } else {
                        finish()
                    }

                    Toast.makeText(this@MmobBrowser,
                        "Returning to previous page...", Toast.LENGTH_SHORT).show()
                }

                builder.setNegativeButton("Proceed unsafely") { _, _ ->
                    handler.proceed()
                    userProceededUnsafely = true

                    Toast.makeText(this@MmobBrowser,
                        "Proceeding unsafely...", Toast.LENGTH_SHORT).show()
                }

                builder.show()
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
                val subtitle = helper.getHost(url)

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
                    forwardButton.isClickable = false
                    forwardButton.setImageResource(R.drawable.ic_arrow_forward_disabled)
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