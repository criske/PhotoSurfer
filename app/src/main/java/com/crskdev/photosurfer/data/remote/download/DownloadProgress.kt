package com.crskdev.photosurfer.data.remote.download

data class DownloadProgress(val percent: Int, val isStaringValue: Boolean, val doneOrCanceled: Boolean) {
    companion object {
        val NONE = DownloadProgress(Int.MIN_VALUE, false, false)
        val INDETERMINATED_START = DownloadProgress(-1, true, false)
        val INDETERMINATED_END = DownloadProgress(-1, true, false)
    }
}