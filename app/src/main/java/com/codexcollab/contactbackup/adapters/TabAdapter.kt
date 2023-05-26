package com.codexcollab.contactbackup.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.codexcollab.contactbackup.ui.restore.view.RestoreViaFileFragment
import com.codexcollab.contactbackup.ui.restore.view.RestoreViaLinkFragment


class TabAdapter(var context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                RestoreViaLinkFragment()
            }
            1 -> {
                RestoreViaFileFragment()
            }
            else -> getItem(position)
        }
    }
    override fun getCount() = 2
}