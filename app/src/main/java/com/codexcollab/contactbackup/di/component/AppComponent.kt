package com.codexcollab.contactbackup.di.component

import android.app.Application
import android.content.Context
import com.codexcollab.contactbackup.application.App
import com.codexcollab.contactbackup.di.modules.AppModule

import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        AppModule::class,
    ]
)
interface AppComponent : AndroidInjector<App> {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        fun build(): AppComponent
    }
    override fun inject(app: App)
}
