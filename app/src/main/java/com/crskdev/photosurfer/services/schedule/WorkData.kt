package com.crskdev.photosurfer.services.schedule

class WorkData(val tag: Tag,
               vararg val extras: Pair<String, Any>){
    fun morph(type: WorkType): WorkData = WorkData(tag.morph(type), *extras)
}