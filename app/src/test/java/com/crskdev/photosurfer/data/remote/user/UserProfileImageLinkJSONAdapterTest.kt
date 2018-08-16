package com.crskdev.photosurfer.data.remote.user

import com.crskdev.photosurfer.entities.ImageType
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Test

import org.junit.Assert.*
import java.util.*

/**
 * Created by Cristian Pela on 16.08.2018.
 */
class UserProfileImageLinkJSONAdapterTest {

    val json = """
        {
            "downloads": 225974,
            "profile_image": {
                 "small": "small1",
                 "medium": "medium2",
                 "large": "large3"
            },
            "name":"Foo"
        }
    """.trimIndent()

    @Test
    fun fromJson() {
        val moshi = Moshi.Builder()
                .add(UserProfileImageLinkJsonAdapterFactory())
                .add(KotlinJsonAdapterFactory())
                .build()

        val profile = moshi.adapter(Profile::class.java).fromJson(json)

        assertEquals(225974L, profile?.downloads)
        assertEquals("small1", profile?.profileImages?.get(ImageType.SMALL))
        assertEquals("medium2", profile?.profileImages?.get(ImageType.MEDIUM))
        assertEquals("large3", profile?.profileImages?.get(ImageType.LARGE))

    }

    class Profile {
        var downloads: Long = 0L
        @Json(name = "profile_image")
        lateinit var profileImages: EnumMap<ImageType, String>
        lateinit var name: String
    }
}