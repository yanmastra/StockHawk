package com.udacity.stockhawk.ui


import android.content.Context
import android.database.Cursor
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.udacity.stockhawk.R
import com.udacity.stockhawk.data.Contract
import com.udacity.stockhawk.data.PrefUtils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

import butterknife.BindView
import butterknife.ButterKnife

internal class StockAdapter(private val context: Context, private val clickHandler: StockAdapter.StockAdapterOnClickHandler) : RecyclerView.Adapter<StockAdapter.StockViewHolder>() {
    private val dollarFormatWithPlus: DecimalFormat
    private val dollarFormat: DecimalFormat
    private val percentageFormat: DecimalFormat
    private var cursor: Cursor? = null

    init {

        dollarFormat = NumberFormat.getCurrencyInstance(Locale.US) as DecimalFormat
        dollarFormatWithPlus = NumberFormat.getCurrencyInstance(Locale.US) as DecimalFormat
        dollarFormatWithPlus.positivePrefix = "+$"
        percentageFormat = NumberFormat.getPercentInstance(Locale.getDefault()) as DecimalFormat
        percentageFormat.maximumFractionDigits = 2
        percentageFormat.minimumFractionDigits = 2
        percentageFormat.positivePrefix = "+"
    }

    fun setCursor(cursor: Cursor) {
        this.cursor = cursor
        notifyDataSetChanged()
    }

    fun getSymbolAtPosition(position: Int): String {

        cursor!!.moveToPosition(position)
        return cursor!!.getString(Contract.Quote.POSITION_SYMBOL)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {

        val item = LayoutInflater.from(context).inflate(R.layout.list_item_quote, parent, false)

        return StockViewHolder(item)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {

        cursor!!.moveToPosition(position)


        holder.symbol!!.text = cursor!!.getString(Contract.Quote.POSITION_SYMBOL)
        holder.price!!.text = dollarFormat.format(cursor!!.getFloat(Contract.Quote.POSITION_PRICE).toDouble())


        val rawAbsoluteChange = cursor!!.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE)
        val percentageChange = cursor!!.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE)

        if (rawAbsoluteChange > 0) {
            holder.change!!.setBackgroundResource(R.drawable.percent_change_pill_green)
        } else {
            holder.change!!.setBackgroundResource(R.drawable.percent_change_pill_red)
        }

        val change = dollarFormatWithPlus.format(rawAbsoluteChange.toDouble())
        val percentage = percentageFormat.format((percentageChange / 100).toDouble())

        if (PrefUtils.getDisplayMode(context) == context.getString(R.string.pref_display_mode_absolute_key)) {
            holder.change!!.text = change
        } else {
            holder.change!!.text = percentage
        }


    }

    override fun getItemCount(): Int {
        var count = 0
        if (cursor != null) {
            count = cursor!!.count
        }
        return count
    }


    internal interface StockAdapterOnClickHandler {
        fun onClick(symbol: String)
    }

    internal inner class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        @BindView(R.id.symbol)
        var symbol: TextView? = null

        @BindView(R.id.price)
        var price: TextView? = null

        @BindView(R.id.change)
        var change: TextView? = null

        init {
            ButterKnife.bind(this, itemView)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val adapterPosition = adapterPosition
            cursor!!.moveToPosition(adapterPosition)
            val symbolColumn = cursor!!.getColumnIndex(Contract.Quote.COLUMN_SYMBOL)
            clickHandler.onClick(cursor!!.getString(symbolColumn))

        }


    }
}
