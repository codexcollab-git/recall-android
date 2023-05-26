package com.codexcollab.contactbackup.listners

import android.net.Uri

interface DeepLinkListener {
    fun onSuccess(uri: Uri?)
    fun onFailure()
}