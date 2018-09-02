package com.crskdev.photosurfer.data.local.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.crskdev.photosurfer.data.local.BaseDBEntity
import com.crskdev.photosurfer.data.local.Contract

/**
 * Created by Cristian Pela on 25.08.2018.
 */
@Entity(tableName = Contract.TABLE_USERS)
class UserEntity : BaseDBEntity() {
    @PrimaryKey
    lateinit var id: String
    lateinit var lastUpdated: String
    lateinit var userName: String
    var firstName: String? = null
    var lastName: String? = null
    lateinit var profileImageLinks: String
    var twitterUserName: String? = null
    var instagramUsername: String? = null
    var portfolioUrl: String? = null
    var bio: String? = null
    var location: String? = null
    var totalLikes: Int = 0
    var totalPhotos: Int = 0
    var totalCollections: Int = 0
    var isFollowedByMe: Boolean = false
    var followers: Int = 0
    var following: Int = 0
    var likes: Int = 0
    var downloads: Int = 0
}