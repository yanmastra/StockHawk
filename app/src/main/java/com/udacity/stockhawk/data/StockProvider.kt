package com.udacity.stockhawk.data

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri


class StockProvider : ContentProvider() {

    private var dbHelper: DbHelper? = null


    override fun onCreate(): Boolean {
        dbHelper = DbHelper(context)
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val returnCursor: Cursor
        val db = dbHelper!!.readableDatabase

        when (uriMatcher.match(uri)) {
            QUOTE -> returnCursor = db.query(
                    Contract.Quote.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs, null, null,
                    sortOrder
            )

            QUOTE_FOR_SYMBOL -> returnCursor = db.query(
                    Contract.Quote.TABLE_NAME,
                    projection,
                    Contract.Quote.COLUMN_SYMBOL + " = ?",
                    arrayOf(Contract.Quote.getStockFromUri(uri)), null, null,
                    sortOrder
            )
            else -> throw UnsupportedOperationException("Unknown URI:" + uri)
        }

        val context = context
        if (context != null) {
            returnCursor.setNotificationUri(context.contentResolver, uri)
        }

        return returnCursor
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = dbHelper!!.writableDatabase
        val returnUri: Uri

        when (uriMatcher.match(uri)) {
            QUOTE -> {
                db.insert(
                        Contract.Quote.TABLE_NAME, null,
                        values
                )
                returnUri = Contract.Quote.URI
            }
            else -> throw UnsupportedOperationException("Unknown URI:" + uri)
        }

        val context = context
        if (context != null) {
            context.contentResolver.notifyChange(uri, null)
        }

        return returnUri
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        var selection = selection
        val db = dbHelper!!.writableDatabase
        val rowsDeleted: Int

        if (null == selection) {
            selection = "1"
        }
        when (uriMatcher.match(uri)) {
            QUOTE -> rowsDeleted = db.delete(
                    Contract.Quote.TABLE_NAME,
                    selection,
                    selectionArgs
            )

            QUOTE_FOR_SYMBOL -> {
                val symbol = Contract.Quote.getStockFromUri(uri)
                rowsDeleted = db.delete(
                        Contract.Quote.TABLE_NAME,
                        '"' + symbol + '"' + " =" + Contract.Quote.COLUMN_SYMBOL,
                        selectionArgs
                )
            }
            else -> throw UnsupportedOperationException("Unknown URI:" + uri)
        }

        if (rowsDeleted != 0) {
            val context = context
            if (context != null) {
                context.contentResolver.notifyChange(uri, null)
            }
        }

        return rowsDeleted
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun bulkInsert(uri: Uri, values: Array<ContentValues>): Int {

        val db = dbHelper!!.writableDatabase

        when (uriMatcher.match(uri)) {
            QUOTE -> {
                db.beginTransaction()
                val returnCount = 0
                try {
                    for (value in values) {
                        db.insert(
                                Contract.Quote.TABLE_NAME, null,
                                value
                        )
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }

                val context = context
                if (context != null) {
                    context.contentResolver.notifyChange(uri, null)
                }

                return returnCount
            }
            else -> return super.bulkInsert(uri, values)
        }


    }

    companion object {

        private val QUOTE = 100
        private val QUOTE_FOR_SYMBOL = 101

        private val uriMatcher = buildUriMatcher()

        private fun buildUriMatcher(): UriMatcher {
            val matcher = UriMatcher(UriMatcher.NO_MATCH)
            matcher.addURI(Contract.AUTHORITY, Contract.PATH_QUOTE, QUOTE)
            matcher.addURI(Contract.AUTHORITY, Contract.PATH_QUOTE_WITH_SYMBOL, QUOTE_FOR_SYMBOL)
            return matcher
        }
    }
}
