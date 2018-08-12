package com.crskdev.photosurfer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import com.crskdev.photosurfer.presentation.HasUpOrBackPressedAwareness


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onSupportNavigateUp() = findNavController(R.id.nav_host_fragment).navigateUp()

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        AppPermissions.notifyPermissionGranted(this, permissions, grantResults)
    }

    override fun onBackPressed() {
        val topFragment = findTopFragment()
        if (topFragment != null && topFragment is HasUpOrBackPressedAwareness) {
            topFragment.onBackOrUpPressed()
            if (!topFragment.handleBack()) {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }

    }

}

fun FragmentActivity.findTopFragment(): Fragment? {
    val navHostFragment = this.supportFragmentManager.fragments[0] // nav host fragment
    return navHostFragment.findTopChildFragment()
}

fun Fragment.findTopChildFragment(): Fragment? {
    return childFragmentManager
            .takeIf { it.backStackEntryCount > 0 }
            ?.fragments
            ?.firstOrNull { f -> f.isResumed }
}
