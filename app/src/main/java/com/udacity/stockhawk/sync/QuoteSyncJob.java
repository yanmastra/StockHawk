package com.udacity.stockhawk.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.icu.math.BigDecimal;
import android.icu.text.SimpleDateFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public final class QuoteSyncJob {

    private static final int ONE_OFF_ID = 2;
    private static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    private static final int YEARS_OF_HISTORY = 2;

    private static final String QUANDL_ROOT = "https://www.quandl.com/api/v3/datasets/WIKI/";

    private static final OkHttpClient client = new OkHttpClient();
    private static final LocalDate startDate = LocalDate.now().minus(YEARS_OF_HISTORY, ChronoUnit.YEARS);
    private static final LocalDate endDate = LocalDate.now();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static StringBuilder historyBuilder = new StringBuilder();



    private QuoteSyncJob() { }

    static HttpUrl createQuery(String symbol) {

        HttpUrl.Builder httpUrl = HttpUrl.parse(QUANDL_ROOT+symbol+".json").newBuilder();
        httpUrl.addQueryParameter("column_index", "4")  //closing price
                .addQueryParameter("start_date", formatter.format(startDate))
                .addQueryParameter("end_date", formatter.format(endDate));
        return httpUrl.build();
    }

    static ContentValues processStock(JSONObject jsonObject) throws JSONException{

        String stockSymbol = jsonObject.getString("dataset_code");

        JSONArray historicData = jsonObject.getJSONArray("data");

        double price = historicData.getJSONArray(0).getDouble(1);
        double change = price - historicData.getJSONArray(1).getDouble(1);
        double percentChange = 100 * (( price - historicData.getJSONArray(1).getDouble(1) ) / historicData.getJSONArray(1).getDouble(1));

        historyBuilder = new StringBuilder();

        for (int i = 0; i<historicData.length(); i++) {
            JSONArray array = historicData.getJSONArray(i);
            // Append date
            historyBuilder.append(array.get(0));
            historyBuilder.append(", ");
            // Append close
            historyBuilder.append(array.getDouble(1));
            historyBuilder.append("\n");
        }

        ContentValues quoteCV = new ContentValues();
        quoteCV.put(Contract.Quote.COLUMN_SYMBOL, stockSymbol);
        quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
        quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
        quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);
        quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());

        return quoteCV;
    }

    static void getQuotes(final Context context) {

        Timber.d("Running sync job");

        historyBuilder = new StringBuilder();

        try {

            Set<String> stockPref = PrefUtils.getStocks(context);

            for (String stock : stockPref) {
                Request request = new Request.Builder()
                        .url(createQuery(stock)).build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Timber.e("OKHTTP", e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String body = response.body().string();
                            JSONObject jsonObject = new JSONObject(body);
                            ContentValues quotes = processStock(jsonObject.getJSONObject("dataset"));

                            context.getContentResolver().insert(Contract.Quote.URI,quotes);
                        } catch(JSONException ex){}
                    }
                });


            }

            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
            context.sendBroadcast(dataUpdatedIntent);

        } catch (Exception exception) {
            Timber.e(exception, "Error fetching stock quotes");
        }
    }

    private static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic task");


        JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));


        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        scheduler.schedule(builder.build());
    }


    public static synchronized void initialize(final Context context) {

        schedulePeriodic(context);
        syncImmediately(context);

    }

    public static synchronized void syncImmediately(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
        } else {

            JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));


            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            scheduler.schedule(builder.build());


        }
    }


}
