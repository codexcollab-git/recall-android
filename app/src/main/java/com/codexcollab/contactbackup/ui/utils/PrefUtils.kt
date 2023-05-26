package com.codexcollab.contactbackup.ui.utils

import android.content.SharedPreferences

private const val PREVIOUS_BACKUP_LINK = "PREVIOUS_BACKUP_LINK"
private const val PREVIOUS_BACKUP_FILE_PATH = "PREVIOUS_BACKUP_FILE_PATH"
private const val FIREBASE_FCM_TOKEN = "FIREBASE_FCM_TOKEN"
private const val IS_FCM_SUBSCRIBED = "IS_FCM_SUBSCRIBED"
private const val ALREADY_RESTORED = "ALREADY_RESTORED"

class PrefUtils(private val prefs: SharedPreferences) {

    fun clearAll() = prefs.edit().clear().apply()

    var previousFilePath: String?
        get() = prefs.getString(PREVIOUS_BACKUP_FILE_PATH, null)
        set(value) = prefs.edit().putString(PREVIOUS_BACKUP_FILE_PATH, value).apply()

    var previousBackupLink: String?
        get() = prefs.getString(PREVIOUS_BACKUP_LINK, null)
        set(value) = prefs.edit().putString(PREVIOUS_BACKUP_LINK, value).apply()

    var fcmToken: String?
        get() = prefs.getString(FIREBASE_FCM_TOKEN, null)
        set(value) = prefs.edit().putString(FIREBASE_FCM_TOKEN, value).apply()

    var isFCMSubscribed: Boolean
        get() = prefs.getBoolean(IS_FCM_SUBSCRIBED, false)
        set(value) = prefs.edit().putBoolean(IS_FCM_SUBSCRIBED, value).apply()

    var alreadyRestored: Boolean
        get() = prefs.getBoolean(ALREADY_RESTORED, false)
        set(value) = prefs.edit().putBoolean(ALREADY_RESTORED, value).apply()
}