package com.crskdev.photosurfer

import androidx.multidex.MultiDexApplication
import com.crskdev.photosurfer.dependencies.injectDependencyGraph

/**
 * Created by Cristian Pela on 09.08.2018.
 */
class PhotoSurferApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        injectDependencyGraph()
    }
}