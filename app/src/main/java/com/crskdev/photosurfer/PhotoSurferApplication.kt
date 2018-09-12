package com.crskdev.photosurfer

import android.app.Application
import com.crskdev.photosurfer.dependencies.injectDependencyGraph

/**
 * Created by Cristian Pela on 09.08.2018.
 */
class PhotoSurferApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        injectDependencyGraph()
    }
}