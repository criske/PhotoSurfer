package com.crskdev.photosurfer.services.permission

import android.os.Bundle

/**
 * Created by Cristian Pela on 12.08.2018.
 */
interface HasAppPermissionAwareness {

    fun onPermissionsGranted(permissions: List<String>, enqueuedActionArg: String?)

}