package com.example.bondoman.ui.pie_chart

import com.example.bondoman.R
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate

class PieChartFragment() : Fragment(), OnChartValueSelectedListener {
    private var chart: PieChart? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chart = view.findViewById(R.id.pieChart)

        setupChart()
    }

    private fun setupChart() {
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

        // Set dummy data
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(18.5f, 0))
        entries.add(PieEntry(26.7f, 1))

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