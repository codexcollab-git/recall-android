package com.codexcollab.contactbackup.ui.utils

import android.content.Context
import com.codexcollab.contactbackup.customdialog.CustomDialogBuilder

fun Context.showLoaderDialog(
    prevDialog: CustomDialogBuilder?
): CustomDialogBuilder {
    return if (prevDialog != null && prevDialog.dialogInstance.getLoaderDialog().isShowing) prevDialog else {
        val dialog = CustomDialogBuilder(this)
        dialog.setCancelable(false)
        dialog.buildLoader().showLoader()
        return dialog
    }
}