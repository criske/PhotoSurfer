package com.crskdev.photosurfer.data.local.user

import androidx.paging.DataSource
import androidx.room.*
import com.crskdev.photosurfer.data.local.DataAccessor

/**
 * Created by Cristian Pela on 26.08.2018.
 */
@Dao
interface UserDAO: DataAccessor {

    @Query("SELECT * FROM users ORDER BY indexInResponse ASC")
    fun getUsers(): DataSource.Factory<Int, UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUsers(users: List<UserEntity>)

    @Query("DELETE FROM users")
    fun clear()

    @Query("SELECT MAX(indexInResponse) + 1 FROM users")
    fun getNextIndex(): Int

    @Query("SELECT count(*) == 0 FROM users")
    fun isEmpty(): Boolean

    @Query("SELECT * FROM users WHERE id=:id")
    fun getUser(id: String): UserEntity?

    @Update
    fun follow(user: UserEntity)

}