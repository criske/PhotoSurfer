package com.crskdev.photosurfer.data.repository.user

import com.crskdev.photosurfer.data.remote.auth.AuthAPI
import com.crskdev.photosurfer.data.remote.auth.AuthTokenStorage
import com.crskdev.photosurfer.data.remote.auth.toAuthToken
import com.crskdev.photosurfer.data.remote.user.UserAPI
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.entities.User
import com.crskdev.photosurfer.entities.toUser

/**
 * Created by Cristian Pela on 14.08.2018.
 */
interface UserRepository : Repository {

    fun login(email: String, password: String, callback: Repository.Callback<Unit>)

    fun me(callback: Repository.Callback<User>)

    fun getUser(id: String, callback: Repository.Callback<User>)

}


class UserRepositoryImpl(private val userAPI: UserAPI,
                         private val authAPI: AuthAPI,
                         private val authTokenStorage: AuthTokenStorage) : UserRepository {
    override fun login(email: String, password: String, callback: Repository.Callback<Unit>) {
        val authResponse = authAPI.authorize(email, password).execute()
        with(authResponse) {
            if (isSuccessful) {
                val authTokenJSON = authResponse.body()!!
                authTokenStorage.saveToken(authTokenJSON.toAuthToken())
                callback.onSuccess(Unit)
            } else {
                callback.onError(Error("${code()}:${errorBody()?.string()}"))
            }
        }

    }

    override fun me(callback: Repository.Callback<User>) {
        val userResponse = userAPI.getMe().execute()
        with(userResponse) {
            if (isSuccessful) {
                val user = userResponse.body()!!.toUser()
                callback.onSuccess(user)
            } else {
                callback.onError(Error("${code()}:${errorBody()?.string()}"))
            }
        }
    }

    override fun getUser(id: String, callback: Repository.Callback<User>) {
        val userResponse = userAPI.getUser(id).execute()
        with(userResponse) {
            if (isSuccessful) {
                val user = userResponse.body()!!.toUser()
                callback.onSuccess(user)
            } else {
                callback.onError(Error("${code()}:${errorBody()?.string()}"))
            }
        }
    }

}
