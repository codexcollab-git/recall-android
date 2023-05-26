package com.codexcollab.contactbackup.listners;

public interface PermissionsListener {
    void onPermissionGranted();
    void onPermissionDenied();
    void onPermissionPermanentlyDenied();
}
