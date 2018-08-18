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

    fun logout(callback: Repository.Callback<Unit>? = null)

    fun me(callback: Repository.Callback<User>)

    fun meUsername(callback: Repository.Callback<String>)

    fun meLoggedIn(callback: Repository.Callback<Boolean>)

    fun getUser(username: String, callback: Repository.Callback<User>)

}

class UserRepositoryImpl(private val userAPI: UserAPI,
                         private val authAPI: AuthAPI,
                         private val authTokenStorage: AuthTokenStorage) : UserRepository {

    override fun login(email: String, password: String, callback: Repository.Callback<Unit>) {
        try {
            val authResponse = authAPI.authorize(email, password).execute()
            with(authResponse) {
                if (isSuccessful) {
                    //TODO USE DIFFERENT FLOW - MAYBE CREATE A TABLE FOR LOGGED USER instead of saving user name in authtoken storage
                    val authTokenJSON = authResponse.body()!!
                    authTokenStorage.saveToken(authTokenJSON.toAuthToken(""))
                    val meResponse = userAPI.getMe().execute()
                    if (meResponse.isSuccessful) {
                        val me = meResponse.body()?.toUser()!!
                        authTokenStorage.token()?.copy(username = me.userName)?.let { authTokenStorage.saveToken(it) }
                        callback.onSuccess(Unit)
                    } else {
                        authTokenStorage.clearToken() // rollback
                        callback.onError(Error("${code()}:${errorBody()?.string()}"))
                    }
                } else {
                    val isAuthenticationError = code() == 401
                    callback.onError(Error("${code()}:${errorBody()?.string()}"), isAuthenticationError)
                }
            }
        } catch (ex: Exception) {
            authTokenStorage.clearToken()//rollback
            callback.onError(ex)
        }
    }

    override fun logout(callback: Repository.Callback<Unit>?) {
        authTokenStorage.clearToken()
        callback?.onSuccess(Unit)
    }

    override fun me(callback: Repository.Callback<User>) {
        try {
            val userResponse = userAPI.getMe().execute()
            with(userResponse) {
                if (isSuccessful) {
                    val user = userResponse.body()!!.toUser()
                    callback.onSuccess(user)
                } else {
                    val isAuthenticationError = code() == 401
                    callback.onError(Error("${code()}:${errorBody()?.string()}"), isAuthenticationError)
                }
            }
        } catch (ex: Exception) {
            callback.onError(ex)
        }
    }

    override fun meUsername(callback: Repository.Callback<String>) {
        val userName = authTokenStorage.token()?.username
        if (userName != null)
            callback.onSuccess(userName)
        else
            callback.onSuccess("")
    }

    override fun meLoggedIn(callback: Repository.Callback<Boolean>) {
        val userName = authTokenStorage.token()?.username
        callback.onSuccess(userName != null)
    }

    override fun getUser(username: String, callback: Repository.Callback<User>) {
        try {
            val userResponse = userAPI.getUser(username).execute()
            with(userResponse) {
                if (isSuccessful) {
                    val user = userResponse.body()!!.toUser()
                    callback.onSuccess(user)
                } else {
                    callback.onError(Error("${code()}:${errorBody()?.string()}"))
                }
            }
        } catch (ex: Exception) {
            callback.onError(ex)
        }
    }

}
