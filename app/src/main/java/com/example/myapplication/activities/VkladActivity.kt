package com.example.myapplication.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityVkladBinding
import com.example.myapplication.models.Balance
import com.example.myapplication.models.UserDeposits
import com.example.myapplication.models.Vklad
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
import kotlin.math.pow

class VkladActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVkladBinding
    private var buttonEnabled : Boolean = false
    private var selectedTerm: Int = 0
    private var procent : Double = 0.0
    private var switchChecked : Boolean = true
    private var srokChecked : Boolean = false
    private var sumChecked : Boolean = false
    private val supabase = createSupabaseClient(
        supabaseUrl = "https://szyrrfgwyygajvfqhojf.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InN6eXJyZmd3eXlnYWp2ZnFob2pmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTQ4OTQwNzMsImV4cCI6MjAzMDQ3MDA3M30.Y40B1B3hYInmcBS6QIWKdrVukI6G5Oo43LoUUcZge04"
    ) {
        install(Postgrest)
        defaultSerializer = KotlinXSerializer(Json)
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("SetTextI18n", "InflateParams", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVkladBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.openButton.setOnClickListener {
            GlobalScope.launch (Dispatchers.Main) {
                val sharedPref = getSharedPreferences("myPref", MODE_PRIVATE)
                val userUUID = sharedPref.getString("userUUID", "none")
                val balanceResponce = supabase.from("Счета_Пользователей").select {
                    filter {
                        if (userUUID != null) {
                            eq("user_uuid", userUUID)
                        }
                    }
                }.decodeSingle<Balance>()

                if (balanceResponce.balance >= binding.sumEditText.getNumericValue()) {
                    val date = SimpleDateFormat("yyyy-MM-dd")
                    val deposit = UserDeposits(user_uuid = userUUID.toString(),
                        id_Deposit_Type = 3,
                        balance = binding.sumEditText.getNumericValue(),
                        start_sum = binding.sumEditText.getNumericValue(),
                        srok = selectedTerm,
                        open_date = date.format(Date()).toString(),
                        stavka = procent)
                    supabase.from("Вклады").insert(deposit)
                    val newBalance = balanceResponce.balance - binding.sumEditText.getNumericValue()
                    supabase.from("Счета_Пользователей").update({
                        set("Баланс", newBalance)
                    }) {
                        filter {
                            if (userUUID != null) {
                                eq("user_uuid", userUUID)
                            }
                        }
                    }
                    finish()
                } else {
                    Toast.makeText(this@VkladActivity, "На балансе недостаточно средств", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.infoButton.setOnClickListener {
            showPopupWindow(it)
        }

        binding.closeButton.setOnClickListener { finish() }
        val buttons = listOf(binding.srok2Button, binding.srok3Button, binding.srok6Button, binding.srok12Button)
        for (button in buttons) {
            val buttonText = button.text.toString()
            val term = buttonText.substringBefore(" ").toInt()
            button.tag = term
            button.setOnClickListener { selectButton(buttons, button) }
        }

        binding.srokEtcButton.setOnClickListener {
            val dialogBinding = layoutInflater.inflate(R.layout.srok_etc_dialog, null)
            val myDialog = Dialog(this@VkladActivity)
            myDialog.setContentView(dialogBinding)
            myDialog.setCancelable(true)
            myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            myDialog.show()

            val editText : EditText = dialogBinding.findViewById(R.id.dialogSrokEditText)
            setEditTextConstraints(editText)
            val readyButton : Button = dialogBinding.findViewById(R.id.dialogReadyButton)
            readyButton.setOnClickListener {
                if (buttonEnabled) {
                    myDialog.dismiss()
                    binding.srokTextView.text = editText.text.toString() + " мес."
                    selectedTerm = editText.text.toString().toInt()
                    procentCalculate()
                }
            }
            val cancelButton : Button = dialogBinding.findViewById(R.id.dialogCancelButton)
            cancelButton.setOnClickListener {
                myDialog.dismiss()
                binding.srokTextView.text = "6 мес."
            }
        }

        binding.sumEditText.addTextChangedListener(object  : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                procentCalculate()
            }

        })

        binding.switchCompat.setOnCheckedChangeListener { _, isChecked ->
            switchChecked = isChecked
            procentCalculate()
        }
    }

    @SuppressLint("InflateParams")
    private fun showPopupWindow(anchorView: View) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_view, null)
        val width = ViewGroup.LayoutParams.WRAP_CONTENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.showAsDropDown(anchorView)
        popupView.setOnClickListener {
            popupWindow.dismiss()
        }
    }

    private fun selectButton(buttons: List<Button>, selectedButton: Button) {
        selectedTerm = selectedButton.tag as Int
        procentCalculate()
        for (button in buttons) {
            if (button == selectedButton) {
                button.setBackgroundResource(R.drawable.srok_selected_button)
                button.setTextColor(Color.WHITE)
                binding.srokTextView.text = button.text.toString()
            } else {
                button.setBackgroundResource(R.drawable.srok_select_bg)
                button.setTextColor(Color.BLACK)
            }
        }
    }

    private fun setEditTextConstraints(editText: EditText) {
        editText.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            if (source.toString().matches("[0-9]+".toRegex())) {
                null
            } else {
                ""
            }
        })

        editText.filters = editText.filters.plus(InputFilter.LengthFilter(2))

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val value = s.toString().toIntOrNull()
                if (value == null || value < 3 || value > 24) {
                    editText.setBackgroundResource(R.drawable.error_edittext)
                    buttonEnabled = false
                } else {
                    editText.setBackgroundResource(R.drawable.custom_edittext)
                    buttonEnabled = true
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    @SuppressLint("SetTextI18n")
    @OptIn(DelicateCoroutinesApi::class)
    private fun procentCalculate() {
        var data : Vklad
        GlobalScope.launch (Dispatchers.Main) {
            if (switchChecked) {
                data = supabase.from("Виды_Вкладов").select(Columns.raw("""
                procents->nepopolnyaemiy->>"2",
                procents->nepopolnyaemiy->>"12",
                procents->nepopolnyaemiy->>"3-6",
                procents->nepopolnyaemiy->>"7-11",
                procents->nepopolnyaemiy->>"13-17",
                procents->nepopolnyaemiy->>"18-24"
            """.trimIndent())) {
                    filter {
                        eq("id", 3)
                    }
                }.decodeSingle<Vklad>()
            } else {
                data = supabase.from("Виды_Вкладов").select(Columns.raw("""
                procents->popolnyaemiy->>"2",
                procents->popolnyaemiy->>"12",
                procents->popolnyaemiy->>"3-6",
                procents->popolnyaemiy->>"7-11",
                procents->popolnyaemiy->>"13-17",
                procents->popolnyaemiy->>"18-24"
            """.trimIndent())) {
                    filter {
                        eq("id", 3)
                    }
                }.decodeSingle<Vklad>()
            }
            when (selectedTerm) {
                2 -> {
                    binding.stavkaTextView.text = data.srok2.toString() + " %"
                    procent = data.srok2
                }
                12 -> {
                    binding.stavkaTextView.text = data.srok12.toString() + " %"
                    procent = data.srok12
                }
                in 3..6 -> {
                    binding.stavkaTextView.text = data.srok3to6.toString() + " %"
                    procent = data.srok3to6
                }
                in 7..11 -> {
                    binding.stavkaTextView.text = data.srok7to11.toString() + " %"
                    procent = data.srok7to11
                }
                in 13..17 -> {
                    binding.stavkaTextView.text = data.srok13to17.toString() + " %"
                    procent = data.srok13to17
                }
                in 18..24 -> {
                    binding.stavkaTextView.text = data.srok18to24.toString() + " %"
                    procent = data.srok18to24
                }
            }

            val dohod = 1.0 + (procent / 100) / 12.0
            val dohod2 = binding.sumEditText.getNumericValue() * dohod.pow(selectedTerm.toDouble())
            binding.dohodTextView.text = dohod2.toInt().toString()
        }
    }
}