package com.crskdev.photosurfer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.crskdev.photosurfer.presentation.HasUpOrBackPressedAwareness


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

    override fun onBackPressed() {
        //TODO use a navigation framework approach?
        val navHostFragment = supportFragmentManager.fragments[0] // nav host fragment
        val topFragment = navHostFragment.childFragmentManager
                .takeIf { it.backStackEntryCount > 0 }
                ?.let {
                    var f:Fragment? = null
                    it.fragments.forEach{
                        if(it.isResumed){
                            f = it
                            return@forEach
                        }
                    }
                    f
                }
        if(topFragment!= null && topFragment is HasUpOrBackPressedAwareness){
            topFragment.onBackOrUpPressed()
            if(!topFragment.handleBack()){
                super.onBackPressed()
            }
        }else{
            super.onBackPressed()
        }

    }

}
