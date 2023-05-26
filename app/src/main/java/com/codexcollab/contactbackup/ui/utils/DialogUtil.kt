package com.codexcollab.contactbackup.ui.utils

import android.content.Context
import com.codexcollab.contactbackup.R
import com.codexcollab.contactbackup.customdialog.CustomDialog
import com.codexcollab.contactbackup.customdialog.CustomDialogBuilder
import com.codexcollab.contactbackup.listners.ClickListener


fun Context.showMessageWithFunctionality(
    heading: String,
    headingColor: Int,
    successMsg: String,
    positiveBtnText: String = getString(android.R.string.ok),
    background: Int,
    prevDialog: CustomDialogBuilder?,
    listener: ClickListener<CustomDialog>
): CustomDialogBuilder {
    if (prevDialog != null && prevDialog.dialogInstance.getDialog().isShowing) { prevDialog.dialogInstance.getDialog().dismiss() }
    val dialog = CustomDialogBuilder(this)
    dialog.setHeadingText(heading, headingColor)
    dialog.setSubHeadingText(successMsg)
    dialog.setCancelable(true)
    dialog.setConfirmTextBackground(background)
    dialog.setPositiveButton(positiveBtnText){
        listener.onClick(it)
        it.dismiss()
    }
    dialog.setNegativeButton(getString(R.string.cancel)) {
        it.dismiss()
    }
    dialog.build().show()
    return dialog
}

fun Context.showErrorDialog(
    errorMessage: String,
    positiveBtnText: String = getString(android.R.string.ok),
    prevDialog: CustomDialogBuilder?,
    heading: String? = getString(R.string.error),
    listener: ClickListener<CustomDialog>? = null
): CustomDialogBuilder {
    return if (prevDialog != null && prevDialog.dialogInstance.getDialog().isShowing) prevDialog else {
        val dialog = CustomDialogBuilder(this)
        dialog.setHeadingText(heading)
        dialog.setSubHeadingText(errorMessage)
        dialog.setCancelable(true)
        dialog.setNegativeButton(positiveBtnText) { if(listener != null) listener.onClick(it) else it.dismiss() }
        dialog.build().show()
        dialog
    }
}