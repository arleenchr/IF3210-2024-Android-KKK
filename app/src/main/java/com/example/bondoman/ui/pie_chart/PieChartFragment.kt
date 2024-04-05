package com.example.bondoman.ui.pie_chart

import com.example.bondoman.R
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bondoman.room.TransactionDAO
import com.example.bondoman.room.TransactionDatabase
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PieChartFragment() : Fragment(), OnChartValueSelectedListener {
    private var chart: PieChart? = null
    private lateinit var transactionDAO: TransactionDAO
    private lateinit var chartViewModel: PieChartViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        transactionDAO = TransactionDatabase.getDatabase(requireContext()).transactionDAO
        chartViewModel = ViewModelProvider(this, PieChartViewModelFactory(TransactionDatabase.getDatabase(requireContext()).transactionDAO)).get(PieChartViewModel::class.java)

        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chart = view.findViewById(R.id.pieChart)

        setupObservers(view)
        setupChart(view)
    }

    private fun setupObservers(view: View) {
        val incomeIndicator = view.findViewById<TextView>(R.id.tvIncomeIndicator)
        val expenseIndicator = view.findViewById<TextView>(R.id.tvExpenseIndicator)

        chartViewModel.hadIncomeTransactionsLastMonth().observe(viewLifecycleOwner) { hadIncome ->
            incomeIndicator?.visibility = if (hadIncome) View.VISIBLE else View.GONE
        }

        chartViewModel.hadExpenseTransactionsLastMonth().observe(viewLifecycleOwner) { hadExpense ->
            expenseIndicator?.visibility = if (hadExpense) View.VISIBLE else View.GONE
        }

        chartViewModel.calculateMonthlyGrowth("Income").observe(viewLifecycleOwner) { growth ->
            if (growth != null) {
                val growthString = formatGrowth(growth)
                incomeIndicator?.text = "$growthString from last month"
            }
        }

        chartViewModel.calculateMonthlyGrowth("Expense").observe(viewLifecycleOwner) { growth ->
            if (growth != null) {
                val growthString = formatGrowth(growth)
                expenseIndicator?.text = "$growthString from last month"
            }
        }
    }

    private fun formatGrowth(growth: Double): String =
        String.format(Locale.getDefault(), "%.2f%%", growth)


    private fun setupChart(view: View) {
        chart?.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            setExtraOffsets(5f, 10f, 5f, 5f)
            setDragDecelerationFrictionCoef(0.95f)

            isDrawHoleEnabled = false
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true

            setOnChartValueSelectedListener(this@PieChartFragment)

            animateY(1400, Easing.EaseInOutQuad)

            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
        }

        val dateTextView = view.findViewById<TextView>(R.id.dateTextView)

        // Get current month and year
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate)

        dateTextView.text = formattedDate

        // Get data
        transactionDAO.getTransactionStats().observe(viewLifecycleOwner) { transaction ->
            transaction?.let {
                val entries = ArrayList<PieEntry>()
                entries.add(PieEntry(transaction.totalIncome.toFloat(), 0))
                entries.add(PieEntry(transaction.totalExpense.toFloat(), 1))

                val incomeText = view.findViewById<TextView>(R.id.incomeText)
                incomeText?.text = getString(R.string.rp, NumberFormat.getNumberInstance(Locale("in", "ID")).format(transaction.totalIncome))

                val expenseText = view.findViewById<TextView>(R.id.expenseText)
                expenseText?.text = getString(R.string.rp, NumberFormat.getNumberInstance(Locale("in", "ID")).format(transaction.totalExpense))

                val dataSet = com.github.mikephil.charting.data.PieDataSet(entries, "Results")
                dataSet.setDrawIcons(false)
                dataSet.sliceSpace = 3f
                dataSet.selectionShift = 5f
                dataSet.iconsOffset = com.github.mikephil.charting.utils.MPPointF(0f, 40f)

                val colors = ArrayList<Int>()
                colors.add(Color.rgb(34, 197, 94))
                colors.add(Color.rgb(249, 115, 22))
                dataSet.colors = colors

                val data = com.github.mikephil.charting.data.PieData(dataSet)
                data.setValueFormatter(com.github.mikephil.charting.formatter.PercentFormatter(chart))
                data.setValueTextSize(11f)
                data.setValueTextColor(Color.WHITE)

                chart?.highlightValues(null)
                chart?.data = data
                chart?.invalidate()

                // Disable legend
                val l: Legend = chart!!.legend
                l.isEnabled = false

                // Set the font
                val tf = context?.let { context ->
                    ResourcesCompat.getFont(context, R.font.urbanist_regular)
                }
                chart?.setEntryLabelTypeface(tf)
            }
        }
    }

    override fun onValueSelected(e: Entry, h: Highlight) {
    Log.i(
            "VAL SELECTED",
            "Value: " + e.y + ", index: " + h.x
                    + ", DataSet index: " + h.dataSetIndex
        )
    }

    override fun onNothingSelected() {
        Log.i("PieChart", "nothing selected")
    }
}