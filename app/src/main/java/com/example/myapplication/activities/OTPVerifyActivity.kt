package com.example.myapplication.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityOtpverifyBinding
import com.example.myapplication.models.Balance
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class OTPVerifyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpverifyBinding
    private var codeVerified : Boolean = false
    private val supabase = createSupabaseClient(
        supabaseUrl = "https://szyrrfgwyygajvfqhojf.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InN6eXJyZmd3eXlnYWp2ZnFob2pmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTQ4OTQwNzMsImV4cCI6MjAzMDQ3MDA3M30.Y40B1B3hYInmcBS6QIWKdrVukI6G5Oo43LoUUcZge04"
    ) {
        install(Auth)
        install(Postgrest)
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOtpverifyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.emailTextView.text = "Код отправлен на почту: " + intent.getStringExtra("userEmail")

        binding.mainSignupButton.setOnClickListener {
            if (codeVerified) {
                GlobalScope.launch (Dispatchers.Main) {
                    try {
                        val user = supabase.auth.retrieveUserForCurrentSession(updateSession = true)
                        val sharedPref = getSharedPreferences("myPref", MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putString("userUUID", user.id)
                        editor.apply()
                        val setBalance = Balance(user_uuid = user.id, balance = 300000.0)
                        supabase.from("Счета_Пользователей").insert(setBalance)
                        startActivity(Intent(this@OTPVerifyActivity, PINCreateActivity::class.java))
                    } catch (e : Exception) {
                        Toast.makeText(this@OTPVerifyActivity, e.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this@OTPVerifyActivity, "Введите код!", Toast.LENGTH_SHORT).show()
            }
        }

        setListener()
        initFocus()
    }

    private fun setListener() {
        binding.main.setOnClickListener {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.main.windowToken, 0)
        }

        setTextChangeListener(fromEditText = binding.otpCodeEditText1, targetEditText = binding.otpCodeEditText2)
        setTextChangeListener(fromEditText = binding.otpCodeEditText2, targetEditText = binding.otpCodeEditText3)
        setTextChangeListener(fromEditText = binding.otpCodeEditText3, targetEditText = binding.otpCodeEditText4)
        setTextChangeListener(fromEditText = binding.otpCodeEditText4, targetEditText = binding.otpCodeEditText5)
        setTextChangeListener(fromEditText = binding.otpCodeEditText5, targetEditText = binding.otpCodeEditText6)
        setTextChangeListener(fromEditText = binding.otpCodeEditText6, done = {
            verifyOTPCode()
        })

        setKeyListener(fromEditText = binding.otpCodeEditText2, backToEditText = binding.otpCodeEditText1)
        setKeyListener(fromEditText = binding.otpCodeEditText3, backToEditText = binding.otpCodeEditText2)
        setKeyListener(fromEditText = binding.otpCodeEditText4, backToEditText = binding.otpCodeEditText3)
        setKeyListener(fromEditText = binding.otpCodeEditText5, backToEditText = binding.otpCodeEditText4)
        setKeyListener(fromEditText = binding.otpCodeEditText6, backToEditText = binding.otpCodeEditText5)
    }

    private fun initFocus() {
        binding.otpCodeEditText1.isEnabled = true
        binding.otpCodeEditText1.postDelayed({
            binding.otpCodeEditText1.requestFocus()
            binding.otpCodeEditText1.setBackgroundResource(R.drawable.otp_bg)
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(binding.otpCodeEditText1, InputMethodManager.SHOW_FORCED)
        }, 500)
    }

    private fun reset() {
        val editTexts = listOf(binding.otpCodeEditText1, binding.otpCodeEditText2, binding.otpCodeEditText3,
            binding.otpCodeEditText4, binding.otpCodeEditText5, binding.otpCodeEditText6)
        editTexts.forEach {
            it.isEnabled = false
            it.setText("")
        }
        initFocus()
    }

    private fun setTextChangeListener(
        fromEditText : EditText,
        targetEditText : EditText? = null,
        done: (() -> Unit)? = null
    ) {
        fromEditText.addTextChangedListener {
            it?.let {string ->
                if (string.isNotEmpty()) {
                    targetEditText?.let { editText ->
                        editText.isEnabled = true
                        editText.requestFocus()
                        editText.setBackgroundResource(R.drawable.otp_bg)
                    } ?: kotlin.run {
                        done ?.let { done ->
                            done()
                        }
                    }
                    fromEditText.clearFocus()
                    fromEditText.isEnabled = false
                }
            }
        }
    }

    private fun setKeyListener(fromEditText: EditText, backToEditText: EditText) {
        fromEditText.setOnKeyListener { _, _, event ->
            if (null != event && KeyEvent.KEYCODE_DEL == event.keyCode) {
                backToEditText.isEnabled = true
                backToEditText.requestFocus()
                backToEditText.setText("")

                fromEditText.clearFocus()
                fromEditText.isEnabled = false
            }
            false
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun verifyOTPCode() {
        val otpCode = binding.otpCodeEditText1.text.toString().trim() +
                binding.otpCodeEditText2.text.toString().trim() +
                binding.otpCodeEditText3.text.toString().trim() +
                binding.otpCodeEditText4.text.toString().trim() +
                binding.otpCodeEditText5.text.toString().trim() +
                binding.otpCodeEditText6.text.toString().trim()

        try {
            val editTexts = listOf(binding.otpCodeEditText1, binding.otpCodeEditText2, binding.otpCodeEditText3,
                binding.otpCodeEditText4, binding.otpCodeEditText5, binding.otpCodeEditText6)

            GlobalScope.launch (Dispatchers.Main) {
                try {
                    supabase.auth.verifyEmailOtp(
                        type = OtpType.Email.EMAIL,
                        email = intent.getStringExtra("userEmail").toString(),
                        token = otpCode
                    )
                    editTexts.forEach {
                        it.setBackgroundResource(R.drawable.otp_success)
                    }
                    codeVerified = true
                } catch (e : Exception) {
                    Toast.makeText(this@OTPVerifyActivity, "Неверный код", Toast.LENGTH_SHORT).show()
                    editTexts.forEach {
                        it.setBackgroundResource(R.drawable.otp_error_bg)
                    }
                    reset()
                }
            }
            return
        } catch (e : Exception) {
            Toast.makeText(this@OTPVerifyActivity, e.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}