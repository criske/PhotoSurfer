package com.crskdev.photosurfer.data.repository.scheduled

class Tag(val type: WorkType, val uniqueId: String = "") {
    override fun toString(): String {
        return "$type#$uniqueId"
    }
}