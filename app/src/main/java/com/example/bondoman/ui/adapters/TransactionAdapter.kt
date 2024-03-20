package com.example.bondoman.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bondoman.R
import com.example.bondoman.ViewTransaction
import com.example.bondoman.room.TransactionEntity

class TransactionAdapter(private val transactions: List<TransactionEntity>) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View, transactions: List<TransactionEntity>) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        var tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        var tvLocation: TextView = itemView.findViewById(R.id.tvLocation)

        init {
            // Set OnClickListener to start ViewTransaction activity when item is clicked
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, ViewTransaction::class.java)
                val transactionId = transactions[adapterPosition].id
                intent.putExtra("id", transactionId)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view, transactions)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.tvTitle.text = transaction.title
        holder.tvCategory.text = transaction.category
        holder.tvAmount.text = "Rp${transaction.amount}"

        var locationName = transaction.location.name
        if (locationName != null) {
            if (locationName.length > 25) {
                locationName = locationName.substring(0, 25) + "..."
            }
        }
        holder.tvLocation.text = locationName
    }

    override fun getItemCount(): Int = transactions.size
}