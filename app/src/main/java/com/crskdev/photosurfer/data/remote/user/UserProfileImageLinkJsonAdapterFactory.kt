package com.crskdev.photosurfer.data.remote.user

import com.crskdev.photosurfer.entities.ImageType
import com.squareup.moshi.*
import java.lang.reflect.Type
import java.util.*


/**
 * Created by Cristian Pela on 16.08.2018.
 */
private class UserProfileImageLinkJSONAdapter : JsonAdapter<EnumMap<ImageType, String>>() {

    @FromJson
    override fun fromJson(reader: JsonReader): EnumMap<ImageType, String> {
        val map = EnumMap<ImageType, String>(ImageType::class.java)
        reader.beginObject()
        while (reader.hasNext()) {
            val type = reader.nextName()
            val value = reader.nextString()
            when (type) {
                "small" -> map[ImageType.SMALL] = value
                "medium" -> map[ImageType.MEDIUM] = value
                "large" -> map[ImageType.LARGE] = value
                "custom" -> map[ImageType.CUSTOM] = value
                "raw" -> map[ImageType.RAW] = value
                "full" -> map[ImageType.FULL] = value
                "regular" -> map[ImageType.REGULAR] = value
                "thumb" -> map[ImageType.THUMB] = value
            }
        }
        reader.endObject()
        return map
    }

    @ToJson
    override fun toJson(writer: JsonWriter?, value: EnumMap<ImageType, String>?) = Unit
}

class UserProfileImageLinkJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*> {
        val clazz = Types.getRawType(type)
        return if (clazz == EnumMap::class.java) {
            UserProfileImageLinkJSONAdapter()
        } else {
            moshi.nextAdapter<JsonAdapter.Factory>(this, type, annotations)
        }
    }
}
