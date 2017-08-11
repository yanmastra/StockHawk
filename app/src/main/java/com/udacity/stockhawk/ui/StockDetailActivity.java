package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.udacity.stockhawk.data.Contract.Quote;

public class StockDetailActivity extends AppCompatActivity {
    private static final String TAG = StockDetailActivity.class.toString();

    @BindView(R.id.chart)
    LineChart chart;

    String stock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        stock = intent.getExtras().getString("stock");

        setTitle(stock);

        Uri uri = Quote.makeUriForStock(stock);

        Cursor c = getContentResolver().query(uri, Quote.QUOTE_COLUMNS, null, null, null);
        c.moveToFirst();
        String history = c.getString(c.getColumnIndexOrThrow(Quote.COLUMN_HISTORY));
        Timber.d(TAG, history);

        if (history != null) {
            List<Entry> entries = new ArrayList<>();
            int i = 0;
            String[] historyData = history.split("\n");

            // The items are stored in the database in descending order by data
            // Grab only the most recent 30 items
            List<String> recentItems = Arrays.asList(historyData).subList(0, 29);

            Timber.d(recentItems.toString());

            for (String rawEntry : recentItems) {
                String[] entry = rawEntry.split(",");

                entries.add(new Entry(Float.valueOf(i++), Float.valueOf(entry[1])));

            }

            LineDataSet dataSet = new LineDataSet(entries, "Stock Price");

            LineData lineData = new LineData(dataSet);

            chart.setData(lineData);
            chart.setDescription(new Description());
            chart.setDragEnabled(true);
            chart.setScaleEnabled(true);
            chart.getXAxis().setDrawLabels(false);
            chart.invalidate();
        }
    }


}
