package com.codexcollab.contactbackup.firebase.utils

import android.content.Intent
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.dynamiclinks.ShortDynamicLink

class FirebaseUtils(ref: DatabaseReference, private val dLink : FirebaseDynamicLinks) {
    var contactRef: DatabaseReference = ref.child("contacts")

    fun getDynamicLink(uniqueId: String): Task<ShortDynamicLink> {
        return dLink.createDynamicLink()
            .setLink(Uri.parse("https://recall.codexcollab.com/restore/${uniqueId}/"))
            .setDomainUriPrefix("https://recall.codexcollab.com/")
            .buildShortDynamicLink(ShortDynamicLink.Suffix.SHORT)
    }

    fun parseDynamicLink(intent: Intent) : Task<PendingDynamicLinkData> {
        return dLink.getDynamicLink(intent)
    }
}