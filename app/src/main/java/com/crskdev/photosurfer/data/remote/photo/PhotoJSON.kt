package com.crskdev.photosurfer.data.remote.photo

import com.crskdev.photosurfer.entities.ImageType
import com.squareup.moshi.Json
import okhttp3.Headers
import java.util.*

class PhotoJSON {
    lateinit var id: String
    @Json(name = "created_at")
    lateinit var createdAt: String
    @Json(name = "updated_at")
    lateinit var updatedAt: String
    var width: Int = 0
    var height: Int = 0
    @Json(name = "color")
    lateinit var colorString: String
    lateinit var urls: EnumMap<ImageType, String>
    var description: String? = null
    lateinit var categories: List<String>
    var likes: Int = 0
    @Json(name = "liked_by_user")
    var likedByMe: Boolean = false
    var views: Int = 0
    @Json(name = "user")
    lateinit var author: AuthorJSON
}

data class PhotoPagingData(val total: Int, val curr: Int, val prev: Int?, val next: Int?) {
    companion object {
        private fun extractQueryParams(link: String): Map<String, String> =
                link.split("?").takeIf { it.size == 2 }
                        ?.get(1)
                        ?.split("&")
                        ?.fold(mutableMapOf()) { acc, curr ->
                            val pair = curr.split("=")
                            acc[pair.first()] = pair.last()
                            acc
                        }
                        ?: emptyMap()

        fun createFromHeaders(headers: Headers): PhotoPagingData {
            val total = headers["x-total"]?.toInt()
                    ?: throw Error("Could not find x-total entry in header")
            val (curr: Int, prev: Int?, next: Int?) = headers["link"]?.let { hv ->
                val split = hv.split(",")
                val prev = split.firstOrNull { it.contains("prev") }
                        ?.split(";")
                        ?.first()
                        ?.let { extractQueryParams(it.substring(1, it.lastIndex)) }
                        ?.get("page")
                        ?.toInt()
                val next = split.firstOrNull { it.contains("next") }
                        ?.split(";")
                        ?.first()
                        ?.let { extractQueryParams(it.substring(1, it.lastIndex)) }
                        ?.get("page")
                        ?.toInt()
                val curr = next?.let { it - 1 } ?: prev?.let { it + 1 } ?: 1
                Triple(curr, prev, next)

            } ?: Triple(1, null, null)
            return PhotoPagingData(total, curr, prev, next)
        }
    }
}