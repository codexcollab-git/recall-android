package com.codexcollab.contactbackup.model.contact

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DataDTO(
    var dataType: Int = 0,
    var dataValue: String? = null,
    var dataTypeStr: String? = null
): Parcelable