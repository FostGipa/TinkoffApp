package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityLoginBinding
import com.example.myapplication.utils.SupabaseClient

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val supabaseClient: SupabaseClient = SupabaseClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.mainLoginButton.setOnClickListener {
            if (binding.loginEmailEditText.text.isNotEmpty()) {
                supabaseClient.login(this@LoginActivity, binding.loginEmailEditText.text.toString())
                startActivity(Intent(this@LoginActivity, OTPVerifyActivity::class.java).putExtra("userEmail", binding.loginEmailEditText.text.toString()))
            } else {
                Toast.makeText(this@LoginActivity, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }
    }
}