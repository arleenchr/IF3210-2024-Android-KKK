package com.example.bondoman.ui.adapters
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bondoman.R
import com.example.bondoman.ui.transaction.ViewTransaction
import com.example.bondoman.room.TransactionEntity
import com.example.bondoman.ui.list_items.TransactionListItem
import java.text.NumberFormat
import java.util.Locale

class TransactionAdapter(private val items: List<TransactionListItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_DATE_HEADER = 0
        private const val TYPE_TRANSACTION = 1
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is TransactionListItem.DateHeader -> TYPE_DATE_HEADER
        is TransactionListItem.TransactionItem -> TYPE_TRANSACTION
        else -> {
            throw IllegalArgumentException("Invalid type of data $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_DATE_HEADER -> DateHeaderViewHolder(inflater.inflate(R.layout.item_date_header, parent, false))
            else -> TransactionViewHolder(inflater.inflate(R.layout.item_transaction, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is TransactionListItem.DateHeader -> (holder as DateHeaderViewHolder).bind(item.date)
            is TransactionListItem.TransactionItem -> (holder as TransactionViewHolder).bind(item.transaction)
        }
    }
    override fun getItemCount() = items.size

    class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(date: String) {
            itemView.findViewById<TextView>(R.id.tvDateHeader).text = date
        }
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var icon: ImageView = itemView.findViewById(R.id.transaction_icon)
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        var tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        var tvLocation: TextView = itemView.findViewById(R.id.tvLocation)

        fun bind(transaction: TransactionEntity) {
            icon.setImageResource(
                if (transaction.category == "Income") {
                    R.drawable.ic_income_item
                } else {
                    R.drawable.ic_expense_item
                }
            )

            var title = transaction.title
            if (title != null){
                if (title.length > 15){
                    title = title.substring(0,15) + "..."
                }
            }
            tvTitle.text = title
            tvCategory.text = transaction.category
            tvAmount.text = "Rp${NumberFormat.getNumberInstance(Locale("in", "ID")).format(transaction.amount)}"
            if (transaction.category == "Income") {
                tvAmount.setTextColor(tvAmount.context.getColor(R.color.green_500))
            } else {
                tvAmount.text = "-${tvAmount.text}"
                tvAmount.setTextColor(tvAmount.context.getColor(R.color.gray_300))
            }

            var locationName = transaction.location.name
            if (locationName != null) {
                if (locationName.length > 15) {
                    locationName = locationName.substring(0, 15) + "..."
                }
            }
            tvLocation.text = locationName

            // Set OnClickListener to start ViewTransaction activity when item is clicked
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, ViewTransaction::class.java)
                val transactionId = transaction.id
                intent.putExtra("id", transactionId)
                context.startActivity(intent)
            }
        }
    }
}