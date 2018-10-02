package com.crskdev.photosurfer.services.schedule

class Tag(val type: WorkType, private val uniqueId: String = "") {
    override fun toString(): String {
        return "$type#$uniqueId"
    }

    fun morph(type: WorkType) = Tag(type, uniqueId)

}