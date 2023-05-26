package com.codexcollab.contactbackup.listners

interface ContactPermissionListener {
    fun onPermissionGranted()
    fun onPermissionDenied()
}