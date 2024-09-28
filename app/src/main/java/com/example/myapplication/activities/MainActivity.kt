package com.example.myapplication.activities

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapters.DepositsAdapter
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.models.Balance
import com.example.myapplication.models.UserDeposits
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.serializer.KotlinXSerializer
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter : DepositsAdapter
    private lateinit var hiddenAdapter : DepositsAdapter
    private var mList = mutableListOf<UserDeposits>()
    private var depositsList = mutableListOf<UserDeposits>()
    private var hiddenList = mutableListOf<UserDeposits>()
    private val supabase = createSupabaseClient(
        supabaseUrl = "https://szyrrfgwyygajvfqhojf.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InN6eXJyZmd3eXlnYWp2ZnFob2pmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTQ4OTQwNzMsImV4cCI6MjAzMDQ3MDA3M30.Y40B1B3hYInmcBS6QIWKdrVukI6G5Oo43LoUUcZge04"
    ) {
        install(Auth)
        install(Postgrest)
        defaultSerializer = KotlinXSerializer(Json)
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        adapter = DepositsAdapter(depositsList, this@MainActivity)
        binding.depositsRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        binding.depositsRecyclerView.adapter = adapter

        hiddenAdapter = DepositsAdapter(hiddenList, this@MainActivity)
        binding.hiddenDepositsRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        binding.hiddenDepositsRecyclerView.adapter = hiddenAdapter

        binding.mainNewDepositButton.setOnClickListener {
            startActivity(Intent(this@MainActivity, SelectDepositActivity::class.java))
        }

        val layouts = binding.layouts
        val layoutTransition = LayoutTransition()
        layouts.layoutTransition = layoutTransition
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        binding.expandableLayout.setOnClickListener {
            val view : Int
            if (binding.hiddenDepositsRecyclerView.visibility == View.GONE) {
                view = View.VISIBLE
                binding.arrow.rotation = 180F
            } else {
                view = View.GONE
                binding.arrow.rotation = 360F
            }
            binding.hiddenDepositsRecyclerView.visibility = view
        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                GlobalScope.launch (Dispatchers.Main) {
                    supabase.from("Вклады").update({
                        set("Скрыт", true)
                    }) {
                        filter {
                            depositsList[viewHolder.adapterPosition].id?.let { eq("id", it) }
                        }
                    }
                    hiddenList.add(depositsList[viewHolder.adapterPosition])
                    hiddenAdapter.notifyDataSetChanged()
                    depositsList.removeAt(viewHolder.adapterPosition)
                    adapter.notifyItemRemoved(viewHolder.adapterPosition)
                }
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.white
                        )
                    )
                    .addActionIcon(R.drawable.ic_visibility_off)
                    .create()
                    .decorate()

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }).attachToRecyclerView(binding.depositsRecyclerView)

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                GlobalScope.launch (Dispatchers.Main) {
                    supabase.from("Вклады").update({
                        set("Скрыт", false)
                    }) {
                        filter {
                            hiddenList[viewHolder.adapterPosition].id?.let { eq("id", it) }
                        }
                    }
                    depositsList.add(hiddenList[viewHolder.adapterPosition])
                    adapter.notifyDataSetChanged()
                    hiddenList.removeAt(viewHolder.adapterPosition)
                    hiddenAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                    if (hiddenList.isEmpty()) {
                        binding.hiddenDepositsRecyclerView.visibility = View.GONE
                        binding.arrow.rotation = 360F
                    }
                }
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.white
                        )
                    )
                    .addActionIcon(R.drawable.ic_visibility)
                    .create()
                    .decorate()

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }).attachToRecyclerView(binding.hiddenDepositsRecyclerView)

        binding.transactionsCardView.setOnClickListener {
            startActivity(Intent(this@MainActivity, TransactionHistoryActivity::class.java))
        }
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    @OptIn(DelicateCoroutinesApi::class)
    override fun onResume() {
        super.onResume()
        GlobalScope.launch (Dispatchers.Main){
            try {
                val response = supabase.from("Вклады").select().decodeList<UserDeposits>()
                val sharedPref = getSharedPreferences("myPref", MODE_PRIVATE)
                val balance = supabase.from("Счета_Пользователей").select {
                    filter {
                        sharedPref.getString("userUUID", "none")?.let { eq("user_uuid", it) }
                    }
                }.decodeSingle<Balance>()

                binding.balanceTextView.text = formatter(balance.balance) + " ₽"

                mList.addAll(response)
                binding.depositsCountTextView.text = mList.size.toString()
                for (i in 0 until mList.size) {
                    if (mList[i].hide == true) {
                        hiddenList.add(mList[i])
                        hiddenAdapter.notifyDataSetChanged()
                        Log.e("123", hiddenAdapter.itemCount.toString())
                    } else {
                        depositsList.add(mList[i])
                        adapter.notifyDataSetChanged()
                    }
                }
            } catch (e : Exception) {
                Log.e("123", e.message.toString())
                Toast.makeText(this@MainActivity, "Ошибочка", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mList.clear()
        hiddenList.clear()
        depositsList.clear()
    }

    override fun onPause() {
        super.onPause()
        mList.clear()
        hiddenList.clear()
        depositsList.clear()
    }

    private fun formatter(n: Double) : String =
        DecimalFormat("#,###")
            .format(n)
            .replace(",", " ")
}