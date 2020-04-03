package io.github.jmatsu.license.example

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class HtmlLicenseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_html_license)

        val webView: WebView = findViewById(R.id.webView)

        webView.loadUrl("file:///android_asset/license.html")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.share, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.html)?.isEnabled = false
        menu?.findItem(R.id.main)?.setOnMenuItemClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            true
        }
        return super.onPrepareOptionsMenu(menu)
    }
}
