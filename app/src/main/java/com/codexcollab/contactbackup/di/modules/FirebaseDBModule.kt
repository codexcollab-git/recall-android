package com.codexcollab.contactbackup.di.modules

import com.codexcollab.contactbackup.firebase.utils.FirebaseUtils
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class FirebaseDBModule {
    @Provides
    @Singleton
    fun provideFirebaseDb() : DatabaseReference {
        return Firebase.database.reference
    }

    @Provides
    @Singleton
    fun provideFirebaseLink() : FirebaseDynamicLinks {
        return Firebase.dynamicLinks
    }

    @Provides
    @Singleton
    fun provideFirebaseUtils(ref: DatabaseReference, link : FirebaseDynamicLinks): FirebaseUtils {
        return FirebaseUtils(ref, link)
    }
}