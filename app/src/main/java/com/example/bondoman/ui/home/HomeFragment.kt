package com.example.bondoman.ui.home

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bondoman.MainActivity
import com.example.bondoman.R
import com.example.bondoman.room.TransactionDAO
import com.example.bondoman.room.TransactionDatabase
import com.example.bondoman.room.TransactionEntity
import com.example.bondoman.ui.adapters.RecentTransactionAdapter
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment() {
    private lateinit var transactionDAO: TransactionDAO

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val sharedPreferences = requireContext().getSharedPreferences("identity", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "")

        val textView: TextView = view.findViewById(R.id.textView12)

        textView.text = username

        transactionDAO = TransactionDatabase.getDatabase(requireContext()).transactionDAO

        val recyclerView: RecyclerView = view.findViewById(R.id.transaction_recycler_view)

        val transactionObserver = Observer<List<TransactionEntity>> { transactions ->
            if (transactions != null) {
                recyclerView.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = RecentTransactionAdapter(transactions)
                }
            }
        }

        // Observe the LiveData
        transactionDAO.getTopTransaction().observe(viewLifecycleOwner, transactionObserver)

        // Find the see all button
        val seeAllButton: TextView = view.findViewById(R.id.see_all_button)

        // Set OnClickListener for the see all button to navigate to the TransactionFragment
        seeAllButton.setOnClickListener {
            (requireActivity() as MainActivity).bottomNav.selectedItemId = R.id.transaction
        }

        transactionDAO.getTransactionStats().observe(viewLifecycleOwner) { transaction ->
            transaction?.let {
                val incomeTextView = view.findViewById<TextView>(R.id.income_home_value)
                incomeTextView.text = getString(R.string.rp, NumberFormat.getNumberInstance(Locale("in", "ID")).format(transaction.totalIncome))

                val expenseTextView = view.findViewById<TextView>(R.id.expense_home_value)
                expenseTextView.text = getString(R.string.rp, NumberFormat.getNumberInstance(Locale("in", "ID")).format(transaction.totalExpense))
            }
        }

        return view
    }
}