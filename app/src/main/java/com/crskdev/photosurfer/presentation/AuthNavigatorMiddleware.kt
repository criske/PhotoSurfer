package com.crskdev.photosurfer.presentation

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.remote.auth.AuthTokenStorage
import com.crskdev.photosurfer.util.defaultTransitionNavOptions

/**
 * Created by Cristian Pela on 14.08.2018.
 */
class AuthNavigatorMiddleware(private val authTokenStorage: AuthTokenStorage) {

    fun navigate(controller: NavController, navDirections: NavDirections, options: NavOptions = defaultTransitionNavOptions()) {
        val hasToken = authTokenStorage.hasToken()
        if (hasToken) {
            controller.navigate(navDirections, options)
        } else {
            controller.navigate(R.id.action_global_login, null, options)
        }
    }

    fun navigate(controller: NavController, @IdRes id: Int, args: Bundle? = null, options: NavOptions = defaultTransitionNavOptions()) {
        val hasToken = authTokenStorage.hasToken()
        if (hasToken) {
            controller.navigate(id, args, options)
        } else {
            controller.navigate(R.id.action_global_login, args, options)
        }
    }

    fun navigateToLogin(controller: NavController, options: NavOptions = defaultTransitionNavOptions()) =
            controller.navigate(R.id.action_global_login, null, options)

}