package com.crskdev.photosurfer.presentation

/**
 * Created by Cristian Pela on 12.08.2018.
 */
interface HasAppPermissionAwareness {

    fun onPermissionsGranted(permissions: List<String>)

}