package com.udacity.stockhawk.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView

import com.udacity.stockhawk.R

import butterknife.BindView
import butterknife.ButterKnife


class AddStockDialog : DialogFragment() {

    @BindView(R.id.dialog_stock)
    internal var stock: EditText? = null

    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {

        val builder = AlertDialog.Builder(activity)

        val inflater = LayoutInflater.from(activity)
        @SuppressLint("InflateParams") val custom = inflater.inflate(R.layout.add_stock_dialog, null)

        ButterKnife.bind(this, custom)

        stock!!.setOnEditorActionListener { v, actionId, event ->
            addStock()
            true
        }
        builder.setView(custom)

        builder.setMessage(getString(R.string.dialog_title))
        builder.setPositiveButton(getString(R.string.dialog_add)
        ) { dialog, id -> addStock() }
        builder.setNegativeButton(getString(R.string.dialog_cancel), null)

        val dialog = builder.create()

        val window = dialog.window
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        return dialog
    }

    private fun addStock() {
        val parent = activity
        if (parent is MainActivity) {
            parent.addStock(stock!!.text.toString())
        }
        dismissAllowingStateLoss()
    }


}
