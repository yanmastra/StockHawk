package com.udacity.stockhawk.data


import android.net.Uri
import android.provider.BaseColumns

import com.google.common.collect.ImmutableList
import android.provider.ContactsContract





object Contract {

    internal val AUTHORITY = "com.udacity.stockhawk"
    internal val PATH_QUOTE = "quote"
    internal val PATH_QUOTE_WITH_SYMBOL = "quote/*"
    private val BASE_URI = Uri.parse("content://" + AUTHORITY)

    class Quote : BaseColumns {
        companion object {
            val _ID = BaseColumns._ID
            val URI = BASE_URI.buildUpon().appendPath(PATH_QUOTE).build()
            val COLUMN_SYMBOL = "symbol"
            val COLUMN_PRICE = "price"
            val COLUMN_ABSOLUTE_CHANGE = "absolute_change"
            val COLUMN_PERCENTAGE_CHANGE = "percentage_change"
            val COLUMN_HISTORY = "history"
            val POSITION_ID = 0
            val POSITION_SYMBOL = 1
            val POSITION_PRICE = 2
            val POSITION_ABSOLUTE_CHANGE = 3
            val POSITION_PERCENTAGE_CHANGE = 4
            val POSITION_HISTORY = 5
            val QUOTE_COLUMNS = arrayOf(BaseColumns._ID, COLUMN_SYMBOL, COLUMN_PRICE, COLUMN_ABSOLUTE_CHANGE, COLUMN_PERCENTAGE_CHANGE, COLUMN_HISTORY)
            internal val TABLE_NAME = "quotes"

            fun makeUriForStock(symbol: String): Uri {
                return URI.buildUpon().appendPath(symbol).build()
            }

            internal fun getStockFromUri(queryUri: Uri): String {
                return queryUri.lastPathSegment
            }
        }


    }

}
