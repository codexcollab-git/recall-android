package com.codexcollab.contactbackup.ui.splash.viewmodel

import android.app.Application
import android.content.Intent
import android.util.Log
import com.codexcollab.contactbackup.BuildConfig
import com.codexcollab.contactbackup.R
import com.codexcollab.contactbackup.base.BaseViewModel
import com.codexcollab.contactbackup.commonrepo.CommonRepository
import com.codexcollab.contactbackup.firebase.fcm.TOPIC_GLOBAL_DEBUG
import com.codexcollab.contactbackup.firebase.fcm.TOPIC_GLOBAL_LIVE
import com.codexcollab.contactbackup.listners.DeepLinkListener
import com.codexcollab.contactbackup.ui.utils.BUILD_DEBUG
import com.codexcollab.contactbackup.ui.utils.getStringResource
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    app: Application,
    private val repo: CommonRepository
) : BaseViewModel(app) {

    fun registerFCM() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            val token = task.result
            val topic =
                if (BuildConfig.BUILD_TYPE == BUILD_DEBUG) TOPIC_GLOBAL_DEBUG else TOPIC_GLOBAL_LIVE
            if (!pref.isFCMSubscribed) {
                FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnSuccessListener { pref.isFCMSubscribed = true }
            }
            pref.fcmToken = token
            Log.e("FIREBASE FCM TOKEN -> ", pref.fcmToken ?: "")
        })
    }

    fun parseDynamicLink(intent: Intent, listener: DeepLinkListener) {
        val dynamicTask = firebaseRef.parseDynamicLink(intent)
        dynamicTask.addOnSuccessListener {
            if (it != null) {
                val link = it.link
                listener.onSuccess(link)
            }else listener.onFailure()
        }.addOnFailureListener {
            listener.onFailure()
        }
    }
}