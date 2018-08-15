package com.crskdev.photosurfer.entities

import com.crskdev.photosurfer.data.remote.user.UserJSON

/**
 * Created by Cristian Pela on 14.08.2018.
 */
fun UserJSON.toUser() = User(
        id, lastUpdated, userName, firstName, lastName, profileImageLinks, twitterUserName, portfolioUrl, bio, location,
        totalLikes, totalPhotos, totalCollections, isFollowedByMe, downloads)