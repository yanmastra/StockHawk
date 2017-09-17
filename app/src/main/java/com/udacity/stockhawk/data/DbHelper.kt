package com.udacity.stockhawk.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import com.udacity.stockhawk.data.Contract.Quote


internal class DbHelper(context: Context) : SQLiteOpenHelper(context, DbHelper.NAME, null, DbHelper.VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val builder = "CREATE TABLE " + Quote.TABLE_NAME + " (" +
        Quote._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        Quote.COLUMN_SYMBOL + " TEXT NOT NULL, " +
        Quote.COLUMN_PRICE + " REAL NOT NULL, " +
        Quote.COLUMN_ABSOLUTE_CHANGE + " REAL NOT NULL, " +
        Quote.COLUMN_PERCENTAGE_CHANGE + " REAL NOT NULL, " +
        Quote.COLUMN_HISTORY + " TEXT NOT NULL, " +
        "UNIQUE (" + Quote.COLUMN_SYMBOL + ") ON CONFLICT REPLACE);"

        db.execSQL(builder)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        db.execSQL(" DROP TABLE IF EXISTS " + Quote.TABLE_NAME)

        onCreate(db)
    }

    companion object {


        private val NAME = "StockHawk.db"
        private val VERSION = 1
    }
}
