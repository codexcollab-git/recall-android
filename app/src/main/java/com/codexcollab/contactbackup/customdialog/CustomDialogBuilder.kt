package com.codexcollab.contactbackup.customdialog

import android.content.Context
import com.codexcollab.contactbackup.listners.ClickListener

class CustomDialogBuilder(private val context: Context) {
    var dialogInstance: CustomDialog
    private var cancelable = false
    private var heading: String? = null
    private var headingColor: Int? = null
    private var subHeading: String? = null
    private var confirmText: String? = null
    private var cancelText: String? = null
    private var confirmTextBg : Int? = null
    private var negativeTextBg : Int? = null
    private var positiveListener: ClickListener<CustomDialog>? = null
    private var negativeListener: ClickListener<CustomDialog>? = null

    init {
        dialogInstance = CustomDialog(context)
    }

    fun build(): CustomDialog {
        dialogInstance = CustomDialog(
            context,
            heading,
            headingColor,
            subHeading,
            confirmText,
            cancelText,
            cancelable,
            confirmTextBg,
            negativeTextBg,
            positiveListener,
            negativeListener,
        )
        return dialogInstance
    }

    fun buildLoader(): CustomDialog {
        dialogInstance = CustomDialog(context, cancelable)
        return dialogInstance
    }

    fun setHeadingText(headingText: String?, headingColor : Int? = null): CustomDialogBuilder {
        this.heading = headingText
        this.headingColor = headingColor
        return this
    }

    fun setConfirmTextBackground(drawableId : Int? = null): CustomDialogBuilder {
        this.confirmTextBg = drawableId
        return this
    }

    fun setNegativeTextBackground(drawableId : Int? = null): CustomDialogBuilder {
        this.negativeTextBg = drawableId
        return this
    }

    fun setSubHeadingText(subHeadingText: String?): CustomDialogBuilder {
        this.subHeading = subHeadingText
        return this
    }

    fun setCancelTextText(cancelText: String?): CustomDialogBuilder {
        this.cancelText = cancelText
        return this
    }

    fun setCancelable(cancelable: Boolean): CustomDialogBuilder {
        this.cancelable = cancelable
        return this
    }

    fun setConfirmText(confirmText: String?) {
        this.confirmText = confirmText
    }

    fun setPositiveButton(
        confirmText: String?,
        listener: ClickListener<CustomDialog>?
    ): CustomDialogBuilder {
        positiveListener = listener
        this.confirmText = confirmText
        return this
    }

    fun setNegativeButton(
        cancelText: String?,
        listener: ClickListener<CustomDialog>?
    ): CustomDialogBuilder {
        negativeListener = listener
        this.cancelText = cancelText
        return this
    }
}