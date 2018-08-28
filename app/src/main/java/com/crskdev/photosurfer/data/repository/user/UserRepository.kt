package com.crskdev.photosurfer.data.repository.user

import androidx.paging.DataSource
import com.crskdev.photosurfer.data.local.Contract
import com.crskdev.photosurfer.data.local.DaoManager
import com.crskdev.photosurfer.data.local.TransactionRunner
import com.crskdev.photosurfer.data.local.track.StaleDataTrackSupervisor
import com.crskdev.photosurfer.data.local.user.UserDAO
import com.crskdev.photosurfer.data.remote.APICallDispatcher
import com.crskdev.photosurfer.data.remote.PagingData
import com.crskdev.photosurfer.data.remote.auth.AuthAPI
import com.crskdev.photosurfer.data.remote.auth.AuthTokenStorage
import com.crskdev.photosurfer.data.remote.auth.toAuthToken
import com.crskdev.photosurfer.data.remote.user.UserAPI
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.entities.User
import com.crskdev.photosurfer.entities.toDbUserEntity
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

    fun getUsers(): DataSource.Factory<Int, User>

    fun searchUsers(query: String, page: Int, callback: Repository.Callback<Unit>? = null)

    fun follow(isFollowed: Boolean, callback: Repository.Callback<User>)

    fun clear()

}

class UserRepositoryImpl(private val daoManager: DaoManager,
                         private val staleDataTrackSupervisor: StaleDataTrackSupervisor,
                         private val apiCallDispatcher: APICallDispatcher,
                         private val userAPI: UserAPI,
                         private val authAPI: AuthAPI,
                         private val authTokenStorage: AuthTokenStorage) : UserRepository {

    private val userDAO: UserDAO = daoManager.getDao(Contract.TABLE_USERS)
    private val transactional: TransactionRunner = daoManager.transactionRunner()

    override fun clear() {
        userDAO.clear()
    }

    override fun getUsers(): DataSource.Factory<Int, User> =
            userDAO.getUsers().mapByPage { page ->
                staleDataTrackSupervisor.runStaleDataCheckForTable(Contract.TABLE_USERS)
                page.map { it.toUser() }
            }


    override fun searchUsers(query: String, page: Int, callback: Repository.Callback<Unit>?) {
        try {
            val response = apiCallDispatcher { userAPI.search(query, page) }
            with(response) {
                if (isSuccessful) {
                    val pagingData = PagingData.createFromHeaders(headers())
                    transactional {
                        val nextIndex = userDAO.getNextIndex()
                        val users = response.body()!!.results.map {
                            it.toDbUserEntity(pagingData, nextIndex)
                        }
                        userDAO.insertUsers(users)
                    }
                    callback?.onSuccess(Unit)
                } else {
                    callback?.onError(Error("${code()}:${errorBody()?.string()}"), false)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            callback?.onError(ex)
        }
    }

    override fun follow(isFollowed: Boolean, callback: Repository.Callback<User>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun login(email: String, password: String, callback: Repository.Callback<Unit>) {
        try {
            val authResponse = apiCallDispatcher { authAPI.authorize(email, password) }
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
        daoManager.clearAll()
        callback?.onSuccess(Unit)
    }

    override fun me(callback: Repository.Callback<User>) {
        try {
            val userResponse = apiCallDispatcher { userAPI.getMe() }
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
            val userResponse = apiCallDispatcher { userAPI.getUser(username) }
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
