package com.codexcollab.contactbackup.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.codexcollab.contactbackup.customdialog.CustomDialogBuilder
import com.codexcollab.contactbackup.ui.utils.NetworkManager
import com.codexcollab.contactbackup.ui.utils.PrefUtils
import com.codexcollab.contactbackup.ui.utils.internetAvailable
import com.codexcollab.contactbackup.ui.utils.showLoaderDialog
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

abstract class BaseActivity<VB : ViewBinding, VM : ViewModel> : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var pref: PrefUtils
    lateinit var binding: VB
    var dialog: CustomDialogBuilder? = null
    var loaderDialog: CustomDialogBuilder? = null
    private var networkManager: NetworkManager? = null

    val viewModel: VM by lazy {
        initiateViewModel(ViewModelProvider(this, viewModelFactory))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = inflateLayout(layoutInflater)
        setContentView(binding.root)
        initNetwork()
        initCreate()
    }

    private fun initNetwork() {
        networkManager = NetworkManager(this)
        networkManager!!.register().observe(this, networkObserver)
    }

    abstract fun inflateLayout(layoutInflater: LayoutInflater): VB

    abstract fun initiateViewModel(viewModelProvider: ViewModelProvider): VM

    open fun initCreate() {}

    val loadingObserver = Observer<Boolean> {
        if (it) loaderDialog = showLoaderDialog(prevDialog = loaderDialog)
        else loaderDialog?.dialogInstance?.dismissLoader()
    }

    private val networkObserver = Observer<Boolean?> {
        it?.let {
            internetAvailable = it
            observeNetwork(it)
        }
    }
    open fun observeNetwork(isNetworkAvailable: Boolean) {}

    override fun onDestroy() {
        super.onDestroy()
        networkManager?.unRegister()
    }
}
