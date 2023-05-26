package com.codexcollab.contactbackup.application

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.codexcollab.contactbackup.di.component.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class App : DaggerApplication(), Application.ActivityLifecycleCallbacks {

    companion object {
        private var appContext: Context? = null
        private lateinit var app: Application
        fun getAppContext() = app
        fun getCurrentActivity() = appContext
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        registerActivityLifecycleCallbacks(this)
    }

    private val applicationInjector = DaggerAppComponent.builder().application(this).build()
    override fun applicationInjector(): AndroidInjector<out DaggerApplication> = applicationInjector

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        appContext = activity
    }

    override fun onActivityStarted(activity: Activity) {
        appContext = activity
    }

    override fun onActivityResumed(activity: Activity) {
        appContext = activity
    }

    override fun onActivityPaused(activity: Activity) {
        appContext = null
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, p1: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }
}
