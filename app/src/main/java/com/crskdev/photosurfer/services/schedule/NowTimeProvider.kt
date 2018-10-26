package com.crskdev.photosurfer.services.schedule

interface NowTimeProvider {
    companion object {
        val DEFAULT = object : NowTimeProvider {}
    }

    fun now() = System.currentTimeMillis()
}