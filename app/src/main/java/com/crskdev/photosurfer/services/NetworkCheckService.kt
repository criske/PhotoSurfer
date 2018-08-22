package com.crskdev.photosurfer.services

import android.content.Context
import android.net.ConnectivityManager


/**
 * Created by Cristian Pela on 22.08.2018.
 */
interface NetworkCheckService {

    fun isNetworkAvailableAndOnline(): Boolean = true
}

class NetworkCheckServiceImpl(private val context: Context) : NetworkCheckService {

    override fun isNetworkAvailableAndOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
        //todo do a ping test
    }

}