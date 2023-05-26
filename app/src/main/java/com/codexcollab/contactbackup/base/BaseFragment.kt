package com.codexcollab.contactbackup.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.codexcollab.contactbackup.customdialog.CustomDialogBuilder
import com.codexcollab.contactbackup.ui.utils.PrefUtils
import com.codexcollab.contactbackup.ui.utils.showLoaderDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject

abstract class BaseFragment<VB : ViewBinding, VM : ViewModel>(layoutId: Int) :
    DaggerFragment(layoutId) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var pref: PrefUtils
    lateinit var binding: VB
    var dialog: CustomDialogBuilder? = null
    var loaderDialog: CustomDialogBuilder? = null

    val viewModel: VM by lazy {
        initiateViewModel(ViewModelProvider(this, viewModelFactory))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflateLayout(inflater, container, false)
        initCreate()
        return binding.root
    }

    abstract fun inflateLayout(
        layoutInflater: LayoutInflater,
        container: ViewGroup?,
        attachToContext: Boolean
    ): VB

    abstract fun initiateViewModel(viewModelProvider: ViewModelProvider): VM

    open fun initCreate() {}

    val loadingObserver = Observer<Boolean> {
        if (it) loaderDialog = requireContext().showLoaderDialog(prevDialog = loaderDialog)
        else loaderDialog?.dialogInstance?.dismissLoader()
    }
}