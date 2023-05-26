package com.codexcollab.contactbackup.di.modules

import com.codexcollab.contactbackup.commonrepo.CommonRepository
import com.codexcollab.contactbackup.commonrepo.DefaultCommonRepository
import dagger.Binds
import dagger.Module

@Module
interface RepositoryModule {
    @Binds
    fun bindCommonRepository(commonRepo: DefaultCommonRepository): CommonRepository
}