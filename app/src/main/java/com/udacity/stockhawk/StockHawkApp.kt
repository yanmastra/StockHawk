package com.udacity.stockhawk

import android.app.Application

import com.jakewharton.threetenabp.AndroidThreeTen

import timber.log.Timber

class StockHawkApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)

        if (BuildConfig.DEBUG) {
            Timber.uprootAll()
            Timber.plant(Timber.DebugTree())
        }
    }
}
