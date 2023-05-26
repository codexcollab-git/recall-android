package com.codexcollab.contactbackup.ui.restore.viewmodel

import android.app.Application
import android.content.*
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.codexcollab.contactbackup.R
import com.codexcollab.contactbackup.application.App
import com.codexcollab.contactbackup.base.BaseViewModel
import com.codexcollab.contactbackup.commonrepo.CommonRepository
import com.codexcollab.contactbackup.listners.AuthListener
import com.codexcollab.contactbackup.model.response.CommonResponse
import com.codexcollab.contactbackup.model.contact.ContactDTO
import com.codexcollab.contactbackup.model.contact.DataDTO
import com.codexcollab.contactbackup.ui.utils.getStringResource
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


class RestoreViewModel @Inject constructor(
    app: Application,
    private val repo: CommonRepository
) : BaseViewModel(app) {

    private val _response: MutableLiveData<CommonResponse?> = MutableLiveData()
    val response: LiveData<CommonResponse?> = _response
    val progressTxt = MutableLiveData("")
    var file: File? = null

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

    private fun getSuccessResponse(): CommonResponse {
        val response = CommonResponse()
        response.status = true
        response.message = getStringResource(R.string.restore_success)
        return response
    }

    private fun getErrorResponse(error: String): CommonResponse {
        val response = CommonResponse()
        response.status = false
        response.message = error
        return response
    }

    /** WRITE CONTACT ------------------------------------------------------------------------ */

    private fun readContactFile(path: File?): String {
        val stringBuilder = StringBuilder()
        var line: String?
        try {
            val bf = BufferedReader(FileReader(path))
            while (bf.readLine().also { line = it } != null) stringBuilder.append(line)
        } catch (e: FileNotFoundException) {
            Log.e("Error -> ", "${e.message}")
        } catch (e: IOException) {
            Log.e("Error -> ", "${e.message}")
        }
        return stringBuilder.toString().trim()
    }

    fun parseContactFile(file: File){
        val encryptedContacts = readContactFile(file)
        startWritingContact(encryptedContacts)
    }

    fun parseDynamicLink(intent: Intent) {
        val dynamicTask = firebaseRef.parseDynamicLink(intent)
        dynamicTask.addOnSuccessListener {
            if (it != null) {
                val link = it.link.toString()
                val linkArr = link.split("/")
                if (linkArr.isNotEmpty()) {
                    val code = linkArr[4]
                    readContactsFromFirebase(code)
                }else _response.value = getErrorResponse(getStringResource(R.string.link_error))
            }else _response.value = getErrorResponse(getStringResource(R.string.restore_error))
        }.addOnFailureListener {
            _response.value = getErrorResponse(it.message ?: getStringResource(R.string.restore_error))
        }
    }

    private fun readContactsFromFirebase(code : String){
        val path = firebaseRef.contactRef.child(code)
        path.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val encryptedContacts = snapshot.child("contacts").value.toString()
                    startWritingContact(encryptedContacts)
                }else _response.value = getErrorResponse(getStringResource(R.string.link_no_data_error))
            }
            override fun onCancelled(error: DatabaseError) {
                _response.value = getErrorResponse(error.message)
            }
        })
    }

    fun startWritingContact(fileContents: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val decrypted = repo.decrypt(fileContents)
            val itemType = object : TypeToken<List<ContactDTO>>() {}.type
            val contactList = Gson().fromJson<ArrayList<ContactDTO>>(decrypted, itemType)
            contactList?.forEachIndexed { index, contactDTO ->
                addContact(contactDTO, App.getAppContext().contentResolver)
                delay(10)
                if (index == contactList.size - 1) {
                    viewModelScope.launch(Dispatchers.Main) {
                        val successResponse = getSuccessResponse()
                        _response.value = successResponse
                    }
                }
            }
        }
    }

    private fun addContact(contactDTO: ContactDTO, contentResolver: ContentResolver) {
        viewModelScope.launch(Dispatchers.IO) {
            contactDTO.rawContactId = insertContact(contentResolver, contactDTO)

            insertGroupId(contentResolver, contactDTO.groupId, contactDTO.rawContactId)

            insertListData(
                contentResolver, ContactsContract.Data.CONTENT_URI,
                contactDTO.rawContactId,
                ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.SipAddress.TYPE,
                ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS,
                contactDTO.addressList
            )

            insertOrganization(contentResolver, contactDTO)

            insertName(contentResolver, contactDTO)

            insertListData(
                contentResolver, ContactsContract.Data.CONTENT_URI,
                contactDTO.rawContactId,
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Email.TYPE,
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                contactDTO.emailList
            )

            insertNickName(contentResolver, contactDTO)

            insertNote(contentResolver, contactDTO)

            insertListData(
                contentResolver, ContactsContract.Data.CONTENT_URI,
                contactDTO.rawContactId,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                contactDTO.phoneList
            )

            insertListData(
                contentResolver, ContactsContract.Data.CONTENT_URI,
                contactDTO.rawContactId,
                ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Website.TYPE,
                ContactsContract.CommonDataKinds.Website.URL,
                contactDTO.websiteList
            )

            insertListData(
                contentResolver, ContactsContract.Data.CONTENT_URI,
                contactDTO.rawContactId,
                ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Im.PROTOCOL,
                ContactsContract.CommonDataKinds.Im.DATA,
                contactDTO.imList
            )

            insertPostalAddress(contentResolver, contactDTO)

            insertIdentity(contentResolver, contactDTO)

            val name = contactDTO.displayName ?: contactDTO.nickName
            viewModelScope.launch(Dispatchers.Main) {
                progressTxt.value = "$name ${getStringResource(R.string.restored_txt)}"
            }
        }
    }

    private fun insertContact(contentResolver: ContentResolver, contactDto: ContactDTO): Long {
        val contentValues = ContentValues()
        contentValues.put(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY, contactDto.displayName)
        contentValues.put(
            ContactsContract.RawContacts.DISPLAY_NAME_ALTERNATIVE,
            contactDto.displayName
        )
        val rawContactUri =
            contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, contentValues)
        return ContentUris.parseId(rawContactUri!!)
    }

    private fun insertGroupId(
        contentResolver: ContentResolver,
        groupRowId: Int,
        rawContactId: Long
    ) {
        val contentValues = ContentValues()
        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)

        contentValues.put(
            ContactsContract.CommonDataKinds.StructuredName.MIMETYPE,
            ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
        )
        contentValues.put(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, groupRowId)
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues)
    }

    private fun insertListData(
        contentResolver: ContentResolver,
        contentUri: Uri,
        rawContactId: Long,
        mimeType: String,
        dataTypeColumnName: String,
        dataValueColumnName: String,
        dataList: List<DataDTO>?
    ) {
        if (dataList != null) {
            val contentValues = ContentValues()
            val size = dataList.size
            for (i in 0 until size) {
                val dataDto = dataList[i]
                contentValues.clear()
                contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                contentValues.put(ContactsContract.Data.MIMETYPE, mimeType)
                contentValues.put(dataTypeColumnName, dataDto.dataType)
                contentValues.put(dataValueColumnName, dataDto.dataValue)
                contentResolver.insert(contentUri, contentValues)
            }
        }
    }

    private fun insertOrganization(contentResolver: ContentResolver, contactDto: ContactDTO?) {
        if (contactDto != null) {
            val contentValues = ContentValues()
            contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, contactDto.rawContactId)
            contentValues.put(
                ContactsContract.CommonDataKinds.Organization.MIMETYPE,
                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
            )
            contentValues.put(
                ContactsContract.CommonDataKinds.Organization.COMPANY,
                contactDto.company
            )
            contentValues.put(
                ContactsContract.CommonDataKinds.Organization.DEPARTMENT,
                contactDto.department
            )
            contentValues.put(
                ContactsContract.CommonDataKinds.Organization.TITLE,
                contactDto.title
            )
            contentValues.put(
                ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION,
                contactDto.jobDescription
            )
            contentValues.put(
                ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION,
                contactDto.officeLocation
            )
            contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues)
        }
    }

    private fun insertName(contentResolver: ContentResolver, contactDto: ContactDTO?) {
        if (contactDto != null) {
            val contentValues = ContentValues()
            contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, contactDto.rawContactId)
            contentValues.put(
                ContactsContract.CommonDataKinds.StructuredName.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
            )
            contentValues.put(
                ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                contactDto.displayName
            )
            contentValues.put(
                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                contactDto.givenName
            )
            contentValues.put(
                ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                contactDto.familyName
            )
            contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues)
        }
    }

    private fun insertNickName(contentResolver: ContentResolver, contactDto: ContactDTO?) {
        if (contactDto != null) {
            val contentValues = ContentValues()
            contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, contactDto.rawContactId)
            contentValues.put(
                ContactsContract.CommonDataKinds.Nickname.MIMETYPE,
                ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE
            )
            contentValues.put(
                ContactsContract.CommonDataKinds.Nickname.NAME,
                contactDto.nickName
            )
            contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues)
        }
    }

    private fun insertNote(contentResolver: ContentResolver, contactDto: ContactDTO?) {
        if (contactDto != null) {
            val contentValues = ContentValues()
            contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, contactDto.rawContactId)
            contentValues.put(
                ContactsContract.CommonDataKinds.Note.MIMETYPE,
                ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
            )
            contentValues.put(ContactsContract.CommonDataKinds.Note.NOTE, contactDto.note)
            contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues)
        }
    }

    private fun insertPostalAddress(contentResolver: ContentResolver, contactDto: ContactDTO) {
        val contentValues = ContentValues()
        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, contactDto.rawContactId)
        contentValues.put(
            ContactsContract.CommonDataKinds.StructuredPostal.MIMETYPE,
            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
        )
        contentValues.put(
            ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY,
            contactDto.country
        )
        contentValues.put(
            ContactsContract.CommonDataKinds.StructuredPostal.CITY,
            contactDto.city
        )
        contentValues.put(
            ContactsContract.CommonDataKinds.StructuredPostal.REGION,
            contactDto.region
        )
        contentValues.put(
            ContactsContract.CommonDataKinds.StructuredPostal.STREET,
            contactDto.street
        )
        contentValues.put(
            ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
            contactDto.postCode
        )
        contentValues.put(
            ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
            contactDto.postType
        )
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues)
    }

    private fun insertIdentity(contentResolver: ContentResolver, contactDto: ContactDTO) {
        val contentValues = ContentValues()
        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, contactDto.rawContactId)
        contentValues.put(
            ContactsContract.CommonDataKinds.Identity.MIMETYPE,
            ContactsContract.CommonDataKinds.Identity.CONTENT_ITEM_TYPE
        )
        contentValues.put(
            ContactsContract.CommonDataKinds.Identity.IDENTITY,
            contactDto.identity
        )
        contentValues.put(
            ContactsContract.CommonDataKinds.Identity.NAMESPACE,
            contactDto.namespace
        )
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues)
    }

    /** WRITE CONTACT ------------------------------------------------------------------------ */
}