package com.crskdev.photosurfer

import android.app.Application
import com.crskdev.photosurfer.dependencies.DependencyGraph
import com.crskdev.photosurfer.dependencies.RealMockDependencyGraph

/**
 * Created by Cristian Pela on 19.10.2018.
 */
class MockApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DependencyGraph.install {
            RealMockDependencyGraph()
        }
    }
}