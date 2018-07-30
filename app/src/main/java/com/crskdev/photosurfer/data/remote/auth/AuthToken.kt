package com.crskdev.photosurfer.data.remote.auth

data class AuthToken(val access: String, val type: String, val scope: String, val createdAt: Long)