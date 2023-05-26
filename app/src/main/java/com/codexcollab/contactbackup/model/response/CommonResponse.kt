package com.codexcollab.contactbackup.model.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommonResponse(
    var status: Boolean = false,
    var message: String? = null,
    var data: Data? = null
) : Parcelable

@Parcelize
data class Data(
    var link: String? = null
) : Parcelable