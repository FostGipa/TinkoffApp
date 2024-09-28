package com.example.myapplication.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityDepositDescriptionBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.text.DecimalFormat

class DepositDescriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDepositDescriptionBinding
    private val supabase = createSupabaseClient(
        supabaseUrl = "https://szyrrfgwyygajvfqhojf.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InN6eXJyZmd3eXlnYWp2ZnFob2pmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTQ4OTQwNzMsImV4cCI6MjAzMDQ3MDA3M30.Y40B1B3hYInmcBS6QIWKdrVukI6G5Oo43LoUUcZge04"
    ) {
        install(Auth)
        install(Postgrest)
        defaultSerializer = KotlinXSerializer(Json)
    }

    class MyClass{
        companion object{
            var activity: Activity? = null
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDepositDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        MyClass.activity = this@DepositDescriptionActivity
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.depositNameTextView.text = intent.getStringExtra("depositName")
        binding.depositBalanceTextView.text = formatter(intent.getDoubleExtra("depositBalance", 0.0)) + " ₽"
        binding.stavkaTextView.text = "Ставка " + intent.getDoubleExtra("depositStavka", 0.0).toString() + "%"

        binding.closeButton.setOnClickListener {
            GlobalScope.launch (Dispatchers.Main){
                supabase.from("Вклады").delete{
                    filter {
                        eq("id", intent.getIntExtra("depositId", 1))
                    }
                }
                finish()
            }
        }

        binding.shareButton.setOnClickListener {
            val intent= Intent()
            intent.action=Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT,"Реквизиты счета: ")
            intent.type="text/plain"
            startActivity(Intent.createChooser(intent,"Отправить"))
        }

        binding.popolnenieButton.setOnClickListener {
            val dialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.fragment_popolnenie_select, null)
            val popolnenieButton = view.findViewById<ConstraintLayout>(R.id.myDepositButton)
            popolnenieButton.setOnClickListener {
                startActivity(Intent(this@DepositDescriptionActivity, PopolnenieActivity::class.java)
                    .putExtra("depositId", intent.getIntExtra("depositId", 1)))
            }
            dialog.setCancelable(true)
            dialog.setContentView(view)
            dialog.show()
        }

        binding.historyTransaction.setOnClickListener {
            startActivity(Intent(this@DepositDescriptionActivity, TransactionHistoryActivity::class.java).putExtra("depositId", intent.getIntExtra("depositId", 0)))
        }
    }

    private fun formatter(n: Double) : String =
        DecimalFormat("#,###")
            .format(n)
            .replace(",", " ")
}