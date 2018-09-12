package com.crskdev.photosurfer.presentation

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.remote.auth.AuthTokenStorage
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.util.livedata.SingleLiveEvent

/**
 * Created by Cristian Pela on 17.08.2018.
 */
abstract class AuthAwareFragment<T : AuthAwareViewModel> : Fragment() {

    abstract val viewModel: T

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.errorAuthLiveData.observe(this, Observer {
            val controller = Navigation.findNavController(activity!!, R.id.nav_host_fragment)
            context!!.dependencyGraph().authNavigatorMiddleware.navigateToLogin(controller)
        })
    }

}

open class AuthAwareViewModel(private val authTokenStorage: AuthTokenStorage) : ViewModel() {
    val errorAuthLiveData = SingleLiveEvent<Throwable>()

}