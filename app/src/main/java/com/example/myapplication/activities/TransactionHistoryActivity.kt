package com.example.myapplication.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapters.TransactionHistoryAdapter
import com.example.myapplication.databinding.ActivityTransactionHistoryBinding
import com.example.myapplication.models.Transaction
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class TransactionHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionHistoryBinding
    private var mList = mutableListOf<Transaction>()
    private lateinit var adapter: TransactionHistoryAdapter
    private val supabase = createSupabaseClient(
        supabaseUrl = "https://szyrrfgwyygajvfqhojf.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InN6eXJyZmd3eXlnYWp2ZnFob2pmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTQ4OTQwNzMsImV4cCI6MjAzMDQ3MDA3M30.Y40B1B3hYInmcBS6QIWKdrVukI6G5Oo43LoUUcZge04"
    ) {
        install(Postgrest)
        defaultSerializer = KotlinXSerializer(Json)
    }

    @SuppressLint("NotifyDataSetChanged")
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.closeButton.setOnClickListener { finish() }

        adapter = TransactionHistoryAdapter(mList, this@TransactionHistoryActivity)
        binding.transactionRecyclerview.layoutManager = LinearLayoutManager(this@TransactionHistoryActivity)
        binding.transactionRecyclerview.adapter = adapter

        GlobalScope.launch (Dispatchers.Main) {
            val depositId = intent.getIntExtra("depositId", 0)
            if (depositId == 0) {
                val response = supabase.from("Транкзации").select().decodeList<Transaction>()
                mList.addAll(response)
            } else {
                val response = supabase.from("Транкзации").select{
                    filter {
                        eq("id_вклад", intent.getIntExtra("depositId", 0))
                    }
                }.decodeList<Transaction>()
                mList.addAll(response)
            }
            adapter.notifyDataSetChanged()
        }
    }
}