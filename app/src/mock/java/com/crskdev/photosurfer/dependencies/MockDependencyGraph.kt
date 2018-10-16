package com.crskdev.photosurfer.dependencies

import android.content.Context
import com.crskdev.photosurfer.data.repository.playwave.MockPlaywaveRepository
import com.crskdev.photosurfer.data.repository.playwave.PlaywaveRepository

/**
 * Created by Cristian Pela on 16.10.2018.
 */
class MockDependencyGraph(context: Context): ProdDependencyGraph(context) {

    override val playwaveRepository: PlaywaveRepository = MockPlaywaveRepository()
}