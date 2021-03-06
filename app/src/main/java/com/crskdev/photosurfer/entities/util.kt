package com.crskdev.photosurfer.entities

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Cristian Pela on 25.08.2018.
 */

fun transformMapUrls(map: Map<*, *>): String =
        map.map { "${it.key}$KV_DELIM${it.value}" }.joinToString(ENTRY_DELIM)

fun Map<*, *>.stringify(): String =
        transformMapUrls(this)

fun transformStrMapToUrls(strMap: String): EnumMap<ImageType, String> =
        strMap.split(ENTRY_DELIM)
                .fold(EnumMap<ImageType, String>(ImageType::class.java))
                { a, c -> c.split(KV_DELIM).let { a.apply { a[ImageType.valueOf(it[0].toUpperCase())] = it[1] } } }

fun transformStrMapToMap(strMap: String): Map<String, String> =
        strMap.trim().takeIf { it.isNotEmpty() }?.split(ENTRY_DELIM)
                ?.fold(mutableMapOf())
                { a, c -> c.split(KV_DELIM).let { a.apply { a[it[0]] = it[1] } } }
                ?: emptyMap()


val DISPLAY_DATE_FORMATTER: DateFormat  by lazy {
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }
}

val UNSPLASH_DATE_FORMATTER: DateFormat by lazy {
    SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssX", Locale.getDefault())
}

fun DateFormat.formatNow(): String = format(System.currentTimeMillis())