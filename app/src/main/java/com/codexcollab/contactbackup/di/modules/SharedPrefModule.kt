package com.codexcollab.contactbackup.di.modules

import android.content.Context
import android.content.SharedPreferences
import com.codexcollab.contactbackup.R
import com.codexcollab.contactbackup.application.App
import com.codexcollab.contactbackup.ui.utils.PrefUtils
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SharedPrefModule {
    @Provides
    @Singleton
    fun provideSharedPreference() : SharedPreferences {
        return App.getAppContext().getSharedPreferences("${App.getAppContext().getString(R.string.app_name)}_preferences", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun providePrefUtils(sharedPref: SharedPreferences): PrefUtils {
        return PrefUtils(sharedPref)
    }
}