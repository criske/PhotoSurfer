package com.crskdev.photosurfer.entities

import com.crskdev.photosurfer.data.local.user.UserEntity
import com.crskdev.photosurfer.data.remote.PagingData
import com.crskdev.photosurfer.data.remote.user.UserJSON

/**
 * Created by Cristian Pela on 14.08.2018.
 */
fun UserJSON.toUser(): User = User(
        id,
        lastUpdated,
        userName, firstName ?: "", lastName ?: "",
        profileImageLinks,
        twitterUserName,
        instagramUsername,
        portfolioUrl,
        bio,
        location,
        totalLikes,
        totalPhotos,
        totalCollections,
        isFollowedByMe,
        followers,
        following,
        likes,
        downloads)

fun UserJSON.toDbUserEntity(pagingData: PagingData, nextIndex: Int): UserEntity = UserEntity().apply {
    this.id = this@toDbUserEntity.id
    this.lastUpdated = this@toDbUserEntity.lastUpdated
    this.firstName = this@toDbUserEntity.firstName
    this.lastName = this@toDbUserEntity.lastName
    this.userName = this@toDbUserEntity.userName
    this.profileImageLinks = transformMapUrls(this@toDbUserEntity.profileImageLinks)
    this.twitterUserName = this@toDbUserEntity.twitterUserName
    this.instagramUsername = this@toDbUserEntity.instagramUsername
    this.portfolioUrl = this@toDbUserEntity.portfolioUrl
    this.bio = this@toDbUserEntity.bio
    this.location = this@toDbUserEntity.location
    this.totalLikes = this@toDbUserEntity.totalLikes
    this.totalPhotos = this@toDbUserEntity.totalPhotos
    this.totalCollections = this@toDbUserEntity.totalCollections
    this.isFollowedByMe = this@toDbUserEntity.isFollowedByMe
    this.followers = this@toDbUserEntity.followers
    this.following = this@toDbUserEntity.following
    this.likes = this@toDbUserEntity.likes
    this.downloads = this@toDbUserEntity.downloads
    //paging
    this.indexInResponse = nextIndex
    this.curr = pagingData.curr
    this.next = pagingData.next
    this.prev = pagingData.prev
    this.total = pagingData.total
}

fun UserEntity.toUser(): User = User(
        id,
        lastUpdated,
        userName, firstName ?: "", lastName ?: "",
        transformStrMapToUrls(profileImageLinks),
        twitterUserName,
        instagramUsername,
        portfolioUrl,
        bio,
        location,
        totalLikes,
        totalPhotos,
        totalCollections,
        isFollowedByMe,
        followers,
        following,
        likes,
        downloads,
        PagingData(total ?: 0, curr ?: 1, prev, next))