package com.codexcollab.contactbackup.ui.restore.view

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.codexcollab.contactbackup.R
import com.codexcollab.contactbackup.base.BaseFragment
import com.codexcollab.contactbackup.databinding.BottomsheetRestoreBinding
import com.codexcollab.contactbackup.databinding.FragmentRestoreViaLinkBinding
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

class RestoreViaLinkFragment : BaseFragment<FragmentRestoreViaLinkBinding, RestoreViewModel>(R.layout.fragment_restore_via_link) {

    private lateinit var bsRestore : BottomSheetDialog
    private lateinit var bsRestoreBinding : BottomsheetRestoreBinding
    private var reviewManager: ReviewManager? = null
    private var reviewInfo: ReviewInfo? = null

    override fun inflateLayout(
        layoutInflater: LayoutInflater,
        container: ViewGroup?,
        attachToContext: Boolean
    ): FragmentRestoreViaLinkBinding = FragmentRestoreViaLinkBinding.inflate(layoutInflater, container, attachToContext)

    override fun initiateViewModel(viewModelProvider: ViewModelProvider): RestoreViewModel = viewModelProvider[RestoreViewModel::class.java]

    override fun initCreate() {
        super.initCreate()
        checkForDeepLink()
        buttonInit()
        setupRestoreBS()
        setupObservers()
    }

    private fun checkForDeepLink(){
        val bundle = requireActivity().intent.getBundleExtra(BUNDLE_DATA)
        if(bundle != null){
            val link = bundle.getString(DEEP_LINK)
            val dynamicLink = bundle.getString(DYNAMIC_LINK)
            if(link != null){
                val arrLink = dynamicLink?.split("/")
                val last = arrLink?.get(arrLink.size - 1)
                binding.code.setText(last)
                dialog = requireContext().showMessageWithFunctionality(getString(R.string.restore_contact),  R.color.color_pop_secondary, getString(R.string.restore_contact_txt2),
                    getString(R.string.confirm), R.drawable.btn_ripple_full_blue, dialog){
                    it.dismiss()
                    checkIfAlreadyRestored()
                }
            }
        }
    }

    private fun buttonInit(){
        binding.restoreBtn.onClick { checkIfAlreadyRestored() }
    }

    private fun checkIfAlreadyRestored(){
        if (pref.alreadyRestored){
            dialog = requireContext().showMessageWithFunctionality(getString(R.string.warning), R.color.color_pop_red, getString(R.string.confirm_restore_again),
                getString(R.string.restore_again), R.drawable.btn_ripple_full_blue, dialog){
                it.dismiss()
                askForContactPermission()
            }
        }else askForContactPermission()
    }

    private fun askForContactPermission(){
        val builder = permissionsBuilder(android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.WRITE_CONTACTS).build()
        builder.send { result ->
            if (result.allGranted()) {
                viewModel.signInAnonymous(object : AuthListener {
                    override fun onSuccess() {
                        startRestoringContact()
                    }
                    override fun onFailure() {
                        /** Do Nothing */
                    }
                })
            }
            else showPermissionNotGrantedDialog()
        }
    }

    private fun startRestoringContact() {
        val code = binding.code.text
        if(!TextUtils.isEmpty(code)){
            if(internetAvailable) {
                val link = "https://recall.codexcollab.com/$code"
                val intent = Intent()
                intent.data = Uri.parse(link)
                if(this::bsRestore.isInitialized) bsRestore.show()
                viewModel.parseDynamicLink(intent)
            } else dialog = requireContext().showErrorDialog(getString(R.string.net_require), getString(R.string.cancel), dialog, getString(R.string.no_net))
        }else dialog = requireContext().showErrorDialog(getStringResource(R.string.code_txt), getString(R.string.cancel), dialog)
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
        if(this::bsRestore.isInitialized) bsRestore.dismiss()
        if (it != null && it.status) {
            viewModel.loading.value = false
            pref.alreadyRestored = true
            restoreSuccess()
            setupInAppReview()
        } else dialog = requireContext().showErrorDialog(it.message ?: "", getString(R.string.cancel), dialog)
    }

    private fun showPermissionNotGrantedDialog(){
        dialog = requireContext().showMessageWithFunctionality(getString(R.string.permission_needed),  R.color.color_pop_red, getString(R.string.restore_permission_13),
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