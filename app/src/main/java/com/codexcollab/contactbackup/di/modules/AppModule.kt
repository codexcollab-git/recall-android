package com.codexcollab.contactbackup.di.modules

import dagger.Module

@Module(
    includes = [
        SharedPrefModule::class,
        ActivityModule::class,
        FirebaseDBModule::class,
        ViewModelModule::class,
        RepositoryModule::class
    ]
)
class AppModule