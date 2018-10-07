package com.crskdev.photosurfer.entities

import org.junit.Test

import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Cristian Pela on 07.10.2018.
 */
class PhotoTest {

    @Test
    fun getCreatedAt() {
        val now = Date(System.currentTimeMillis())
        val format = UNSPLASH_DATE_FORMATTER
        val otherFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }

        println(otherFormat.format(format.parse("2018-10-06T06:47:55-04:00")))



    }
}