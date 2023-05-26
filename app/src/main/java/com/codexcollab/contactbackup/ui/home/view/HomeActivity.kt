package com.codexcollab.contactbackup.ui.home.view

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.codexcollab.contactbackup.R
import com.codexcollab.contactbackup.base.BaseActivity
import com.codexcollab.contactbackup.databinding.ActivityHomeBinding
import com.codexcollab.contactbackup.databinding.BottomsheetBackupBinding
import com.codexcollab.contactbackup.listners.AuthListener
import com.codexcollab.contactbackup.ui.history.PreviousBackupActivity
import com.codexcollab.contactbackup.model.response.CommonResponse
import com.codexcollab.contactbackup.ui.home.viewmodel.BackupViewModel
import com.codexcollab.contactbackup.ui.backupsuccess.BackupResultActivity
import com.codexcollab.contactbackup.ui.restore.view.RestoreContactActivity
import com.codexcollab.contactbackup.ui.utils.*
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.extension.send
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class HomeActivity : BaseActivity<ActivityHomeBinding, BackupViewModel>() {

    private var backPressToExit = false
    private var downloadManager: DownloadManager? = null
    private lateinit var bsBackup: BottomSheetDialog
    private lateinit var bsBackupBinding: BottomsheetBackupBinding

    override fun inflateLayout(layoutInflater: LayoutInflater): ActivityHomeBinding =
        ActivityHomeBinding.inflate(layoutInflater)

    override fun initiateViewModel(viewModelProvider: ViewModelProvider): BackupViewModel =
        viewModelProvider[BackupViewModel::class.java]

    override fun initCreate() {
        super.initCreate()
        setDefaults()
        buttonInit()
        setupBackupBS()
        setupObservers()
        checkNotificationPermissions()
    }

    override fun observeNetwork(isNetworkAvailable: Boolean) {
        super.observeNetwork(isNetworkAvailable)
        if (!isNetworkAvailable) {
            binding.offlineRibbion.showView()
        } else {
            binding.offlineRibbion.goneView()
        }
    }

    private fun setDefaults() {
        checkForDeepLink()
        if (pref.previousBackupLink != null) binding.history.showView()
        else binding.history.goneView()
    }

    private fun checkForDeepLink(){
        val bundle = intent.getBundleExtra(BUNDLE_DATA)
        if(bundle != null){
            val link = bundle.getString(DEEP_LINK)
            val dynamicLink = bundle.getString(DYNAMIC_LINK)
            if(link != null){
                val initBundle = Bundle()
                initBundle.putString(DEEP_LINK, link)
                initBundle.putString(DYNAMIC_LINK, dynamicLink)
                startActivityWithParams(RestoreContactActivity::class.java, bundleData = initBundle)
            }
        }
    }

    private fun checkNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val builder = permissionsBuilder(android.Manifest.permission.POST_NOTIFICATIONS).build()
            builder.send { /** Do Nothing */ }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun buttonInit() {
        binding.upload.onClick {
           if(internetAvailable) checkForPermission()
           else dialog = showErrorDialog(getString(R.string.net_require), getString(R.string.cancel), dialog, getString(R.string.no_net))
        }
        binding.restore.setOnClickListener {
            startActivityWithParams(RestoreContactActivity::class.java)
        }
        binding.history.setOnClickListener {
            startActivityWithParams(PreviousBackupActivity::class.java)
        }
    }

    private fun checkForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) askForContactPermission()
        else askForContactAndStoragePermission()
    }

    private fun askForContactPermission(){
        val builder = permissionsBuilder(android.Manifest.permission.READ_CONTACTS).build()
        builder.send { result ->
            if (result.allGranted()) startBackingUpContact()
            else showPermissionNotGrantedDialog()
        }
    }

    private fun askForContactAndStoragePermission(){
        val builder = permissionsBuilder(android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE).build()
        builder.send { result ->
            if (result.allGranted()) startBackingUpContact()
            else showPermissionNotGrantedDialog()
        }
    }

    private fun startBackingUpContact() {
        viewModel.signInAnonymous(object : AuthListener{
            override fun onSuccess() {
                if (this@HomeActivity::bsBackup.isInitialized) bsBackup.show()
                viewModel.startUploadingContacts()
            }
            override fun onFailure() {
                /** Do Nothing */
            }
        })
    }

    private fun setupObservers() {
        viewModel.loading.observe(this, loadingObserver)
        viewModel.response.observe(this, backupContactObserver)
        viewModel.progressTxt.observe(this, progressTxtObserver)
    }

    private val progressTxtObserver = Observer<String> {
        if (this::bsBackup.isInitialized) bsBackupBinding.message.text = it
    }

    private val backupContactObserver = Observer<CommonResponse?> {
        if (this::bsBackup.isInitialized) bsBackup.dismiss()
        if (it != null && it.status) {
            viewModel.loading.value = false
            showToast(it.message ?: "")
            showDownloadNotification()
            startActivityWithParams(BackupResultActivity::class.java, isSingleTop = true)
        } else dialog = showErrorDialog(it.message ?: "", getString(R.string.cancel), dialog)
    }

    private fun showDownloadNotification() {
        try {
            downloadManager = this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager?.addCompletedDownload(
                FILE_NAME, FILE_NAME, true,
                "application/json", viewModel.file?.path, viewModel.file?.length() ?: 0, true
            )
        } catch (e: Exception) {
            Log.e("Download Manager -> ", e.message ?: "")
        }
    }

    private fun showPermissionNotGrantedDialog(){
        dialog = showMessageWithFunctionality(getString(R.string.permission_needed),  R.color.color_pop_red,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) getString(R.string.backup_permission_13) else getString(R.string.backup_permission),
            getString(R.string.setting), R.drawable.btn_ripple_full_blue, dialog){
            it.dismiss()
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", applicationContext.packageName, null)
            permanentDenied.launch(intent)
        }
    }

    private val permanentDenied =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                /** Do Nothing */
            }
        }

    private fun setupBackupBS() {
        bsBackup = BottomSheetDialog(this, R.style.BottomSheetDialog)
        bsBackupBinding = BottomsheetBackupBinding.inflate(LayoutInflater.from(this))
        bsBackup.setContentView(bsBackupBinding.root)
        bsBackup.dismissWithAnimation = true
        bsBackup.setCancelable(false)
        if (bsBackup.window != null) bsBackup.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        bsBackupBinding.cancelBtn.onClick { bsBackup.dismiss() }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (backPressToExit) {
            finish()
            super.onBackPressed()
            return
        }
        backPressToExit = true
        showToast(getString(R.string.press_back_to_exit))
        Handler(Looper.getMainLooper()).postDelayed({
            backPressToExit = false
        }, 2000)
    }
}