package com.codexcollab.contactbackup.di.modules.fragment

import com.codexcollab.contactbackup.ui.restore.view.RestoreViaFileFragment
import com.codexcollab.contactbackup.ui.restore.view.RestoreViaLinkFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class RestoreFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeLinkFragment(): RestoreViaLinkFragment

    @ContributesAndroidInjector
    abstract fun contributeFileFragment(): RestoreViaFileFragment
}
