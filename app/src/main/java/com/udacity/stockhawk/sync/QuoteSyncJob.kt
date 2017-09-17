package com.udacity.stockhawk.sync

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.icu.math.BigDecimal
import android.icu.text.SimpleDateFormat
import android.net.ConnectivityManager
import android.net.NetworkInfo

import com.udacity.stockhawk.data.Contract
import com.udacity.stockhawk.data.PrefUtils

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit

import java.io.IOException
import java.util.ArrayList
import java.util.Calendar
import java.util.HashSet
import java.util.concurrent.TimeUnit

import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber

object QuoteSyncJob {

    private val ONE_OFF_ID = 2
    private val ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED"
    private val PERIOD = 300000
    private val INITIAL_BACKOFF = 10000
    private val PERIODIC_ID = 1
    private val YEARS_OF_HISTORY = 2

    private val QUANDL_ROOT = "https://www.quandl.com/api/v3/datasets/WIKI/"

    private val client = OkHttpClient()
    private val startDate = LocalDate.now().minus(YEARS_OF_HISTORY.toLong(), ChronoUnit.YEARS)
    private val endDate = LocalDate.now()
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private var historyBuilder = StringBuilder()

    internal fun createQuery(symbol: String): HttpUrl {

        val httpUrl = HttpUrl.parse(QUANDL_ROOT + symbol + ".json")!!.newBuilder()
        httpUrl.addQueryParameter("column_index", "4")  //closing price
                .addQueryParameter("start_date", formatter.format(startDate))
                .addQueryParameter("end_date", formatter.format(endDate))
        return httpUrl.build()
    }

    @Throws(JSONException::class)
    internal fun processStock(jsonObject: JSONObject): ContentValues {

        val stockSymbol = jsonObject.getString("dataset_code")

        val historicData = jsonObject.getJSONArray("data")

        val price = historicData.getJSONArray(0).getDouble(1)
        val change = price - historicData.getJSONArray(1).getDouble(1)
        val percentChange = 100 * ((price - historicData.getJSONArray(1).getDouble(1)) / historicData.getJSONArray(1).getDouble(1))

        historyBuilder = StringBuilder()

        for (i in 0..historicData.length() - 1) {
            val array = historicData.getJSONArray(i)
            // Append date
            historyBuilder.append(array.get(0))
            historyBuilder.append(", ")
            // Append close
            historyBuilder.append(array.getDouble(1))
            historyBuilder.append("\n")
        }

        val quoteCV = ContentValues()
        quoteCV.put(Contract.Quote.COLUMN_SYMBOL, stockSymbol)
        quoteCV.put(Contract.Quote.COLUMN_PRICE, price)
        quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange)
        quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change)
        quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString())

        return quoteCV
    }

    internal fun getQuotes(context: Context) {

        Timber.d("Running sync job")

        historyBuilder = StringBuilder()

        try {

            val stockPref = PrefUtils.getStocks(context)

            for (stock in stockPref) {
                val request = Request.Builder()
                        .url(createQuery(stock)).build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Timber.e("OKHTTP", e.message)
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        try {
                            val body = response.body()!!.string()
                            val jsonObject = JSONObject(body)
                            val quotes = processStock(jsonObject.getJSONObject("dataset"))

                            context.contentResolver.insert(Contract.Quote.URI, quotes)
                        } catch (ex: JSONException) {
                        }

                    }
                })


            }

            val dataUpdatedIntent = Intent(ACTION_DATA_UPDATED)
            context.sendBroadcast(dataUpdatedIntent)

        } catch (exception: Exception) {
            Timber.e(exception, "Error fetching stock quotes")
        }

    }

    private fun schedulePeriodic(context: Context) {
        Timber.d("Scheduling a periodic task")


        val builder = JobInfo.Builder(PERIODIC_ID, ComponentName(context, QuoteJobService::class.java))


        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD.toLong())
                .setBackoffCriteria(INITIAL_BACKOFF.toLong(), JobInfo.BACKOFF_POLICY_EXPONENTIAL)


        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        scheduler.schedule(builder.build())
    }


    @Synchronized fun initialize(context: Context) {

        schedulePeriodic(context)
        syncImmediately(context)

    }

    @Synchronized fun syncImmediately(context: Context) {

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnectedOrConnecting) {
            val nowIntent = Intent(context, QuoteIntentService::class.java)
            context.startService(nowIntent)
        } else {

            val builder = JobInfo.Builder(ONE_OFF_ID, ComponentName(context, QuoteJobService::class.java))


            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF.toLong(), JobInfo.BACKOFF_POLICY_EXPONENTIAL)


            val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

            scheduler.schedule(builder.build())


        }
    }


}
