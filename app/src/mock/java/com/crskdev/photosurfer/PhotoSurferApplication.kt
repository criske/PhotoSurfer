package com.crskdev.photosurfer

import android.app.Application
import com.crskdev.photosurfer.dependencies.DependencyGraph
import com.crskdev.photosurfer.dependencies.MockDependencyGraph

/**
 * Created by Cristian Pela on 09.08.2018.
 */
class PhotoSurferApplication : Application() {

    override fun onCreate() {
        DependencyGraph.install { MockDependencyGraph(this) }
        super.onCreate()
    }

}