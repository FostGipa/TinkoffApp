package com.example.myapplication.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityInvestBinding
import com.example.myapplication.models.Stavka
import com.example.myapplication.models.UserDeposits
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date

class InvestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInvestBinding
    private val supabase = createSupabaseClient(
        supabaseUrl = "https://szyrrfgwyygajvfqhojf.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InN6eXJyZmd3eXlnYWp2ZnFob2pmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTQ4OTQwNzMsImV4cCI6MjAzMDQ3MDA3M30.Y40B1B3hYInmcBS6QIWKdrVukI6G5Oo43LoUUcZge04"
    ) {
        install(Postgrest)
        defaultSerializer = KotlinXSerializer(Json)
    }

    @SuppressLint("SimpleDateFormat")
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityInvestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.closeButton.setOnClickListener { finish() }

        binding.podrobneeButton.setOnClickListener {
            startActivity(
                Intent(this@InvestActivity, PDFReaderActivity::class.java)
                .putExtra("pdfLink", "https://acdn.tinkoff.ru/static/documents/a19d680a-e1d8-4ad1-af72-7866184c2af2.pdf"))
        }

        binding.openButton.setOnClickListener {
            GlobalScope.launch (Dispatchers.Main) {
                val procent = supabase.from("Виды_Вкладов").select(
                    Columns.raw("""
                    procents->>"stavka"
                """.trimIndent())) {
                    filter {
                        eq("id", 2)
                    }
                }.decodeSingle<Stavka>()
                val sharedPref = getSharedPreferences("myPref", MODE_PRIVATE)
                val userUUID = sharedPref.getString("userUUID", "none")
                val date = SimpleDateFormat("yyyy-MM-dd")
                val deposit = UserDeposits(user_uuid = userUUID.toString(),
                    id_Deposit_Type = 2,
                    balance = 0.0,
                    start_sum = 0.0,
                    srok = null,
                    open_date = date.format(Date()).toString(),
                    stavka = procent.stavka)
                supabase.from("Вклады").insert(deposit)
                finish()
            }
        }
    }
}