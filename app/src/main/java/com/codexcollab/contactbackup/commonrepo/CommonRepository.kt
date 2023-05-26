package com.codexcollab.contactbackup.commonrepo

import android.util.Log
import com.codexcollab.contactbackup.ui.utils.encryption.Encryption
import java.lang.Exception
import javax.inject.Inject


interface CommonRepository {
    fun encrypt(data: String): String
    fun decrypt(data: String): String?
}

class DefaultCommonRepository @Inject constructor() : CommonRepository {
    private val encryptionKey = "RecallContactBackup"
    private val encryptionSalt = "RecallContactBackupSalt"

    private fun getEncryption(): Encryption {
        val byteSize = ByteArray(16)
        return Encryption.getDefault(encryptionKey, encryptionSalt, byteSize)
    }

    override fun encrypt(data: String): String {
        return getEncryption().encryptOrNull(data)
    }

    override fun decrypt(data: String): String? {
        var dataDe : String? = null
        try {
            dataDe = getEncryption().decryptOrNull(data)
        }catch (e : Exception){
            Log.e("Error -> ", "decrypt: ", e)
        }
        return dataDe
    }
}