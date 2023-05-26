package com.codexcollab.contactbackup.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.codexcollab.contactbackup.firebase.utils.FirebaseUtils
import com.codexcollab.contactbackup.ui.utils.PrefUtils
import javax.inject.Inject

open class BaseViewModel @Inject constructor(app: Application) : AndroidViewModel(app) {
    @Inject
    lateinit var pref: PrefUtils
    @Inject
    lateinit var firebaseRef: FirebaseUtils
    internal val loading = MutableLiveData(false)
}