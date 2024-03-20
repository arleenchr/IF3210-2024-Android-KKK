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
import com.example.bondoman.models.Transaction
import com.example.bondoman.room.TransactionDAO
import com.example.bondoman.room.TransactionDatabase
import com.example.bondoman.room.TransactionEntity
import com.example.bondoman.ui.adapters.RecentTransactionAdapter
import com.example.bondoman.ui.adapters.TransactionAdapter
import com.example.bondoman.ui.transaction.TransactionFragment
import com.google.android.libraries.places.api.model.Place
import java.sql.Timestamp
import java.text.NumberFormat
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var transactionDAO: TransactionDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Retrieve text from SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("identity", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "")

        // Find the TextView
        val textView: TextView = view.findViewById(R.id.textView12)

        // Set text to the TextView
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}