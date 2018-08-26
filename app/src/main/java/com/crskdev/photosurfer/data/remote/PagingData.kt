package com.crskdev.photosurfer.data.remote

import okhttp3.Headers

data class PagingData(val total: Int, val curr: Int, val prev: Int?, val next: Int?) {
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

        fun createFromHeaders(headers: Headers): PagingData {
            val total = headers["x-total"]?.toInt()
                    ?: 0
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
            return PagingData(total, curr, prev, next)
        }
    }
}