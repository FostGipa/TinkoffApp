package com.example.myapplication.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.activities.DepositDescriptionActivity
import com.example.myapplication.models.UserDeposits
import java.text.DecimalFormat

class DepositsAdapter(private val mList : List<UserDeposits>, val context: Context) : RecyclerView.Adapter<DepositsAdapter.ViewHolder>() {
    private var name = ""
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.deposits_list, parent, false)
        return ViewHolder(view)
    }
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.balanceTextView.text = formatter(mList[position].balance) + " ₽"
        when (mList[position].id_Deposit_Type) {
            1 -> { name = "Накопительный счет" }
            2 -> { name = "Инвесткопилка" }
            3 -> { name = "Вклад" }
        }
        holder.nameTextView.text = name
        holder.cardView.setOnClickListener {
            context.startActivity(Intent(context, DepositDescriptionActivity::class.java)
                .putExtra("depositName", name)
                .putExtra("depositBalance", mList[position].balance)
                .putExtra("depositStavka", mList[position].stavka)
                .putExtra("depositId", mList[position].id))
        }
    }
    override fun getItemCount(): Int { return mList.size }
    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val balanceTextView : TextView = itemView.findViewById(R.id.depositBalanceTextView)
        val nameTextView : TextView = itemView.findViewById(R.id.depositNameTextView)
        val cardView : CardView = itemView.findViewById(R.id.cardView)
    }
    private fun formatter(n: Double) : String =
        DecimalFormat("#,###")
            .format(n)
            .replace(",", " ")
}