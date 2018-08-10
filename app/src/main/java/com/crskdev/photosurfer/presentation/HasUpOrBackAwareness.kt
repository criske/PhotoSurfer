package com.crskdev.photosurfer.presentation

/**
 * Created by Cristian Pela on 10.08.2018.
 */
interface HasUpOrBackAwareness {

    fun onBackOrUpPressed()

    fun handleBack(): Boolean = false

}