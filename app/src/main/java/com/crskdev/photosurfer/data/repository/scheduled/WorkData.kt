package com.crskdev.photosurfer.data.repository.scheduled

class WorkData(val tag: Tag,
               val isUniqueWork: Boolean = false,
               vararg val extras: Pair<String, Any>)