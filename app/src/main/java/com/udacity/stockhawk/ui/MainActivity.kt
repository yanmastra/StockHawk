package com.udacity.stockhawk.ui

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast

import com.udacity.stockhawk.R
import com.udacity.stockhawk.data.Contract
import com.udacity.stockhawk.data.PrefUtils
import com.udacity.stockhawk.sync.QuoteSyncJob

import butterknife.BindView
import butterknife.ButterKnife
import timber.log.Timber

class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener, StockAdapter.StockAdapterOnClickHandler {
    @BindView(R.id.recycler_view)
    var stockRecyclerView: RecyclerView? = null
    @BindView(R.id.swipe_refresh)
    var swipeRefreshLayout: SwipeRefreshLayout? = null
    @BindView(R.id.error)
    var error: TextView? = null
    private var adapter: StockAdapter? = null

    override fun onClick(symbol: String) {
        Timber.d("Symbol clicked: %s", symbol)
        val intent = Intent(this, StockDetailActivity::class.java)
        intent.putExtra("stock", symbol)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        adapter = StockAdapter(this, this)
        stockRecyclerView!!.adapter = adapter
        stockRecyclerView!!.layoutManager = LinearLayoutManager(this)

        swipeRefreshLayout!!.setOnRefreshListener(this)
        swipeRefreshLayout!!.isRefreshing = true
        onRefresh()

        QuoteSyncJob.initialize(this)
        supportLoaderManager.initLoader(STOCK_LOADER, null, this)

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val symbol = adapter!!.getSymbolAtPosition(viewHolder.adapterPosition)
                PrefUtils.removeStock(this@MainActivity, symbol)
                contentResolver.delete(Contract.Quote.makeUriForStock(symbol), null, null)
            }
        }).attachToRecyclerView(stockRecyclerView)


    }

    private fun networkUp(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }

    override fun onRefresh() {

        QuoteSyncJob.syncImmediately(this)

        if (!networkUp() && adapter!!.itemCount == 0) {
            swipeRefreshLayout!!.isRefreshing = false
            error!!.text = getString(R.string.error_no_network)
            error!!.visibility = View.VISIBLE
        } else if (!networkUp()) {
            swipeRefreshLayout!!.isRefreshing = false
            Toast.makeText(this, R.string.toast_no_connectivity, Toast.LENGTH_LONG).show()
        } else if (PrefUtils.getStocks(this).size == 0) {
            swipeRefreshLayout!!.isRefreshing = false
            error!!.text = getString(R.string.error_no_stocks)
            error!!.visibility = View.VISIBLE
        } else {
            error!!.visibility = View.GONE
        }
    }

    fun button(view: View) {
        AddStockDialog().show(fragmentManager, "StockDialogFragment")
    }

    internal fun addStock(symbol: String?) {
        if (symbol != null && !symbol.isEmpty()) {

            if (networkUp()) {
                swipeRefreshLayout!!.isRefreshing = true
            } else {
                val message = getString(R.string.toast_stock_added_no_connectivity, symbol)
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }

            PrefUtils.addStock(this, symbol)
            QuoteSyncJob.syncImmediately(this)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Cursor> {
        return CursorLoader(this,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS, null, null, Contract.Quote.COLUMN_SYMBOL)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        swipeRefreshLayout!!.isRefreshing = false

        if (data.count != 0) {
            error!!.visibility = View.GONE
        }
        adapter!!.setCursor(data)
    }


    override fun onLoaderReset(loader: Loader<Cursor>) {
        swipeRefreshLayout!!.isRefreshing = false
        adapter!!.setCursor(null!!)
    }


    private fun setDisplayModeMenuItemIcon(item: MenuItem) {
        if (PrefUtils.getDisplayMode(this) == getString(R.string.pref_display_mode_absolute_key)) {
            item.setIcon(R.drawable.ic_percentage)
        } else {
            item.setIcon(R.drawable.ic_dollar)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_activity_settings, menu)
        val item = menu.findItem(R.id.action_change_units)
        setDisplayModeMenuItemIcon(item)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_change_units) {
            PrefUtils.toggleDisplayMode(this)
            setDisplayModeMenuItemIcon(item)
            adapter!!.notifyDataSetChanged()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {

        private val STOCK_LOADER = 0
    }
}
