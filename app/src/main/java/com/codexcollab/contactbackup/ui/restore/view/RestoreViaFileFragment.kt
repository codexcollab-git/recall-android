package com.codexcollab.contactbackup.ui.restore.view

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.codexcollab.contactbackup.R
import com.codexcollab.contactbackup.base.BaseFragment
import com.codexcollab.contactbackup.databinding.BottomsheetRestoreBinding
import com.codexcollab.contactbackup.databinding.FragmentRestoreViaFileBinding
import com.codexcollab.contactbackup.listners.AuthListener
import com.codexcollab.contactbackup.model.response.CommonResponse
import com.codexcollab.contactbackup.ui.restore.viewmodel.RestoreViewModel
import com.codexcollab.contactbackup.ui.utils.*
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.extension.send
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task
import java.io.File


class RestoreViaFileFragment :
    BaseFragment<FragmentRestoreViaFileBinding, RestoreViewModel>(R.layout.fragment_restore_via_file) {

    private lateinit var bsRestore: BottomSheetDialog
    private lateinit var bsRestoreBinding: BottomsheetRestoreBinding
    private var reviewManager: ReviewManager? = null
    private var reviewInfo: ReviewInfo? = null

    override fun inflateLayout(
        layoutInflater: LayoutInflater,
        container: ViewGroup?,
        attachToContext: Boolean
    ): FragmentRestoreViaFileBinding =
        FragmentRestoreViaFileBinding.inflate(layoutInflater, container, attachToContext)

    override fun initiateViewModel(viewModelProvider: ViewModelProvider): RestoreViewModel =
        viewModelProvider[RestoreViewModel::class.java]

    @RequiresApi(Build.VERSION_CODES.R)
    override fun initCreate() {
        super.initCreate()
        buttonInit()
        setupRestoreBS()
        setupObservers()
    }

    private fun buttonInit() {
        binding.restoreBtn.onClick { checkIfAlreadyRestored() }
    }

    private fun checkIfAlreadyRestored(){
        if (pref.alreadyRestored){
            dialog = requireContext().showMessageWithFunctionality(getString(R.string.warning), R.color.color_pop_red, getString(R.string.confirm_restore_again),
                getString(R.string.restore_again), R.drawable.btn_ripple_full_blue, dialog){
                it.dismiss()
                checkForPermission()
            }
        }else checkForPermission()
    }

    private fun checkForPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) openFile()
        else askForStoragePermission()
    }

    private fun askForStoragePermission(){
        val builder = permissionsBuilder(android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.WRITE_CONTACTS,
            android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE).build()
        builder.send { result ->
            if (result.allGranted()) openFile()
            else showPermissionNotGrantedDialog()
        }
    }

    private fun openFile() {
        viewModel.signInAnonymous(object : AuthListener {
            override fun onSuccess() {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "text/plain"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                restoreViaFile.launch(intent)
            }
            override fun onFailure() {
                /** Do Nothing */
            }
        })
    }

    private val restoreViaFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                if (uri != null) {
                    if(internetAvailable) {
                        if (this::bsRestore.isInitialized) bsRestore.show()
                        val file = File(requireActivity().fileFromContentUri(uri).toString())
                        viewModel.parseContactFile(file)
                    } else dialog = requireContext().showErrorDialog(getString(R.string.net_require), getString(R.string.cancel), dialog, getString(R.string.no_net))
                }
            }
        }

    private fun setupRestoreBS() {
        bsRestore = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        bsRestoreBinding = BottomsheetRestoreBinding.inflate(LayoutInflater.from(requireContext()))
        bsRestore.setContentView(bsRestoreBinding.root)
        bsRestore.dismissWithAnimation = true
        bsRestore.setCancelable(false)
        if (bsRestore.window != null) bsRestore.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        bsRestoreBinding.cancelBtn.onClick { bsRestore.dismiss() }
    }

    private fun setupObservers() {
        viewModel.loading.observe(this, loadingObserver)
        viewModel.response.observe(this, restoreContactObserver)
        viewModel.progressTxt.observe(this, progressTxtObserver)
    }

    private val progressTxtObserver = Observer<String> {
        if(this::bsRestore.isInitialized) bsRestoreBinding.message.text = if(it != null && it != "") it else getString(R.string.please_wait)
    }

    private val restoreContactObserver = Observer<CommonResponse?> {
        if (this::bsRestore.isInitialized) bsRestore.dismiss()
        if (it != null && it.status) {
            viewModel.loading.value = false
            pref.alreadyRestored = true
            restoreSuccess()
            setupInAppReview()
        } else dialog = requireContext().showErrorDialog(it.message ?: "", getString(R.string.cancel), dialog)
    }

    private fun showPermissionNotGrantedDialog(){
        dialog = requireContext().showMessageWithFunctionality(getString(R.string.permission_needed),  R.color.color_pop_red,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) getString(R.string.restore_permission_13) else getString(R.string.restore_permission),
            getString(R.string.setting), R.drawable.btn_ripple_full_blue, dialog){
            it.dismiss()
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", requireContext().packageName, null)
            permanentDenied.launch(intent)
        }
    }

    private val permanentDenied = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            /** Do Nothing */
        }
    }

    private fun restoreSuccess(){
        dialog = requireContext().showMessageWithFunctionality(getString(R.string.restore_success_head),  R.color.color_pop_secondary, getString(R.string.restore_success_txt),
            getString(R.string.cont), R.drawable.btn_ripple_full_blue, dialog){
            it.dismiss()
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}")))
        }
    }

    private fun setupInAppReview(){
        reviewManager = ReviewManagerFactory.create(requireActivity())
        val request : Task<ReviewInfo>? = reviewManager?.requestReviewFlow()
        request?.addOnCompleteListener {
            if (it.isSuccessful){
                reviewInfo = it.result
                reviewManager?.launchReviewFlow(requireActivity(), reviewInfo!!)
            }else { /** Do Nothing */ }
        }
    }
}