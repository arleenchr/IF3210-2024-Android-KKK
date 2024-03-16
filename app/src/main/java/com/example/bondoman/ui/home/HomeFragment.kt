package com.example.bondoman.ui.home

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bondoman.R
import com.example.bondoman.models.Transaction
import com.example.bondoman.ui.adapters.TransactionAdapter
import com.example.bondoman.ui.transaction.TransactionFragment
import com.google.android.libraries.places.api.model.Place
import java.sql.Timestamp

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

        // TODO: Replace with actual data from the database
        val place = Place.builder()
            .setAddress("123 Main St")
            .setName("Grocery Store")
            .build()

        val transactions = listOf(
            Transaction("Groceries", 100000, "Food", place, Timestamp(System.currentTimeMillis())),
            Transaction("Gas", 5000, "Transportation", place, Timestamp(System.currentTimeMillis())),
            Transaction("Coffee", 500, "Food", place, Timestamp(System.currentTimeMillis())),
        )

        val recyclerView: RecyclerView = view.findViewById(R.id.transaction_recycler_view)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = TransactionAdapter(transactions)
        }

        // Find the see all button
        val seeAllButton: TextView = view.findViewById(R.id.see_all_button)

        // Set OnClickListener for the see all button to navigate to the TransactionFragment
        seeAllButton.setOnClickListener {
            val transactionFragment = TransactionFragment()
            val transactionFragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            transactionFragmentTransaction.replace(R.id.container, transactionFragment)
            transactionFragmentTransaction.addToBackStack(null)
            transactionFragmentTransaction.commit()
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