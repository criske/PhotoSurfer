package com.crskdev.photosurfer.entities

/**
 * Created by Cristian Pela on 14.08.2018.
 */
data class User(
    val id: String,
    val lastUpdated: String,
    val userName: String,
    val firstName: String,
    val lastName: String,
    val profileImageLinks: Map<String, String>,
    val twitterUserName: String? = null,
    val portfolioUrl: String? = null,
    val bio: String? = null,
    val location: String? = null,
    val totalLikes: Int = 0,
    val totalPhotos: Int = 0,
    val totalCollections: Int = 0,
    val isFollowedByMe: Boolean = false,
    val downloads: Int = 0,
    val photos: List<Photo>
)