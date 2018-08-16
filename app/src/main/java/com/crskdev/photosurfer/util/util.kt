package com.crskdev.photosurfer.util

import androidx.navigation.NavOptions
import com.crskdev.photosurfer.R

/**
 * Created by Cristian Pela on 15.08.2018.
 */
fun defaultTransitionNavOptionsBuilder(): NavOptions.Builder = NavOptions.Builder()
        .setEnterAnim(R.anim.in_from_right)
        .setExitAnim(R.anim.out_to_left)
        .setPopEnterAnim(R.anim.in_from_left)
        .setPopExitAnim(R.anim.out_to_right)

fun defaultTransitionNavOptions() = defaultTransitionNavOptionsBuilder().build()
