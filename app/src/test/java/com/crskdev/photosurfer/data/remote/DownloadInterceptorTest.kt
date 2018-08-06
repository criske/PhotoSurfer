package com.crskdev.photosurfer.data.remote

import com.crskdev.photosurfer.data.remote.photo.PhotoAPI
import okio.Okio
import org.junit.Test
import java.io.File


/**
 * Created by Cristian Pela on 06.08.2018.
 */
class DownloadInterceptorTest {

    @Test
    fun intercept() {

        RetrofitClient.DEFAULT.networkClient.addDownloadProgressListener{ curr, total, done ->
            val percent = "%.2f".format(curr.toFloat()/total * 100)
            println("Current: $percent")
        }

        val photoAPI = RetrofitClient.DEFAULT.retrofit.create(PhotoAPI::class.java)
        val response = photoAPI.download("zpK28VGxkJw").execute()
        val data = response.body()?.source()
        data?.use {
            val file = File("C:\\Users\\user\\Desktop\\photo.jpg")
            val sink = Okio.buffer(Okio.sink(file))
            sink.writeAll(it)
        }
    }
}