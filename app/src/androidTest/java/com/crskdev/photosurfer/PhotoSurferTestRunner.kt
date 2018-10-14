package com.crskdev.photosurfer

import androidx.multidex.MultiDex
import android.os.Bundle
import android.support.test.runner.AndroidJUnitRunner


/**
 * Created by Cristian Pela on 14.10.2018.
 */
class PhotoSurferTestRunner: AndroidJUnitRunner(){

    override fun onCreate(arguments: Bundle) {
        MultiDex.install(targetContext)
        super.onCreate(arguments)
    }

}