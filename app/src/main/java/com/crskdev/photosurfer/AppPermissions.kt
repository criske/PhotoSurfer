package com.crskdev.photosurfer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.crskdev.photosurfer.presentation.HasAppPermissionAwareness

/**
 * Created by Cristian Pela on 07.08.2018.
 */
object AppPermissions {

    private const val STORAGE_PERMISSION_CODE = 1337

    fun hasStoragePermission(context: Context): Boolean =
            ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    fun requestStoragePermission(activity: FragmentActivity) {
        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(activity, perms, STORAGE_PERMISSION_CODE)
    }

    fun notifyPermissionGranted(activity: FragmentActivity, permissions: Array<out String>,
                                grantResults: IntArray) {
        val grantedPermissions = permissions.zip(grantResults.asList()) { l, r -> l to r }
                .filter { it.second == PackageManager.PERMISSION_GRANTED }
                .map { it.first }
        fun notifyAwareFragments(f: Fragment){
            if(f is HasAppPermissionAwareness){
                f.onPermissionsGranted(grantedPermissions)
            }
            f.childFragmentManager.fragments.forEach {
                notifyAwareFragments(it)
            }
        }
        activity.supportFragmentManager.fragments.forEach {
            notifyAwareFragments(it)
        }
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