package com.crskdev.photosurfer

import android.app.Application
import android.content.Context
import com.crskdev.photosurfer.dependencies.DependencyGraph
import com.crskdev.photosurfer.dependencies.ProdDependencyGraph
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.services.schedule.WorkData
import com.crskdev.photosurfer.services.schedule.WorkType

/**
 * Created by Cristian Pela on 09.08.2018.
 */
class PhotoSurferApplication : Application() {

    override fun onCreate() {
        DependencyGraph.install { ProdDependencyGraph(this) }
        super.onCreate()
        //bootstrap, if needed, the periodic stale data track check
        dependencyGraph().scheduledWorkManager.schedule(WorkData.just(WorkType.STALE_DATA_TRACK))
    }

}