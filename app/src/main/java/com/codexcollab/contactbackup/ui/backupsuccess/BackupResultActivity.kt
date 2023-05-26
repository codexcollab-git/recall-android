package com.codexcollab.contactbackup.ui.backupsuccess

import android.content.ActivityNotFoundException
import android.content.Intent
import android.view.LayoutInflater
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.codexcollab.contactbackup.BuildConfig
import com.codexcollab.contactbackup.R
import com.codexcollab.contactbackup.base.BaseActivity
import com.codexcollab.contactbackup.databinding.ActivityBackupResultBinding
import com.codexcollab.contactbackup.ui.home.viewmodel.BackupViewModel
import com.codexcollab.contactbackup.ui.utils.FILE_NAME
import com.codexcollab.contactbackup.ui.utils.onClick
import com.codexcollab.contactbackup.ui.utils.showErrorDialog
import com.codexcollab.contactbackup.ui.utils.showToast
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task
import java.io.File


class BackupResultActivity : BaseActivity<ActivityBackupResultBinding, BackupViewModel>() {

    private var link : String? = null
    private var reviewManager: ReviewManager? = null
    private var reviewInfo: ReviewInfo? = null

    override fun inflateLayout(layoutInflater: LayoutInflater): ActivityBackupResultBinding = ActivityBackupResultBinding.inflate(layoutInflater)

    override fun initiateViewModel(viewModelProvider: ViewModelProvider): BackupViewModel = viewModelProvider[BackupViewModel::class.java]

    override fun initCreate() {
        super.initCreate()
        setData()
        buttonInit()
        setupInAppReview()
    }

    private fun setData(){
        link = pref.previousBackupLink
        val linkArr = link?.split("/")
        if (linkArr != null && linkArr.isNotEmpty()) {
            binding.code.text = linkArr[linkArr.indexOf(linkArr.last())]
        }
    }

    private fun buttonInit(){
        binding.backBtn.onClick { finish() }
        binding.shareLinkBtn.onClick { shareLink() }
        binding.shareFileBtn.onClick { shareFile() }
        binding.shareWhatsapp.onClick { shareLinkWhatsapp() }
    }

    private fun shareFile(){
        val file = File(pref.previousFilePath ?: "")
        if (file.exists()) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/*"
            val uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", File(pref.previousFilePath ?: ""))
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_contact_file))
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_contact_via))
            startActivity(Intent.createChooser(intent, FILE_NAME))
        }else{
            dialog = showErrorDialog(getString(R.string.backup_not_found), getString(R.string.cancel), dialog)
        }
    }

    private fun shareLink(){
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        share.putExtra(Intent.EXTRA_SUBJECT,  getString(R.string.share_contact_file))
        share.putExtra(Intent.EXTRA_TEXT, link)
        startActivity(Intent.createChooser(share, FILE_NAME))
    }

    private fun shareLinkWhatsapp(){
        val whatsappIntent = Intent(Intent.ACTION_SEND)
        whatsappIntent.type = "text/plain"
        whatsappIntent.setPackage("com.whatsapp")
        whatsappIntent.putExtra(Intent.EXTRA_TEXT,  getString(R.string.whatsapp_share)+"\n$link")
        try {
            startActivity(whatsappIntent)
        } catch (ex: ActivityNotFoundException) {
            showToast(getString(R.string.whatsapp_not_found))
        }
    }

    private fun setupInAppReview(){
        reviewManager = ReviewManagerFactory.create(this)
        val request : Task<ReviewInfo>? = reviewManager?.requestReviewFlow()
        request?.addOnCompleteListener {
            if (it.isSuccessful){
                reviewInfo = it.result
                reviewManager?.launchReviewFlow(this, reviewInfo!!)
            }else { /** Do Nothing */ }
        }
    }
}