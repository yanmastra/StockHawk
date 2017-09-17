package com.udacity.stockhawk.sync

import android.app.IntentService
import android.content.Intent

import timber.log.Timber


class QuoteIntentService : IntentService(QuoteIntentService::class.java.simpleName) {

    override fun onHandleIntent(intent: Intent?) {
        Timber.d("Intent handled")
        QuoteSyncJob.getQuotes(applicationContext)
    }
}
