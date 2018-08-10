package com.crskdev.photosurfer.presentation

/**
 * Created by Cristian Pela on 10.08.2018.
 */
interface HasUpOrBackPressedAwareness {

    fun onBackOrUpPressed()

    fun handleBack(): Boolean = false

}