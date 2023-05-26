package com.codexcollab.contactbackup.ui.splash.view

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import androidx.lifecycle.ViewModelProvider
import com.codexcollab.contactbackup.base.BaseActivity
import com.codexcollab.contactbackup.databinding.ActivitySplashBinding
import com.codexcollab.contactbackup.listners.DeepLinkListener
import com.codexcollab.contactbackup.ui.home.view.HomeActivity
import com.codexcollab.contactbackup.ui.splash.viewmodel.SplashViewModel
import com.codexcollab.contactbackup.ui.utils.DEEP_LINK
import com.codexcollab.contactbackup.ui.utils.DYNAMIC_LINK
import com.codexcollab.contactbackup.ui.utils.startActivityWithParams
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.messaging.FirebaseMessaging

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding, SplashViewModel>() {

    private var inAppUpdateManager: AppUpdateManager? = null
    private var updateRequest = 1001

    override fun inflateLayout(layoutInflater: LayoutInflater): ActivitySplashBinding =
        ActivitySplashBinding.inflate(layoutInflater)

    override fun initiateViewModel(viewModelProvider: ViewModelProvider): SplashViewModel =
        viewModelProvider[SplashViewModel::class.java]

    override fun initCreate() {
        super.initCreate()
        //setInAppUpdateListeners()
        init()
        viewModel.registerFCM()
        Log.d("Lokesh -> ", "initCreate: " + pref.fcmToken)
    }

    private fun setInAppUpdateListeners() {
        inAppUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoIntent = inAppUpdateManager?.appUpdateInfo
        appUpdateInfoIntent?.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                inAppUpdateManager?.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, updateRequest)
            } else init()
        }
    }

    override fun onResume() {
        super.onResume()
        inAppUpdateManager?.appUpdateInfo?.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                inAppUpdateManager?.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, updateRequest)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == updateRequest) {
            if (resultCode != RESULT_OK) { }
        }
    }

    private fun init(){
        if(intent != null){
            viewModel.parseDynamicLink(intent, object : DeepLinkListener{
                override fun onSuccess(uri: Uri?) {
                    if(uri != null) proceedToHome(uri.toString()) else proceedToHome()
                }
                override fun onFailure() {
                    proceedToHome()
                }
            })
        }else proceedToHome()
    }

    private fun proceedToHome(link : String? = null) {
        val dynamicLinkUri = intent.data.toString()
        val bundle = Bundle()
        bundle.putString(DEEP_LINK, link)
        bundle.putString(DYNAMIC_LINK, dynamicLinkUri)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivityWithParams(HomeActivity::class.java, bundleData = bundle)
            finish()
        }, 700)
    }
}