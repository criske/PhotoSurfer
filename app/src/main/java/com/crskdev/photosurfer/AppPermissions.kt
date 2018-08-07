package com.crskdev.photosurfer

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Created by Cristian Pela on 07.08.2018.
 */
object AppPermissions {

    private val STORAGE_PERMISSION_CODE = 1337

    fun hasStoragePermission(context: Context): Boolean =
            ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    fun requestStoragePermission(activity: Activity) {
        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(activity, perms, STORAGE_PERMISSION_CODE)
    }

    fun showPermissionsGrantingStatus(context: Context, requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            STORAGE_PERMISSION_CODE -> {
                val grantSum = grantResults.sum()
                val message = when (grantSum) {
                    0 -> "Storage permission granted. Now try again the action"
                    -1 -> "Storage permission partial granted."
                    else -> "Storage permission denied"
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}