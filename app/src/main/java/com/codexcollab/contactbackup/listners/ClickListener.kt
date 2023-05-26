package com.codexcollab.contactbackup.listners


fun interface ClickListener<T> {
    fun onClick(result: T)
}