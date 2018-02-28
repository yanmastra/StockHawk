package com.udacity.stockhawk.ui.detail;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.ui.MainActivity;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements FragmentToActivity{
    @BindView(R.id.tv_date_selected) TextView tvDateSelected;
    @BindView(R.id.tv_data_selected) TextView tvDataSelected;
    @BindView(R.id.tv_empty) TextView tvEmpty;

    private String symbol = "";
    private long period = 0;
    private int title = R.string.week;

    private ActivityToFragment activityToFragment;

    @Override
    public void onChartValueSelected(String date, String data) {
        tvDataSelected.setText(data);
        tvDateSelected.setText(date);
    }

    public interface ActivityToFragment{
        void onChangePeriod(long period, int title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        symbol = getIntent().getStringExtra(MainActivity.SYMBOL_KEY);

        if (savedInstanceState != null){
            period = savedInstanceState.getLong(DetailFragment.SELECTED_PERIOD);
            title = savedInstanceState.getInt(DetailFragment.TITLE);
            symbol = savedInstanceState.getString(DetailFragment.SELECTED_SYMBOL);
        }else {
            period = generatePeriod(Calendar.DATE, 7);
        }
        if (!TextUtils.isEmpty(symbol)) {
            setTitle(symbol);
            setUp();
        }else {
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }

    public void setActivityToFragment(ActivityToFragment activityToFragment){
        this.activityToFragment = activityToFragment;
    }

    private void setUp(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        DetailFragment detailFragment = new DetailFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(DetailFragment.SELECTED_PERIOD, period);
        bundle.putString(DetailFragment.SELECTED_SYMBOL, symbol);
        bundle.putInt(DetailFragment.TITLE, title);
        detailFragment.setArguments(bundle);
        transaction.replace(R.id.fl_fragment, detailFragment);
        transaction.commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(DetailFragment.SELECTED_SYMBOL, symbol);
        outState.putLong(DetailFragment.SELECTED_PERIOD, period);
        outState.putInt(DetailFragment.TITLE, title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!TextUtils.isEmpty(symbol)) {
            int id = item.getItemId();
            switch (id) {
                case R.id.mn_week:
                    period = generatePeriod(Calendar.DATE, 7);
                    title = R.string.week;
                    break;
                case R.id.mn_1_month:
                    period = generatePeriod(Calendar.MONTH, 1);
                    title = R.string.month_1;
                    break;
                case R.id.mn_3_month:
                    period = generatePeriod(Calendar.MONTH, 3);
                    title = R.string.month_3;
                    break;
            }
            activityToFragment.onChangePeriod(period, title);
        }
        return super.onOptionsItemSelected(item);
    }

    private long generatePeriod(int period, int between){
        Calendar calendar = Calendar.getInstance();
        calendar.add(period, -between);
        return calendar.getTimeInMillis();
    }
}
