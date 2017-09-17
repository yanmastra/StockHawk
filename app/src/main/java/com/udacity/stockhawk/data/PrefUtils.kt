package com.udacity.stockhawk.data

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

import com.udacity.stockhawk.R

import java.util.Arrays
import java.util.HashSet

object PrefUtils {

    fun getStocks(context: Context): MutableSet<String> {
        val stocksKey = context.getString(R.string.pref_stocks_key)
        val initializedKey = context.getString(R.string.pref_stocks_initialized_key)
        val defaultStocksList = context.resources.getStringArray(R.array.default_stocks)

        val defaultStocks = HashSet(Arrays.asList(*defaultStocksList))
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)


        val initialized = prefs.getBoolean(initializedKey, false)

        if (!initialized) {
            val editor = prefs.edit()
            editor.putBoolean(initializedKey, true)
            editor.putStringSet(stocksKey, defaultStocks)
            editor.apply()
            return defaultStocks
        }
        return prefs.getStringSet(stocksKey, HashSet<String>())

    }

    private fun editStockPref(context: Context, symbol: String, add: Boolean?) {
        val key = context.getString(R.string.pref_stocks_key)
        val stocks = getStocks(context)

        if (add!!) {
            stocks.add(symbol)
        } else {
            stocks.remove(symbol)
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.putStringSet(key, stocks)
        editor.apply()
    }

    fun addStock(context: Context, symbol: String) {
        editStockPref(context, symbol, true)
    }

    fun removeStock(context: Context, symbol: String) {
        editStockPref(context, symbol, false)
    }

    fun getDisplayMode(context: Context): String {
        val key = context.getString(R.string.pref_display_mode_key)
        val defaultValue = context.getString(R.string.pref_display_mode_default)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(key, defaultValue)
    }

    fun toggleDisplayMode(context: Context) {
        val key = context.getString(R.string.pref_display_mode_key)
        val absoluteKey = context.getString(R.string.pref_display_mode_absolute_key)
        val percentageKey = context.getString(R.string.pref_display_mode_percentage_key)

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val displayMode = getDisplayMode(context)

        val editor = prefs.edit()

        if (displayMode == absoluteKey) {
            editor.putString(key, percentageKey)
        } else {
            editor.putString(key, absoluteKey)
        }

        editor.apply()
    }

}
