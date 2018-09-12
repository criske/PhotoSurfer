package com.crskdev.photosurfer

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.crskdev.photosurfer.data.remote.APICallDispatcher
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.presentation.HasUpOrBackPressedAwareness
import com.crskdev.photosurfer.util.Listenable
import com.crskdev.photosurfer.util.livedata.ListenableLiveData
import com.crskdev.photosurfer.util.livedata.viewModelFromProvider
import com.crskdev.photosurfer.util.setAlphaComponent


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val viewModel = viewModelFromProvider(this) {
            MainActivityViewModel(dependencyGraph().apiCallDispatcher)
        }
//        if (BuildConfig.DEBUG) {
//            viewModel.apiCallStateLiveData.observe(this, Observer {
//                if (!(savedInstanceState != null && it == APICallDispatcher.State.EXECUTED)) {
//                    Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
//                }
//            })
//        }
    }

    override fun onSupportNavigateUp() = findNavController(R.id.nav_host_fragment).navigateUp()

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        AppPermissionsHelper.notifyPermissionGranted(this, permissions, grantResults)
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

class MainActivityViewModel(listenableApiCallState: Listenable<APICallDispatcher.State>) : ViewModel() {

    val apiCallStateLiveData: LiveData<APICallDispatcher.State> = ListenableLiveData(listenableApiCallState)
            .toNonSingleLiveData()
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

fun FragmentActivity.setWindowFlag(bits: Int, on: Boolean) {
    val winParams = window.attributes;
    if (on) {
        winParams.flags = winParams.flags or bits;
    } else {
        winParams.flags = winParams.flags and bits.inv()
    }
    window.attributes = winParams;
}


fun FragmentActivity.setStatusBarColor(@ColorInt color: Int, alpha: Float = 1f) {
    if (Build.VERSION.SDK_INT in 19..20) {
        setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
    }
//    if (Build.VERSION.SDK_INT >= 19) {
//        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
//                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//    }
    if (Build.VERSION.SDK_INT >= 21) {
        // setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        window.statusBarColor = color.setAlphaComponent(alpha)
    }
}