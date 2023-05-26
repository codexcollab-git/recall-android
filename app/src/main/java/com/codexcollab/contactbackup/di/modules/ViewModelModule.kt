package com.codexcollab.contactbackup.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.codexcollab.contactbackup.base.BaseViewModel
import com.codexcollab.contactbackup.di.viewmodelfactory.ViewModelFactory
import com.codexcollab.contactbackup.ui.restore.viewmodel.RestoreViewModel
import com.codexcollab.contactbackup.ui.home.viewmodel.BackupViewModel
import com.codexcollab.contactbackup.ui.splash.viewmodel.SplashViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass

@Suppress("unused")
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(RestoreViewModel::class)
    abstract fun bindRestoreViewModel(restoreViewModel: RestoreViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BackupViewModel::class)
    abstract fun bindBackupViewModel(backupViewModel: BackupViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SplashViewModel::class)
    abstract fun bindSplashViewModel(splashViewModel: SplashViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BaseViewModel::class)
    abstract fun bindBaseViewModel(baseViewModel: BaseViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}

@MustBeDocumented
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)
