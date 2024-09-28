package com.example.myapplication.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityPdfreaderBinding

class PDFReaderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfreaderBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPdfreaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.webView.webViewClient = WebViewClient()
        binding.webView.settings.setSupportZoom(true)
        binding.webView.settings.javaScriptEnabled = true
        val pdf = intent.getStringExtra("pdfLink")
        binding.webView.loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=$pdf")

        binding.closeButton.setOnClickListener { finish() }
    }
}