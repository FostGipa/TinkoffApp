package com.example.myapplication.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityPopolnenieSuccessBinding
import java.text.DecimalFormat

class PopolnenieSuccessActivity : AppCompatActivity() {

    lateinit var binding: ActivityPopolnenieSuccessBinding

    @SuppressLint("SetTextI18n", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPopolnenieSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.okButton.setOnClickListener {
            finish()
            PopolnenieActivity.MyClass.activity?.finish()
            DepositDescriptionActivity.MyClass.activity?.finish()
        }
        binding.fromBalanceTextView.text = formatter(intent.getDoubleExtra("oldFromBalance", 0.0)) + " ₽" +
                " ➜ " + formatter(intent.getDoubleExtra("newFromBalance", 0.0)) + " ₽"
        binding.toBalanceTextView.text = formatter(intent.getDoubleExtra("oldToBalance", 0.0)) + " ₽" +
                " ➜ " + formatter(intent.getDoubleExtra("newToBalance", 0.0)) + " ₽"
        binding.toNameTextView.text = intent.getStringExtra("depositName")
        binding.sumTextView.text = formatter(intent.getDoubleExtra("popolnenieSum", 0.0)) + " ₽"

    }

    private fun formatter(n: Double) : String =
        DecimalFormat("#,###")
            .format(n)
            .replace(",", " ")
}