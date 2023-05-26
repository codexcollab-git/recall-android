package com.codexcollab.contactbackup.customdialog

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat
import com.codexcollab.contactbackup.R
import com.codexcollab.contactbackup.databinding.CustomDialogLayoutBinding
import com.codexcollab.contactbackup.databinding.DialogProgressLoaderBinding
import com.codexcollab.contactbackup.listners.ClickListener
import com.codexcollab.contactbackup.ui.utils.goneView
import com.codexcollab.contactbackup.ui.utils.hideView
import com.codexcollab.contactbackup.ui.utils.showView

class CustomDialog {
    private val context: Context
    private lateinit var loaderDialog: AppCompatDialog
    private lateinit var loaderDialogBinding: DialogProgressLoaderBinding
    private lateinit var dialog: AppCompatDialog
    private lateinit var dialogBinding: CustomDialogLayoutBinding
    private var positiveListener: ClickListener<CustomDialog>? = null
    private var negativeListener: ClickListener<CustomDialog>? = null

    constructor(context: Context) {
        this.context = context
        dialog = AppCompatDialog(context, R.style.CustomDialog)
        dialogBinding = CustomDialogLayoutBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(true)
        if (dialog.window != null) dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    constructor(
        context: Context,
        heading: String?,
        headingColor: Int?,
        subHeading: String?,
        confirmTxt: String?,
        cancelTxt: String?,
        cancelable: Boolean,
        confirmTextBg: Int?,
        negativeTextBg: Int?,
        positiveListener: ClickListener<CustomDialog>?,
        negativeListener: ClickListener<CustomDialog>?
    ) {
        this.context = context
        dialog = AppCompatDialog(context, R.style.CustomDialog)
        dialogBinding = CustomDialogLayoutBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(dialogBinding.root)
        if (dialog.window != null) dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(cancelable)
        if (heading == null || heading.isEmpty()) dialogBinding.heading.goneView() else setHeading(heading, headingColor)
        if (subHeading == null || subHeading.isEmpty()) dialogBinding.subHeading.goneView() else setSubHeading(
            subHeading
        )
        if (confirmTxt == null || confirmTxt.isEmpty()) dialogBinding.ok.goneView() else setPositiveLabel(
            confirmTxt
        )
        if (cancelTxt == null || cancelTxt.isEmpty()) dialogBinding.cancel.goneView() else setNegativeLabel(
            cancelTxt
        )
        if (confirmTextBg != null) dialogBinding.ok.background = ContextCompat.getDrawable(context, confirmTextBg)
        if (negativeTextBg != null) dialogBinding.cancel.background = ContextCompat.getDrawable(context, negativeTextBg)
        if (positiveListener != null) this.positiveListener = positiveListener
        if (negativeListener != null) this.negativeListener = negativeListener
        initConfig()
    }

    constructor(context: Context, cancelable: Boolean) {
        this.context = context
        loaderDialog = AppCompatDialog(context)
        loaderDialogBinding = DialogProgressLoaderBinding.inflate(LayoutInflater.from(context))
        loaderDialog.setContentView(loaderDialogBinding.root)
        loaderDialog.setCancelable(cancelable)
        if (loaderDialog.window != null) loaderDialog.window!!.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
    }

    fun getLoaderDialog(): AppCompatDialog {
        return loaderDialog
    }

    fun showLoader() {
        if (!(context as Activity).isFinishing) loaderDialog.show()
    }

    fun dismissLoader() {
        if (this::loaderDialog.isInitialized) {
            loaderDialog.dismiss()
        }
    }

    fun getDialog(): AppCompatDialog {
        return dialog
    }

    fun show() {
        if (!(context as Activity).isFinishing) dialog.show()
    }

    fun dismiss() {
        if (!(context as Activity).isFinishing && !context.isDestroyed) dialog.dismiss()
    }

    fun setHeading(title: String?, headingColor: Int?) {
        dialogBinding.heading.text = title
        if(headingColor != null) dialogBinding.heading.setTextColor(ContextCompat.getColor(context, headingColor))
    }

    fun setSubHeading(subTitle: String?) {
        dialogBinding.subHeading.text = subTitle
    }

    private fun setPositiveLabel(positive: String) {
        dialogBinding.ok.showView()
        dialogBinding.ok.text = positive
    }

    private fun setNegativeLabel(negative: String) {
        dialogBinding.cancel.showView()
        dialogBinding.cancel.text = negative
    }

    private fun initConfig() {
        dialogBinding.ok.setOnClickListener {
            if (positiveListener != null) positiveListener!!.onClick(this@CustomDialog)
        }
        dialogBinding.cancel.setOnClickListener {
            if (negativeListener != null) negativeListener!!.onClick(this@CustomDialog) else dismiss()
        }
    }

    fun setOnConfirmClickListener(listener: ClickListener<CustomDialog>?) {
        positiveListener = listener
    }

    fun setOnCancelListener(listener: ClickListener<CustomDialog>?) {
        negativeListener = listener
    }
}