package com.udacity.stockhawk.ui

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import butterknife.BindView
import butterknife.ButterKnife
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.udacity.stockhawk.R
import timber.log.Timber

import java.util.ArrayList
import java.util.Arrays

import com.udacity.stockhawk.data.Contract.Quote

class StockDetailActivity : AppCompatActivity() {

    @BindView(R.id.chart)
    internal var chart: LineChart? = null

    lateinit var stock: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_detail)

        ButterKnife.bind(this)

        val intent = intent
        stock = intent.extras.getString("stock")

        title = stock

        val uri = Quote.makeUriForStock(stock)

        val c = contentResolver.query(uri, Quote.QUOTE_COLUMNS, null, null, null)
        c!!.moveToFirst()
        val history = c.getString(c.getColumnIndexOrThrow(Quote.COLUMN_HISTORY))
        Timber.d(TAG, history)

        if (history != null) {
            val entries = ArrayList<Entry>()
            var i = 0
            val historyData = history.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            // The items are stored in the database in descending order by data
            // Grab only the most recent 30 items
            val recentItems = Arrays.asList(*historyData).subList(0, 29)

            Timber.d(recentItems.toString())

            for (rawEntry in recentItems) {
                val entry = rawEntry.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                entries.add(Entry(java.lang.Float.valueOf(i++.toFloat())!!, java.lang.Float.valueOf(entry[1])!!))

            }

            val dataSet = LineDataSet(entries, "Stock Price")

            val lineData = LineData(dataSet)

            chart!!.data = lineData
            chart!!.description = Description()
            chart!!.isDragEnabled = true
            chart!!.setScaleEnabled(true)
            chart!!.xAxis.setDrawLabels(false)
            chart!!.invalidate()
        }
    }

    companion object {
        private val TAG = StockDetailActivity::class.java.toString()
    }


}
