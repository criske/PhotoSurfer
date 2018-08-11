package com.crskdev.photosurfer.data.remote.photo

import okhttp3.Headers
import org.junit.Assert.*
import org.junit.Test

/**
 * Created by Cristian Pela on 04.08.2018.
 */
class PhotoPagingDataTest {

    @Test
    fun shouldExtractFromHeader() {

        val headers = Headers.Builder().add("Link",
                "<https://api.unsplash.com/photos?page=1>; rel=\"first\"," +
                        "<https://api.unsplash.com/photos?page=2>; rel=\"prev\"," +
                        "<https://api.unsplash.com/photos?page=346>; rel=\"last\"," +
                        "<https://api.unsplash.com/photos?page=4>; rel=\"next\"")
                .add("X-Total", "100")
                .build()
        assertEquals(PhotoPagingData(100, 3,2, 4), PhotoPagingData.createFromHeaders(headers))
    }

    @Test
    fun shouldExtractWhenMissingNextPage() {
        val headers = Headers.Builder().add("Link",
                "<https://api.unsplash.com/photos?page=1&foo=foo>; rel=\"first\"," +
                        "<https://api.unsplash.com/photos?page=2&foo=foo>; rel=\"prev\"," +
                        "<https://api.unsplash.com/photos?page=346&foo=foo>; rel=\"last\",")
                .add("X-Total", "100")
                .build()
        assertEquals(PhotoPagingData(100, 3, 2, null), PhotoPagingData.createFromHeaders(headers))
    }

    @Test
    fun shouldExtractWhenMissingPrevPage() {
        val headers = Headers.Builder().add("Link",
                "<https://api.unsplash.com/photos?page=1>; rel=\"first\"," +
                        "<https://api.unsplash.com/photos?page=346>; rel=\"last\"," +
                        "<https://api.unsplash.com/photos?page=4>; rel=\"next\"")
                .add("X-Total", "100")
                .build()
        assertEquals(PhotoPagingData(100, 3,null, 4), PhotoPagingData.createFromHeaders(headers))
    }

    @Test(expected = Error::class)
    fun shouldThrowWhenTotalNotFound() {
        val headers = Headers.Builder().add("Link",
                "<https://api.unsplash.com/photos?page=1>; rel=\"first\"," +
                        "<https://api.unsplash.com/photos?page=346>; rel=\"last\"," +
                        "<https://api.unsplash.com/photos?page=4>; rel=\"next\"")
                .build()
        PhotoPagingData.createFromHeaders(headers)
    }
}