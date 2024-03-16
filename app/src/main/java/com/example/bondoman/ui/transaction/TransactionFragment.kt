package com.example.bondoman.ui.transaction

import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bondoman.R
import com.example.bondoman.models.Transaction
import com.example.bondoman.ui.adapters.TransactionAdapter
import com.example.bondoman.utils.VerticalSpaceItemDecoration
import java.sql.Timestamp

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TransactionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TransactionFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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

        // Create a list of transactions
        // TODO: Replace with actual data from the database
        val transactions = listOf(
            Transaction("Groceries", 100000, "Food", Location("Walmart"), Timestamp(System.currentTimeMillis())),
            Transaction("Gas", 5000, "Transportation", Location("Shell"), Timestamp(System.currentTimeMillis())),
            Transaction("Coffee", 500, "Food", Location("Starbucks"), Timestamp(System.currentTimeMillis())),
            Transaction("Lunch", 1500, "Food", Location("Chipotle"), Timestamp(System.currentTimeMillis())),
            Transaction("Dinner", 3000, "Food", Location("Chick-fil-A"), Timestamp(System.currentTimeMillis())),
            Transaction("Uber", 2000, "Transportation", Location("Uber"), Timestamp(System.currentTimeMillis())),
            Transaction("Lyft", 2500, "Transportation", Location("Lyft"), Timestamp(System.currentTimeMillis()))
        )

        val view = inflater.inflate(R.layout.fragment_transaction, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.transaction_recycler_view)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = TransactionAdapter(transactions)

            // Add item decoration for spacing
            val verticalSpacing = resources.getDimensionPixelSize(R.dimen.item_vertical_spacing)
            addItemDecoration(VerticalSpaceItemDecoration(verticalSpacing))
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
         * @return A new instance of fragment TransactionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TransactionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}