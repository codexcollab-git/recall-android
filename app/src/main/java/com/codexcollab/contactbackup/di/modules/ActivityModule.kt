package com.codexcollab.contactbackup.di.modules


import com.codexcollab.contactbackup.ui.backupsuccess.BackupResultActivity
import com.codexcollab.contactbackup.ui.history.PreviousBackupActivity
import com.codexcollab.contactbackup.di.modules.fragment.RestoreFragmentModule
import com.codexcollab.contactbackup.ui.home.view.HomeActivity
import com.codexcollab.contactbackup.ui.restore.view.RestoreContactActivity
import com.codexcollab.contactbackup.ui.splash.view.SplashActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class ActivityModule {
    @ContributesAndroidInjector
    abstract fun contributeSplashActivity(): SplashActivity

    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): HomeActivity

    @ContributesAndroidInjector
    abstract fun contributeHistoryActivity(): PreviousBackupActivity

    @ContributesAndroidInjector
    abstract fun contributeResultActivity(): BackupResultActivity

    @ContributesAndroidInjector(modules = [RestoreFragmentModule::class])
    abstract fun contributeRestoreActivity(): RestoreContactActivity
}
