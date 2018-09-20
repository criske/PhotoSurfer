package com.crskdev.photosurfer.services.permission

import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Cristian Pela on 19.09.2018.
 */
class AppPermissionsGlobalComponentManager(application: Application) {

    private val requestCodeGenerator = AtomicInteger(1000)

    private val components: ConcurrentHashMap<Int, HasAppPermissionAwarenessGlobalComponent> =
            ConcurrentHashMap()

    fun onActivityInjected(activity: Activity) {
        components.entries.forEach {
            AppPermissionsHelper.requestPermission(activity, it.value.requiredPermissions, it.key)
        }
    }

    fun notifyPermissionGranted(activity: FragmentActivity, requestCode: Int, permissions: Array<out String>,
                                grantResults: IntArray) {
        permissions.zip(grantResults.asList()) { l, r -> l to r }
                .asSequence()
                .filter { it.second == PackageManager.PERMISSION_GRANTED }
                .map { it.first }
                .toList()
                .takeIf { it.isNotEmpty() }
                ?.apply {
                    components.get(requestCode)?.onPermissionsGranted(this, null)
                }
    }

    fun registerPermissionAwarenessGlobalComponent(component: HasAppPermissionAwarenessGlobalComponent) {
        val code = requestCodeGenerator.incrementAndGet()
        components.putIfAbsent(code, component)
    }

}

interface HasAppPermissionAwarenessGlobalComponent : HasAppPermissionAwareness {

    val requiredPermissions: List<String>

}