package com.udacity.stockhawk.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.udacity.stockhawk.R;

/**
 * Created by Mastra on 2/27/2018.
 */

public class QuoteStatus {

    public static final int STOCK_INVALID = 0;
    public static final int STOCK_SERVER_INVALID = 1;
    public static final int STOCK_SERVER_DOWN = 2;
    public static final int STOCK_SERVER_LIMIT = 3;
    public static final int STOCK_UNKNOWN = 4;

    public static void setStatus(Context context, int status) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(context.getString(R.string.pref_stocks_status_key), status);
        editor.apply();
    }

    public static int getStatus(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(context.getString(R.string.pref_stocks_status_key), STOCK_UNKNOWN);
    }
}
