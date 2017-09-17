package com.udacity.stockhawk.sync

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent

import timber.log.Timber

class QuoteJobService : JobService() {


    override fun onStartJob(jobParameters: JobParameters): Boolean {
        Timber.d("Intent handled")
        val nowIntent = Intent(applicationContext, QuoteIntentService::class.java)
        applicationContext.startService(nowIntent)
        return true
    }

    override fun onStopJob(jobParameters: JobParameters): Boolean {
        return false
    }


}
