package com.codexcollab.contactbackup.ui.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

fun Context.isContactAccessGranted(): Boolean {
    var isPermissionGranted = false
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        isPermissionGranted = true
    }
    return isPermissionGranted
}

fun Context.isStorageAccessGranted(): Boolean {
    var isPermissionGranted = false
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        isPermissionGranted = true
    }
    return isPermissionGranted
}

fun Context.isNotificationPermissionGranted(): Boolean {
    var isPermissionGranted = false
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
        isPermissionGranted = true
    }
    return isPermissionGranted
}