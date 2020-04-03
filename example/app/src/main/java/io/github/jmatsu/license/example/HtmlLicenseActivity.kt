package io.github.jmatsu.license.example

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class HtmlLicenseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_html_license)

        val webView: WebView = findViewById(R.id.webView)

        webView.loadUrl("file:///index.html")
    }
}
