package com.crskdev.photosurfer.presentation

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.remote.auth.AuthTokenStorage

/**
 * Created by Cristian Pela on 14.08.2018.
 */
class AuthNavigatorMiddleware(private val authTokenStorage: AuthTokenStorage) {

    fun navigate(controller: NavController, navDirections: NavDirections) {
        if (authTokenStorage.hasToken()) {
            controller.navigate(navDirections)
        } else {
            controller.navigate(R.id.fragment_login)
        }
    }

    fun navigate(controller: NavController, @IdRes id: Int, args: Bundle? = null) {
        if (authTokenStorage.hasToken()) {
            controller.navigate(id)
        } else {
            controller.navigate(R.id.fragment_login, args)
        }
    }

    fun navigateToLogin(controller: NavController) =  controller.navigate(R.id.fragment_login)

}