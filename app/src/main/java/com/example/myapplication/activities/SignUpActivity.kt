package com.example.myapplication.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivitySignUpBinding
import com.example.myapplication.utils.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.OTP
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val supabaseClient: SupabaseClient = SupabaseClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val editTexts = listOf(binding.signupNameEditText, binding.signupEmailEditText, binding.signupPassportEditText, binding.signupAddressEditText)

        editTexts.forEach {
            it.addTextChangedListener {
                updateProgressBar()
            }
        }

        binding.signupButton.setOnClickListener {
            var allFieldsFilled = true

            editTexts.forEach { editText ->
                if (editText.text.isEmpty()) {
                    editText.setBackgroundResource(R.drawable.custom_error_edittext)
                    allFieldsFilled = false
                }
            }

            if (allFieldsFilled) {
                supabaseClient.signUp(
                    this@SignUpActivity,
                    binding.signupEmailEditText.text.toString(),
                    binding.signupNameEditText.text.toString(),
                    binding.signupPassportEditText.text.toString(),
                    binding.signupAddressEditText.text.toString()
                    )
                startActivity(Intent(this@SignUpActivity, OTPVerifyActivity::class.java).putExtra("userEmail", binding.signupEmailEditText.text.toString()))
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgressBar() {
        var progress = 0
        val editTexts = listOf(binding.signupNameEditText, binding.signupEmailEditText, binding.signupPassportEditText, binding.signupAddressEditText)
        editTexts.forEach {
            if (it.text?.isNotEmpty() == true) {
                progress += 25
                it.setBackgroundResource(R.drawable.custom_edittext)
            }
        }
        binding.signupProgressBar.progress = progress
        binding.signupProgressTextView.text = "${progress}%"
    }
}