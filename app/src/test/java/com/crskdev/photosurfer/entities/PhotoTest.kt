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
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").apply {
            timeZone = TimeZone.getTimeZone("Europe/Paris")
        }
        println(format.format(now))

    }
}