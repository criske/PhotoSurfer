package com.crskdev.photosurfer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.crskdev.photosurfer.presentation.HasUpOrBackAwareness


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.addOnBackStackChangedListener {

        }
    }

    override fun onSupportNavigateUp() = findNavController(R.id.nav_host_fragment).navigateUp()

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        AppPermissions.showPermissionsGrantingStatus(this, requestCode, permissions, grantResults)
    }

//    override fun onBackPressed() {
//        //TODO use a navigation framework aproach
//        val fragments = supportFragmentManager.fragments
//        if (supportFragmentManager.backStackEntryCount > 0) {
//            val currentFragment = supportFragmentManager.let {
//                val lastIndex = it.backStackEntryCount - 1
//                it.getBackStackEntryAt(lastIndex)
//            }
//            if (currentFragment is HasUpOrBackAwareness) {
//                currentFragment.onBackOrUpPressed()
//                if (!currentFragment.handleBack()) {
//                    super.onBackPressed()
//                }
//            }
//        } else {
//            super.onBackPressed()
//        }
//        val fragments2 = supportFragmentManager.fragments
//        val br = true
//    }

}
