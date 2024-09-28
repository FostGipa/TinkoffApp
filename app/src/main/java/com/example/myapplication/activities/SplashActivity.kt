package com.example.myapplication.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.utils.SharedPreferenceHelper

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        sharedPreferenceHelper = SharedPreferenceHelper(this@SplashActivity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        Handler(Looper.getMainLooper()).postDelayed({
            if (sharedPreferenceHelper.getBoolData("authorized")) {
                startActivity(Intent(this@SplashActivity, PINLoginActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, OnboardingActivity::class.java))
            }
        }, 3000)
    }
}