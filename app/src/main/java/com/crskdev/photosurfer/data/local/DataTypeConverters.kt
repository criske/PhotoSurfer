package com.crskdev.photosurfer.data.local

import androidx.room.TypeConverter
import com.crskdev.photosurfer.entities.CollectionLite


/**
 * Created by Cristian Pela on 15.09.2018.
 */
class DataTypeConverters {

    @TypeConverter
    fun fromCollectionLiteListToString(collections: List<CollectionLite>): String =
            collections.asSequence().map { "${it.id}#${it.title}" }.joinToString("@")

    @TypeConverter
    fun fromStringToCollectionListList(liteStrList: String): List<CollectionLite> {
        if (liteStrList.isEmpty()) {
            return emptyList()
        }
        return liteStrList.split("@").asSequence().filter { it.isNotEmpty() }.map {
            val split = it.split("#")
            if (split.size != 2) {
                throw IllegalAccessException("Invalid parse from string to collection list")
            } else {
                CollectionLite(split[0].toInt(), split[1])
            }
        }.toList()
    }

//    fun fromCategoryListToString(categories: List<String>): String =
//            categories.

}