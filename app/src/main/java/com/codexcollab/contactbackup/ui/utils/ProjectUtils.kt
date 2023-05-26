package com.codexcollab.contactbackup.ui.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.StringRes
import com.codexcollab.contactbackup.application.App
import java.io.*
import java.util.*


fun getStringResource(@StringRes res: Int) = App.getAppContext().getString(res)

fun Context.startActivityWithParams(
    activity: Class<*>,
    bundleData: Bundle? = null,
    isSingleTop: Boolean = false,
    isNewTask: Boolean = false,
    isClearTop: Boolean = false,
    isClearTask: Boolean = false
) {
    startActivity(Intent(this, activity).putExtra(BUNDLE_DATA, bundleData).apply {
        if (isSingleTop) flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        if (isNewTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK
        if (isClearTop) flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        if (isClearTask) flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
    })
}

fun Context.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

@SuppressLint("HardwareIds")
fun Context.getDeviceUniqueId(): String {
    val deviceId = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID);
    return deviceId.ifEmpty { " " }
}

fun View.goneView() {
    visibility = GONE
}

fun View.hideView() {
    visibility = View.INVISIBLE
}

fun View.showView() {
    visibility = VISIBLE
}

infix fun View.onClick(click: () -> Unit) {
    setOnClickListener { click() }
}

fun Context.getRealPathFromURI(contentURI: Uri): String? {
    val cursor = contentResolver.query(contentURI, null, null, null, null)
    return if (cursor == null) {
        contentURI.path
    } else {
        cursor.moveToFirst()
        val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        cursor.getString(index)
    }
}




fun Context.fileFromContentUri(contentUri: Uri): File {
    val fileName = "contact_backup.txt"
    val tempFile = File(cacheDir, fileName)
    tempFile.createNewFile()

    try {
        val oStream = FileOutputStream(tempFile)
        val inputStream = contentResolver.openInputStream(contentUri)
        inputStream?.let {
            copy(inputStream, oStream)
        }

        oStream.flush()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return tempFile
}

private fun getFileExtension(context: Context, uri: Uri): String? {
    val fileType: String? = context.contentResolver.getType(uri)
    return MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
}

@Throws(IOException::class)
private fun copy(source: InputStream, target: OutputStream) {
    val buf = ByteArray(8192)
    var length: Int
    while (source.read(buf).also { length = it } > 0) {
        target.write(buf, 0, length)
    }
}