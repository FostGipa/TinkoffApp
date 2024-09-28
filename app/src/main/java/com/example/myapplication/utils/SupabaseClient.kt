package com.example.myapplication.utils

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.OTP
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseClient : ViewModel() {

    private val supabase = createSupabaseClient(
        supabaseUrl = "https://szyrrfgwyygajvfqhojf.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InN6eXJyZmd3eXlnYWp2ZnFob2pmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTQ4OTQwNzMsImV4cCI6MjAzMDQ3MDA3M30.Y40B1B3hYInmcBS6QIWKdrVukI6G5Oo43LoUUcZge04"
    ) {
        install(Auth)
    }

    fun signUp(
        context: Context,
        userEmail: String,
        userFullName: String,
        userPassport: String,
        userAddress: String
    ) {
        viewModelScope.launch {
            try {
                supabase.auth.signUpWith(OTP) {
                    email = userEmail
                    data = buildJsonObject {
                        put("full_name", userFullName)
                        put("passport", userPassport)
                        put("address", userAddress)
                    }
                }
                saveToken(context)
            } catch (e: Exception) {
                Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun login(
        context: Context,
        userEmail: String
    ) {
        viewModelScope.launch {
            try {
                supabase.auth.signInWith(OTP) {
                    email = userEmail
                }
                saveToken(context)
            } catch (e: Exception) {
                Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun saveToken(context: Context) {
        viewModelScope.launch {
            val accessToken = supabase.auth.currentAccessTokenOrNull()
            val sharedPref = SharedPreferenceHelper(context)
            sharedPref.saveStringData("accessToken", accessToken)
        }

    }
}