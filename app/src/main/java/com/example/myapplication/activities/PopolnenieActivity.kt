package com.example.myapplication.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityPopolnenieBinding
import com.example.myapplication.models.Balance
import com.example.myapplication.models.Transaction
import com.example.myapplication.models.UserDeposits
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date

class PopolnenieActivity : AppCompatActivity() {

    lateinit var binding: ActivityPopolnenieBinding
    private val supabase = createSupabaseClient(
        supabaseUrl = "https://szyrrfgwyygajvfqhojf.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InN6eXJyZmd3eXlnYWp2ZnFob2pmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTQ4OTQwNzMsImV4cCI6MjAzMDQ3MDA3M30.Y40B1B3hYInmcBS6QIWKdrVukI6G5Oo43LoUUcZge04"
    ) {
        install(Postgrest)
        defaultSerializer = KotlinXSerializer(Json)
    }

    class MyClass{
        companion object{
            var activity: Activity? = null
        }
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPopolnenieBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        MyClass.activity = this@PopolnenieActivity
        binding.closeButton.setOnClickListener { finish() }

        GlobalScope.launch (Dispatchers.Main) {
            val sharedPref = getSharedPreferences("myPref", MODE_PRIVATE)
            val balance = supabase.from("Счета_Пользователей").select {
                filter {
                    sharedPref.getString("userUUID", "none")?.let { eq("user_uuid", it) }
                }
            }.decodeSingle<Balance>()
            binding.fromBalanceTextView.text = formatter(balance.balance) + " ₽"

            val response = supabase.from("Вклады").select {
                filter {
                    eq("id", intent.getIntExtra("depositId", 1))
                }
            }.decodeSingle<UserDeposits>()

            binding.toBalanceTextView.text = formatter(response.balance) + " ₽"
            when (response.id_Deposit_Type) {
                1 -> {
                    binding.toNameTextView.text = "на Накопительный счет"
                }
                2 -> {
                    binding.toNameTextView.text = "на Инвесткопилку"
                }
                3 -> {
                    binding.toNameTextView.text = "на Вклад"
                }
            }

            binding.perevodButton.setOnClickListener {
                if ((balance.balance - binding.sumEditText.getNumericValue()) >= 0) {
                    GlobalScope.launch (Dispatchers.Main) {
                        val oldToBalance = response.balance
                        val oldFromBalance = balance.balance
                        val newToBalance = response.balance + binding.sumEditText.getNumericValue()
                        val newFromBalance = balance.balance - binding.sumEditText.getNumericValue()
                        supabase.from("Вклады").update(
                            {
                                set("Баланс", newToBalance)
                            }
                        ) {
                            filter {
                                eq("id", intent.getIntExtra("depositId", 1) )
                            }
                        }
                        supabase.from("Счета_Пользователей").update(
                            {
                                set("Баланс", newFromBalance)
                            }
                        ) {
                            filter {
                                sharedPref.getString("userUUID", "none")?.let { eq("user_uuid", it)
                                }
                            }
                        }
                        val date = SimpleDateFormat("yyyy-MM-dd")
                        val transaction = Transaction(
                            id_vklad = response.id,
                            id_transaction_type = 1,
                            sum = binding.sumEditText.getNumericValue(),
                            date = date.format(Date()).toString(),
                            name = response.id_Deposit_Type)
                        supabase.from("Транкзации").insert(transaction)

                        startActivity(Intent(this@PopolnenieActivity, PopolnenieSuccessActivity::class.java)
                            .putExtra("newFromBalance", newFromBalance)
                            .putExtra("newToBalance", newToBalance)
                            .putExtra("oldToBalance", oldToBalance)
                            .putExtra("oldFromBalance", oldFromBalance)
                            .putExtra("depositName", binding.toNameTextView.text.toString())
                            .putExtra("popolnenieSum", binding.sumEditText.getNumericValue()))
                    }
                } else {
                    Toast.makeText(this@PopolnenieActivity, "Недостаточно средств", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun formatter(n: Double) : String =
        DecimalFormat("#,###")
            .format(n)
            .replace(",", " ")
}