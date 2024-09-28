package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivitySelectDepositBinding

class SelectDepositActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectDepositBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySelectDepositBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.closeButton.setOnClickListener { finish() }
        binding.vkladButton.setOnClickListener{
            startActivity(Intent(this@SelectDepositActivity, VkladActivity::class.java))
            finish()
        }

        binding.nakopButton.setOnClickListener {
            startActivity(Intent(this@SelectDepositActivity, NakopitelniyActivity::class.java))
            finish()
        }

        binding.investButton.setOnClickListener {
            startActivity(Intent(this@SelectDepositActivity, InvestActivity::class.java))
            finish()
        }
    }
}