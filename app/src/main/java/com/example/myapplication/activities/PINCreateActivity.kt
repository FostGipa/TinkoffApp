package com.example.myapplication.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.databinding.ActivityPincreateBinding
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.utils.SharedPreferenceHelper
import java.util.concurrent.Executor

class PINCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPincreateBinding
    private lateinit var slideAndReplace : Animation
    private lateinit var sharedPreferenceHelper: SharedPreferenceHelper
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: PromptInfo

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPincreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object  : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(this@PINCreateActivity, "Ошибка", Toast.LENGTH_SHORT).show()
                }
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val objects = listOf(binding.pinEditText1, binding.pinEditText2, binding.pinEditText3, binding.pinEditText4,
                        binding.pinView1, binding.pinView2, binding.pinView3, binding.pinView4)
                    objects.forEach {
                        it.startAnimation(slideAndReplace)
                    }
                    sharedPreferenceHelper.saveBoolData("fingerprintAuth", true)
                }
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@PINCreateActivity, "Ошибка", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = PromptInfo.Builder()
            .setTitle("Authorization")
            .setNegativeButtonText("Cancel")
            .setConfirmationRequired(false)
            .build()

        sharedPreferenceHelper = SharedPreferenceHelper(this@PINCreateActivity)

        slideAndReplace = AnimationUtils.loadAnimation(this, R.anim.pin_anim)
        slideAndReplace.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                val objects = listOf(binding.pinEditText1, binding.pinEditText2, binding.pinEditText3, binding.pinEditText4,
                    binding.pinView1, binding.pinView2, binding.pinView3, binding.pinView4)

                objects.forEach {
                    it.visibility = View.INVISIBLE
                }

                binding.successImageView.visibility = View.VISIBLE

                Handler(Looper.getMainLooper()).postDelayed({
                    sharedPreferenceHelper.saveBoolData("authorized", true)
                    startActivity(Intent(this@PINCreateActivity, MainActivity::class.java))
                }, 1000)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        val buttons = listOf(binding.pin1Button, binding.pin2Button, binding.pin3Button, binding.pin4Button, binding.pin5Button,
            binding.pin6Button, binding.pin7Button, binding.pin8Button, binding.pin9Button, binding.pin0Button)

        var activeEditText = binding.pinEditText1
        var lastEditText : EditText = binding.pinEditText1
        binding.pinEditText1.requestFocus()

        buttons.forEach { button ->
            button.setOnClickListener {
                when (activeEditText) {
                    binding.pinEditText1 -> {
                        activeEditText.setText(button.text.toString())
                        binding.pinView1.visibility = View.INVISIBLE
                        activeEditText.clearFocus()
                        activeEditText = binding.pinEditText2
                        binding.pinEditText2.requestFocus()
                        lastEditText = binding.pinEditText1
                    }
                    binding.pinEditText2 -> {
                        activeEditText.setText(button.text.toString())
                        binding.pinView2.visibility = View.INVISIBLE
                        activeEditText.clearFocus()
                        activeEditText = binding.pinEditText3
                        binding.pinEditText3.requestFocus()
                        lastEditText = binding.pinEditText2
                    }
                    binding.pinEditText3 -> {
                        activeEditText.setText(button.text.toString())
                        binding.pinView3.visibility = View.INVISIBLE
                        activeEditText.clearFocus()
                        activeEditText = binding.pinEditText4
                        binding.pinEditText4.requestFocus()
                        lastEditText = binding.pinEditText3
                    }
                    binding.pinEditText4 -> {
                        activeEditText.setText(button.text.toString())
                        binding.pinView4.visibility = View.INVISIBLE
                        activeEditText.clearFocus()
                        lastEditText = binding.pinEditText4

                        val pinCode = binding.pinEditText1.text.toString().trim() +
                                binding.pinEditText2.text.toString().trim() +
                                binding.pinEditText3.text.toString().trim() +
                                binding.pinEditText4.text.toString().trim()
                        sharedPreferenceHelper.saveStringData("pinCode", pinCode)

                        val dialogBinding = layoutInflater.inflate(R.layout.custom_dialog, null)
                        val myDialog = Dialog(this@PINCreateActivity)
                        myDialog.setContentView(dialogBinding)
                        myDialog.setCancelable(true)
                        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        myDialog.show()

                        val yesButton = dialogBinding.findViewById<Button>(R.id.dialogYesButton)
                        yesButton.setOnClickListener {
                            myDialog.dismiss()
                            biometricPrompt.authenticate(promptInfo)
                        }
                        val noButton = dialogBinding.findViewById<Button>(R.id.dialogNoButton)
                        noButton.setOnClickListener {
                            myDialog.dismiss()
                            buttons.forEach {
                                it.isEnabled = false
                            }
                            binding.pinRemoveButton.isEnabled = false

                            binding.pinEditText1.startAnimation(slideAndReplace)
                            binding.pinEditText2.startAnimation(slideAndReplace)
                            binding.pinEditText3.startAnimation(slideAndReplace)
                            binding.pinEditText4.startAnimation(slideAndReplace)
                        }
                    }
                }
            }
        }

        binding.pinRemoveButton.setOnClickListener {
            when (lastEditText) {
                binding.pinEditText1 -> {
                    lastEditText.setText("")
                    binding.pinView1.visibility = View.VISIBLE
                    binding.pinEditText1.requestFocus()
                    activeEditText = binding.pinEditText1
                    lastEditText = binding.pinEditText1
                }
                binding.pinEditText2 -> {
                    lastEditText.setText("")
                    binding.pinView2.visibility = View.VISIBLE
                    activeEditText.clearFocus()
                    lastEditText = binding.pinEditText1
                    lastEditText.requestFocus()
                    activeEditText = binding.pinEditText1
                }
                binding.pinEditText3 -> {
                    lastEditText.setText("")
                    binding.pinView3.visibility = View.VISIBLE
                    activeEditText.clearFocus()
                    lastEditText = binding.pinEditText2
                    lastEditText.requestFocus()
                    activeEditText = binding.pinEditText2
                }
                binding.pinEditText4 -> {
                    lastEditText.setText("")
                    binding.pinView4.visibility = View.VISIBLE
                    activeEditText.clearFocus()
                    lastEditText = binding.pinEditText3
                    lastEditText.requestFocus()
                    activeEditText = binding.pinEditText3
                }
            }
        }
    }
}