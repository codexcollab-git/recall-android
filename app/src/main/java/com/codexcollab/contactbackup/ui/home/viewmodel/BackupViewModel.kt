package com.codexcollab.contactbackup.ui.home.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.*
import android.database.Cursor
import android.os.Environment
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.codexcollab.contactbackup.BuildConfig
import com.codexcollab.contactbackup.R
import com.codexcollab.contactbackup.application.App
import com.codexcollab.contactbackup.base.BaseViewModel
import com.codexcollab.contactbackup.firebase.fcm.TOPIC_GLOBAL_DEBUG
import com.codexcollab.contactbackup.firebase.fcm.TOPIC_GLOBAL_LIVE
import com.codexcollab.contactbackup.model.response.CommonResponse
import com.codexcollab.contactbackup.model.contact.ContactDTO
import com.codexcollab.contactbackup.model.response.Data
import com.codexcollab.contactbackup.model.contact.DataDTO
import com.codexcollab.contactbackup.commonrepo.DefaultCommonRepository
import com.codexcollab.contactbackup.listners.AuthListener
import com.codexcollab.contactbackup.ui.utils.BUILD_DEBUG
import com.codexcollab.contactbackup.ui.utils.FILE_NAME
import com.codexcollab.contactbackup.ui.utils.getDeviceUniqueId
import com.codexcollab.contactbackup.ui.utils.getStringResource
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ServerValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.util.*
import javax.inject.Inject

