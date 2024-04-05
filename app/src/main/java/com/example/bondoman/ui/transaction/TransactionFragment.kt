package com.example.bondoman.ui.transaction

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bondoman.R
import com.example.bondoman.room.TransactionDAO
import com.example.bondoman.room.TransactionDatabase
import com.example.bondoman.room.TransactionEntity
import com.example.bondoman.ui.adapters.TransactionAdapter
import com.example.bondoman.utils.VerticalSpaceItemDecoration
import com.example.bondoman.ui.list_items.TransactionListItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransactionFragment : Fragment() {
    private lateinit var transactionDAO: TransactionDAO

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Create a list of transactions
        transactionDAO = TransactionDatabase.getDatabase(requireContext()).transactionDAO

        val view = inflater.inflate(R.layout.fragment_transaction, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.transaction_recycler_view)

        // Add item decoration for spacing
        val verticalSpacing = resources.getDimensionPixelSize(R.dimen.item_vertical_spacing)
        recyclerView.apply{
            addItemDecoration(VerticalSpaceItemDecoration(verticalSpacing))
        }

        val transactionObserver = Observer<List<TransactionEntity>> { transactions ->
            if (transactions != null) {
                recyclerView.apply {
                    val items = prepareTransactionListItems(transactions)
                    layoutManager = LinearLayoutManager(context)
                    adapter = TransactionAdapter(items)
                }
            }
        }

        // Observe the LiveData
        transactionDAO.getAllTransaction().observe(viewLifecycleOwner, transactionObserver)

        val addButton = view.findViewById<FloatingActionButton>(R.id.add)
        addButton.setOnClickListener {
            val intent = Intent(requireContext(), AddTransaction::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent)
        }

        return view
    }


    fun formatDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())

        val date = inputFormat.parse(dateString)
        val calendarDate = Calendar.getInstance().apply { time = date }

        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        return when {
            // Check if the dates are the same ignoring the time
            calendarDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendarDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"

            calendarDate.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                    calendarDate.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "Yesterday"

            else -> outputFormat.format(date)
        }
    }

    fun prepareTransactionListItems(transactions: List<TransactionEntity>): List<TransactionListItem> {
        val items = mutableListOf<TransactionListItem>()
        var currentDate = ""
        transactions.forEach { transaction ->
            val date = transaction.createdAt
            val dateStr = date.toString().split(" ")[0]
            if (dateStr != currentDate) {
                currentDate = dateStr
                items.add(TransactionListItem.DateHeader(formatDate(dateStr)))
            }
            items.add(TransactionListItem.TransactionItem(transaction))
        }
        return items
    }
}