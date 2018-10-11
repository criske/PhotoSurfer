package com.crskdev.photosurfer.data.local

/**
 * Created by Cristian Pela on 30.07.2018.
 */
fun Any.asSearchTermInRecord(hasPrefix: Boolean = true, hasSuffix: Boolean = true, delim: String = "#"): String {
    val prefix = if (hasPrefix) "%" else ""
    val suffix = if (hasSuffix) "%" else ""
    return "$prefix$this$delim$suffix"
}