class BackupViewModel @Inject constructor(
    app: Application,
    private val repo: DefaultCommonRepository
) : BaseViewModel(app) {

    private val _response: MutableLiveData<CommonResponse?> = MutableLiveData()
    val response: LiveData<CommonResponse?> = _response
    val progressTxt = MutableLiveData("")
    var file: File? = null

    private val uniqueDevice = app.getDeviceUniqueId()

    fun signInAnonymous(listener : AuthListener){
        Firebase.auth.signInAnonymously()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    listener.onSuccess()
                }else{
                    val errorResponse = getErrorResponse(it.toString())
                    _response.value = errorResponse
                    listener.onFailure()
                }
            }
    }

    private fun saveContactFile(fileContents: String) {
        try {
            viewModelScope.launch(Dispatchers.Main) {
                progressTxt.value = getStringResource(R.string.generating_file)
            }
            val encrypted = repo.encrypt(fileContents)
            file = File(App.getAppContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), FILE_NAME)
            pref.previousFilePath = file?.absolutePath
            val out = FileWriter(file)
            saveDataToFirebase(encrypted)
            out.write(encrypted)
            out.close()
        } catch (e: IOException) {
            Log.e("Error -> ", "${e.message}")
            viewModelScope.launch(Dispatchers.Main) {
                _response.value = getErrorResponse(e.message ?: getStringResource(R.string.backup_error))
            }
        }
    }

    private fun saveDataToFirebase(encryptedContacts: String) {
        val path = firebaseRef.contactRef.child(uniqueDevice)
        val data = HashMap<String, Any>()
        data["timestamp"] = ServerValue.TIMESTAMP
        data["contacts"] = encryptedContacts
        viewModelScope.launch(Dispatchers.Main) {
            progressTxt.value = getStringResource(R.string.excp_upload)
        }
        path.setValue(data).addOnCompleteListener {
            if (it.isSuccessful) {
                val dynamicTask = firebaseRef.getDynamicLink(uniqueDevice)
                dynamicTask.addOnCompleteListener { dLink ->
                    val link = dLink.result.shortLink.toString()
                    pref.previousBackupLink = link
                    val successResponse = getSuccessResponse(link)
                    _response.value = successResponse
                    Log.e("FIREBASE -> ", Gson().toJson(successResponse))
                }
            } else {
                val errorResponse = getErrorResponse(it.toString())
                _response.value = errorResponse
                Log.e("FIREBASE -> ", Gson().toJson(errorResponse))
            }
        }
    }

    private fun getSuccessResponse(link: String): CommonResponse {
        val response = CommonResponse()
        response.status = true
        response.message = getStringResource(R.string.backup_success)
        response.data = Data(link)
        return response
    }

    private fun getErrorResponse(error: String): CommonResponse {
        val response = CommonResponse()
        response.status = false
        response.message = error
        return response
    }

    /** READ CONTACT ------------------------------------------------------------------------- */

    fun startUploadingContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = getAllContacts(App.getAppContext().contentResolver)
            saveContactFile(Gson().toJson(list))
        }
    }

    private val home = "Home"
    private val work = "Work"
    private val mobile = "Mobile"

    @SuppressLint("Range")
    fun getAllContacts(contentResolver: ContentResolver): List<ContactDTO> {
        val ret: ArrayList<ContactDTO> = ArrayList()
        val rawContactsIdList = getRawContactsIdList(contentResolver)
        val contactListSize = rawContactsIdList.size

        for (i in 0 until contactListSize) {
            val contactDTO = ContactDTO()
            val rawContactId = rawContactsIdList[i]
            val dataContentUri = ContactsContract.Data.CONTENT_URI
            val queryColumnList: MutableList<String> = ArrayList()

            queryColumnList.add(ContactsContract.Data.CONTACT_ID)
            queryColumnList.add(ContactsContract.Data.MIMETYPE)
            queryColumnList.add(ContactsContract.Data.DATA1)
            queryColumnList.add(ContactsContract.Data.DATA2)
            queryColumnList.add(ContactsContract.Data.DATA3)
            queryColumnList.add(ContactsContract.Data.DATA4)
            queryColumnList.add(ContactsContract.Data.DATA5)
            queryColumnList.add(ContactsContract.Data.DATA6)
            queryColumnList.add(ContactsContract.Data.DATA7)
            queryColumnList.add(ContactsContract.Data.DATA8)
            queryColumnList.add(ContactsContract.Data.DATA9)
            queryColumnList.add(ContactsContract.Data.DATA10)
            queryColumnList.add(ContactsContract.Data.DATA11)
            queryColumnList.add(ContactsContract.Data.DATA12)
            queryColumnList.add(ContactsContract.Data.DATA13)
            queryColumnList.add(ContactsContract.Data.DATA14)
            queryColumnList.add(ContactsContract.Data.DATA15)

            val queryColumnArr = queryColumnList.toTypedArray()

            val whereClauseBuf = StringBuffer()
            whereClauseBuf.append(ContactsContract.Data.RAW_CONTACT_ID)
            whereClauseBuf.append("=")
            whereClauseBuf.append(rawContactId)

            val cursor = contentResolver.query(
                dataContentUri,
                queryColumnArr,
                whereClauseBuf.toString(),
                null,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC"
            )

            if (cursor != null && cursor.count > 0) {
                cursor.moveToFirst()
                val contactId =
                    cursor.getLong(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID))
                contactDTO.contactId = contactId
                do {
                    val mimeType =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE))
                    val dataValueList = getColumnValueByMimetype(contactDTO, cursor, mimeType)
                    if (!ret.contains(dataValueList)) {
                        ret.add(dataValueList)
                        val name = dataValueList.displayName ?: dataValueList.nickName
                        viewModelScope.launch(Dispatchers.Main) {
                            //progressTxt.value = "Backing up contacts..."
                            progressTxt.value =
                                "$name ($i/$contactListSize) ${getStringResource(R.string.backed_up_txt)}"
                        }
                    }
                } while (cursor.moveToNext())
            }
        }
        return ret
    }

    private fun getEmailTypeString(dataType: Int): String {
        var ret = ""
        if (ContactsContract.CommonDataKinds.Email.TYPE_HOME == dataType) {
            ret = home
        } else if (ContactsContract.CommonDataKinds.Email.TYPE_WORK == dataType) {
            ret = work
        }
        return ret
    }

    private fun getPhoneTypeString(dataType: Int): String {
        var ret = ""
        if (ContactsContract.CommonDataKinds.Phone.TYPE_HOME == dataType) {
            ret = home
        } else if (ContactsContract.CommonDataKinds.Phone.TYPE_WORK == dataType) {
            ret = work
        } else if (ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE == dataType) {
            ret = mobile
        }
        return ret
    }

    @SuppressLint("Range")
    private fun getColumnValueByMimetype(
        contactDTO: ContactDTO,
        cursor: Cursor,
        mimeType: String
    ): ContactDTO {
        when (mimeType) {
            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> {
                val emailAddress =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS))
                val emailType =
                    cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE))
                val emailTypeStr = getEmailTypeString(emailType)
                val dataObj = DataDTO()
                dataObj.dataValue = emailAddress
                dataObj.dataType = emailType
                dataObj.dataTypeStr = emailTypeStr
                contactDTO.emailList.add(dataObj)
            }
            ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE -> {
                val imProtocol =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL))
                val imId =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA))
                val dataObj = DataDTO()
                dataObj.dataValue = imId
                dataObj.dataType = 0
                dataObj.dataTypeStr = imProtocol
                contactDTO.imList.add(dataObj)
            }
            ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE -> {
                val nickName =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME))
                contactDTO.nickName = nickName
            }
            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE -> {
                val company =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY))
                val department =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DEPARTMENT))
                val title =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE))
                val jobDescription =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION))
                val officeLocation =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION))
                contactDTO.company = company
                contactDTO.department = department
                contactDTO.title = title
                contactDTO.jobDescription = jobDescription
                contactDTO.officeLocation = officeLocation
            }
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> {
                val phoneNumber =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val phoneTypeInt =
                    cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
                val phoneTypeStr = getPhoneTypeString(phoneTypeInt)
                val dataObj = DataDTO()
                dataObj.dataValue = phoneNumber
                dataObj.dataType = phoneTypeInt
                dataObj.dataTypeStr = phoneTypeStr
                contactDTO.phoneList.add(dataObj)
            }
            ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE -> {
                val address =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS))
                val addressTypeInt =
                    cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.TYPE))
                val addressTypeStr = getEmailTypeString(addressTypeInt)
                val dataObj = DataDTO()
                dataObj.dataValue = address
                dataObj.dataType = addressTypeInt
                dataObj.dataTypeStr = addressTypeStr
                contactDTO.addressList.add(dataObj)
            }
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE -> {
                val displayName =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME))
                val givenName =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME))
                val familyName =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME))
                contactDTO.displayName = displayName
                contactDTO.givenName = givenName
                contactDTO.familyName = familyName
            }
            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE -> {
                val country =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY))
                val city =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY))
                val region =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION))
                val street =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET))
                val postcode =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE))
                val postType =
                    cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE))
                val postTypeStr = getEmailTypeString(postType)
                contactDTO.country = country
                contactDTO.city = city
                contactDTO.region = region
                contactDTO.street = street
                contactDTO.postCode = postcode
                contactDTO.postType = postType
                contactDTO.postTypeStr = postTypeStr
            }
            ContactsContract.CommonDataKinds.Identity.CONTENT_ITEM_TYPE -> {
                val identity =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Identity.IDENTITY))
                val namespace =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Identity.NAMESPACE))
                contactDTO.identity = identity
                contactDTO.namespace = namespace
            }
            ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE -> {
                val groupId =
                    cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID))
                contactDTO.groupId = groupId
            }
            ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE -> {
                val websiteUrl =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL))
                val websiteTypeInt =
                    cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.TYPE))
                val websiteTypeStr = getEmailTypeString(websiteTypeInt)
                val dataObj = DataDTO()
                dataObj.dataValue = websiteUrl
                dataObj.dataType = websiteTypeInt
                dataObj.dataTypeStr = websiteTypeStr
                contactDTO.websiteList.add(dataObj)
            }
            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE -> {
                val note =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE))
                contactDTO.note = note
            }
        }
        return contactDTO
    }

    private fun getRawContactsIdList(contentResolver: ContentResolver): List<Int> {
        val ret: MutableList<Int> = ArrayList()
        val rawContactUri = ContactsContract.RawContacts.CONTENT_URI
        val queryColumnArr = arrayOf(ContactsContract.RawContacts._ID)
        val cursor = contentResolver.query(
            rawContactUri, queryColumnArr, null, null,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC"
        )
        if (cursor != null && cursor.moveToFirst()) {
            cursor.moveToFirst()
            do {
                val idColumnIndex = cursor.getColumnIndex(ContactsContract.RawContacts._ID)
                val rawContactsId = cursor.getInt(idColumnIndex)
                ret.add(rawContactsId)
            } while (cursor.moveToNext())
        }
        cursor!!.close()
        return ret
    }

    /** READ CONTACT ------------------------------------------------------------------------- */
}