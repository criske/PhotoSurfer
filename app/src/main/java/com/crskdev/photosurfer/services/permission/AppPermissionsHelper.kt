package com.crskdev.photosurfer.services.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * Created by Cristian Pela on 07.08.2018.
 */
//TODO better permissions helper system
object AppPermissionsHelper {

    private const val STORAGE_PERMISSION_CODE = 1337

    private const val ENQUEUED_ACTION_ARGS_STORE = "app_permissions_enqueued_action_args_store"

    fun hasStoragePermission(context: Context): Boolean =
            ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    fun hasPermissions(context: Context, requiredPermissions: List<String>): Boolean =
            requiredPermissions.fold(true) { acc, curr ->
                acc && ContextCompat.checkSelfPermission(context, curr) == PackageManager.PERMISSION_GRANTED
            }

    fun requestPermission(activity: Activity, requiredPermissions: List<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, requiredPermissions.toTypedArray(), requestCode)
    }

    fun requestStoragePermission(activity: FragmentActivity, enqueueActionArg: String? = null) {
        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(activity, perms, STORAGE_PERMISSION_CODE)
        if (enqueueActionArg != null) {
            val queueStore = activity.getSharedPreferences(ENQUEUED_ACTION_ARGS_STORE, Context.MODE_PRIVATE)
            queueStore.edit()
                    .putString(STORAGE_PERMISSION_CODE.toString(), enqueueActionArg).apply()
        }
    }

    fun notifyPermissionGranted(activity: FragmentActivity, requestCode: Int, permissions: Array<out String>,
                                grantResults: IntArray) {
        val grantedPermissions = permissions.zip(grantResults.asList()) { l, r -> l to r }
                .asSequence()
                .filter { it.second == PackageManager.PERMISSION_GRANTED }
                .map { it.first }
                .toList()
        val queueStore = activity.getSharedPreferences(ENQUEUED_ACTION_ARGS_STORE, Context.MODE_PRIVATE)

        var argDispatched = false

        //recursively notify all the interested fragments and child fragments
        fun notifyAwareFragments(f: Fragment) {
            if (f is HasAppPermissionAwareness) {
                argDispatched = true
                f.onPermissionsGranted(grantedPermissions, queueStore.getString(requestCode.toString(), null))
            }
            f.childFragmentManager.fragments.forEach {
                notifyAwareFragments(it)
            }
        }
        activity.supportFragmentManager.fragments.forEach {
            notifyAwareFragments(it)
        }

        if (argDispatched) {
            queueStore.edit().remove(requestCode.toString()).apply()
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