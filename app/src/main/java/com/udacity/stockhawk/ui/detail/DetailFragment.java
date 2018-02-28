package com.udacity.stockhawk.ui.detail;


import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.common.collect.Lists;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.db.Contract;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, DetailActivity.ActivityToFragment{
    @BindView(R.id.tv_title) TextView tvTitle;
    @BindView(R.id.lc_chart) LineChart lcChart;

    public static final String SELECTED_PERIOD = "SELECTED_PERIOD";
    public static final String SELECTED_SYMBOL = "SELECTED_SYMBOL";
    public static final String TITLE = "TITLE";

    private int LOADER_ID = 0;

    private Long datePeriod;
    private String symbol;
    private Cursor data;

    private FragmentToActivity fragmentToActivity;

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        ((DetailActivity)getActivity()).setActivityToFragment(this);
        fragmentToActivity = (DetailActivity)getActivity();

        setUp(view);

        return view;
    }

    private void setUp(View v){
        ButterKnife.bind(this, v);
        datePeriod = getArguments().getLong(SELECTED_PERIOD);
        symbol = getArguments().getString(SELECTED_SYMBOL);
        tvTitle.setText(getText(getArguments().getInt(TITLE)));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
        super.onResume();
    }

    //start Loader
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                Contract.Quote.makeUriForStock(symbol),
                Contract.Quote.QUOTE_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        this.data = data;
        if(this.data != null) {
            generateChart(this.data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
    //end loader

    private void generateChart(Cursor cursor){
        @SuppressLint("SimpleDateFormat")
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<ILineDataSet> iLineDataSets = new ArrayList<>();
        Date historyTime = new Date();
        Date periodTime = new Date(datePeriod);

        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            List<Entry> values = new ArrayList<>();
            String[] history = cursor.getString(Contract.Quote.POSITION_HISTORY).split("\n");

            for (int i = 0; historyTime.getTime()>periodTime.getTime(); i++){
                String[] value = history[i].split(", ");

                Entry entry = null;
                try {
                    historyTime = format.parse(value[0]);
                    entry = new Entry(historyTime.getTime(), Float.parseFloat(value[1]));

                }catch (ParseException pe){
                    pe.printStackTrace();
                }
                values.add(entry);
            }

            LineDataSet dataSet = new LineDataSet(Lists.reverse(values), symbol);
            dataSet.setValueTextColor(Color.CYAN);
            dataSet.setValueTextSize(10f);
            iLineDataSets.add(dataSet);
        }
        LineData lineData = new LineData(iLineDataSets);
        lcChart.setData(lineData);
        lcChart.invalidate();

        // setup chart ui
        lcChart.getDescription().setEnabled(false);
        lcChart.getLegend().setEnabled(false);
        lcChart.getAxisRight().setDrawLabels(false);
        lcChart.getAxisLeft().setTextColor(Color.WHITE);
        lcChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat format = new SimpleDateFormat("EEEE");
                Date date = new Date();
                date.setTime((long)e.getX());
                DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
                fragmentToActivity.onChartValueSelected(format.format(date)+", "+dateFormat.format(date)+" :", String.valueOf(e.getY()));
            }

            @Override
            public void onNothingSelected(){
                fragmentToActivity.onChartValueSelected("","");
            }
        });

        // setup base chart line
        setupYAxis(lcChart.getAxisLeft());
        setupXAxis(lcChart.getXAxis());
    }

    private void setupYAxis(YAxis axisLeft) {
        axisLeft.setDrawGridLines(true);
        axisLeft.setAxisLineColor(Color.WHITE);
        axisLeft.setAxisLineWidth(2f);
        axisLeft.setTextColor(Color.WHITE);
    }
    private void setupXAxis(XAxis xAxis) {
        IAxisValueFormatter iAxisValueFormatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getDateFormat());
                return simpleDateFormat.format(new Date((long) value));
            }
        };

        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(Color.WHITE);
        xAxis.setAxisLineWidth(2f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setValueFormatter(iAxisValueFormatter);
    }

    private String getDateFormat() {
        int days = getDateDiff();

        if (days < 8) return "EEE";
        else return "dd";
    }

    private int getDateDiff() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(datePeriod);
        long diff = Calendar.getInstance().getTimeInMillis() - calendar.getTimeInMillis();
        return (int) (diff / (1000 * 60 * 60 * 24));
    }

    @Override
    public void onChangePeriod(long period, int title) {
        datePeriod = period;
        tvTitle.setText(getText(title));
        lcChart.removeAllViews();
        generateChart(this.data);
    }
}
