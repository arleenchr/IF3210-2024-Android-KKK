package com.example.bondoman.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bondoman.models.Transaction
import com.example.bondoman.R
import com.example.bondoman.ViewTransaction

class TransactionAdapter(private val transactions: List<Transaction>) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        var tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        var tvLocation: TextView = itemView.findViewById(R.id.tvLocation)

        init {
            // Set OnClickListener to start ViewTransaction activity when item is clicked
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, ViewTransaction::class.java)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.tvTitle.text = transaction.title
        holder.tvCategory.text = transaction.category
        holder.tvAmount.text = "$${transaction.amount}"
        holder.tvLocation.text = transaction.location.getName()?.toString()
    }

    override fun getItemCount(): Int = transactions.size
}