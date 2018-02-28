package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.db.Contract;
import com.udacity.stockhawk.ui.MainActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Mastra on 2/28/2018.
 */

public class MyRemoteService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            Cursor cursor = null;
            DecimalFormat dolar;
            DecimalFormat dolarWithPlus;

            @Override
            public void onCreate() {
                dolar = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dolarWithPlus = dolar;
                dolarWithPlus.setPositivePrefix("+$");
            }

            @Override
            public void onDataSetChanged() {
                if (cursor != null) cursor.close();
                cursor = getContentResolver().query(
                        Contract.Quote.URI,
                        Contract.Quote.QUOTE_COLUMNS,
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL
                );
            }

            @Override
            public void onDestroy() {
                if(cursor != null){
                    cursor.close();
                    cursor = null;
                }
            }

            @Override
            public int getCount() {
                return cursor==null?0:cursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION || cursor == null || !cursor.moveToPosition(position))
                return null;

                RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.list_item_quote);
                remoteViews.setTextViewText(R.id.symbol, cursor.getString(Contract.Quote.POSITION_SYMBOL));
                remoteViews.setTextViewText(R.id.price, dolar.format(cursor.getFloat(Contract.Quote.POSITION_PRICE)));

                float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);

                if (rawAbsoluteChange > 0) {
                    remoteViews.setInt(
                            R.id.change,
                            "setBackgroundColor",
                            ContextCompat.getColor(getBaseContext(), R.color.material_green_700)
                    );
                } else {
                    remoteViews.setInt(
                            R.id.change,
                            "setBackgroundColor",
                            ContextCompat.getColor(getBaseContext(), R.color.material_red_700)
                    );
                }

                String change = dolarWithPlus.format(rawAbsoluteChange);
                remoteViews.setTextViewText(R.id.change, change);

                final Intent fillInIntent = new Intent();
                final Bundle extras = new Bundle();

                extras.putString(MainActivity.SYMBOL_KEY, cursor.getString(Contract.Quote.POSITION_SYMBOL));
                fillInIntent.putExtras(extras);
                remoteViews.setOnClickFillInIntent(R.id.list_item_quote, fillInIntent);

                return remoteViews;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getBaseContext().getPackageName(), R.layout.list_item_quote);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
