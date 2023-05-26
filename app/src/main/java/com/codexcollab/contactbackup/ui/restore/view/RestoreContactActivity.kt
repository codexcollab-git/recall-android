package com.codexcollab.contactbackup.ui.restore.view

import android.view.LayoutInflater
import androidx.lifecycle.ViewModelProvider
import com.codexcollab.contactbackup.base.BaseActivity
import com.codexcollab.contactbackup.databinding.ActivityRestoreContactBinding
import com.codexcollab.contactbackup.adapters.TabAdapter
import com.codexcollab.contactbackup.ui.restore.viewmodel.RestoreViewModel
import com.codexcollab.contactbackup.ui.utils.goneView
import com.codexcollab.contactbackup.ui.utils.onClick
import com.codexcollab.contactbackup.ui.utils.showView
import com.google.android.material.tabs.TabLayout

class RestoreContactActivity : BaseActivity<ActivityRestoreContactBinding, RestoreViewModel>() {

    override fun inflateLayout(layoutInflater: LayoutInflater): ActivityRestoreContactBinding =
        ActivityRestoreContactBinding.inflate(layoutInflater)

    override fun initiateViewModel(viewModelProvider: ViewModelProvider): RestoreViewModel =
        viewModelProvider[RestoreViewModel::class.java]

    override fun initCreate() {
        super.initCreate()
        setupTabLayout()
        setupViewPager()
        buttonInit()
    }

    private fun setupViewPager() {
        binding.viewPager.apply {
            adapter = TabAdapter(this@RestoreContactActivity, supportFragmentManager)
            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(binding.tablayout))
        }
    }

    override fun observeNetwork(isNetworkAvailable: Boolean) {
        super.observeNetwork(isNetworkAvailable)
        if (!isNetworkAvailable) {
            binding.offlineRibbion.showView()
        } else {
            binding.offlineRibbion.goneView()
        }
    }

    private fun setupTabLayout() {
        binding.tablayout.apply {
            addTab(this.newTab().setText("Link"))
            addTab(this.newTab().setText("File"))
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.position?.let {
                        binding.viewPager.currentItem = it
                    }
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }
                override fun onTabReselected(tab: TabLayout.Tab?) {
                }
            })
        }
    }

    private fun buttonInit(){
        binding.backBtn.onClick { finish() }
    }
}