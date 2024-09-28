package com.example.myapplication.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.Transaction
import java.text.DecimalFormat

class TransactionHistoryAdapter(val mList : List<Transaction>, val context: Context) : RecyclerView.Adapter<TransactionHistoryAdapter.ViewHolder> (){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionHistoryAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_list, parent, false)
        return ViewHolder(view)
    }
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TransactionHistoryAdapter.ViewHolder, position: Int) {
        when (mList[position].id_transaction_type) {
            1 -> holder.transactionTypeTextView.text = "Пополнение"
            2 -> holder.transactionTypeTextView.text = "Снятие"
        }
        holder.transactionDateTextView.text = mList[position].date
        holder.transactionSumTextView.text = formatter(mList[position].sum) + " ₽"
        when (mList[position].name) {
            1 -> holder.transactionNameTextView.text = "Накопительный счет"
            2 -> holder.transactionNameTextView.text = "Инвесткопилка"
            3 -> holder.transactionNameTextView.text = "Вклад"
        }
    }
    override fun getItemCount(): Int { return mList.size }
    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val transactionTypeTextView : TextView = itemView.findViewById(R.id.transactionTypeTextView)
        val transactionDateTextView : TextView = itemView.findViewById(R.id.transactionDateTextView)
        val transactionSumTextView : TextView = itemView.findViewById(R.id.transactionSumTextView)
        val transactionNameTextView : TextView = itemView.findViewById(R.id.transactionNameTextView)
    }
    private fun formatter(n: Double) : String =
        DecimalFormat("#,###")
            .format(n)
            .replace(",", " ")
}