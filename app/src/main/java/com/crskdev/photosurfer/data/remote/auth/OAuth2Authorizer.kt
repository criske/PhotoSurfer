package com.crskdev.photosurfer.data.remote.auth

import com.crskdev.photosurfer.data.remote.BASE_HOST_AUTHORIZING
import com.crskdev.photosurfer.data.remote.LOGIN_FORM_EMAIL
import com.crskdev.photosurfer.data.remote.LOGIN_FORM_PASSWORD
import okhttp3.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Created by Cristian Pela on 30.07.2018.
 */
class OAuth2Authorizer {

    private val scopes = listOf(
            "public",
            "read_user",
            "write_user",
            "read_photos",
            "write_photos",
            "write_likes",
            "write_followers",
            "read_collections",
            "write_collections"
    )


    private val baseAuthHttpUrl = HttpUrl.Builder()
            .host(BASE_HOST_AUTHORIZING)
            .scheme("https")
            .addPathSegment("oauth")
            .build()

    fun authorize(chain: Interceptor.Chain, apiKeys: APIKeys): Response {

        //setup server
        val (hostName, port) = HttpUrl.parse(apiKeys.redirectURI)!!.let {
            it.host() to it.port()
        }
        val redirectURIServer = OAuth2RedirectURIServer(hostName, port)
                .apply { start() }

        val loginRequest = chain.request()
        val internalLoginUrl = loginRequest.url()
        val email = internalLoginUrl.queryParameter(LOGIN_FORM_EMAIL) ?: ""
        val password = internalLoginUrl.queryParameter(LOGIN_FORM_PASSWORD) ?: ""

        var authorizationCode: String?

        val responseAuthenticityToken = requestAuthenticityToken(chain, apiKeys)
        return if (responseAuthenticityToken.isSuccessful) {
            var doc = Jsoup.parse(responseAuthenticityToken.body()?.string())
            authorizationCode = tryExtractAuthorizationCode(doc)
            if (authorizationCode == null) {
                val loginResponse = requestLogin(chain, email, password, doc)
                if (loginResponse.isSuccessful) {
                    doc = Jsoup.parse(loginResponse.body()?.string())
                    val invalidCredentials = tryGetInvalidCredentials(doc)
                    if(invalidCredentials!= null){
                        redirectURIServer.stop()
                        return unauthorizedResponse(loginRequest, invalidCredentials)
                    }
                    authorizationCode = tryExtractAuthorizationCode(doc)
                    if (authorizationCode == null) {
                        val grantResponse = requestGrant(chain, apiKeys, doc)
                        if (grantResponse.isSuccessful) {
                            doc = Jsoup.parse(grantResponse.body()?.string())
                            authorizationCode = tryExtractAuthorizationCode(doc)
                        } else {
                            redirectURIServer.stop()
                            return grantResponse
                        }
                    }
                } else {
                    redirectURIServer.stop()
                    return loginResponse
                }
            }
            return if (authorizationCode != null) {
                redirectURIServer.stop()
                requestToken(chain, apiKeys, authorizationCode)
            } else {
                redirectURIServer.stop()
                unauthorizedResponse(loginRequest)
            }
        } else {
            redirectURIServer.stop()
            responseAuthenticityToken
        }

    }

    private fun tryGetInvalidCredentials(doc: Document): String? {
        val selector = "body > div.flash.flash--alert.animated.js-flash.js-flash-alert > div > div >" +
                " div.col-xs-10.col-sm-6.center-block.flash__message"
        return doc.selectFirst(selector)?.text()
    }


    private fun requestAuthenticityToken(chain: Interceptor.Chain, apiKeys: APIKeys): Response {
        //authorize key request
        val authenticityTokenUrl = baseAuthHttpUrl
                .newBuilder()
                .addPathSegment("authorize")
                .addQueryParameter("client_id", apiKeys.accessKey)
                .addQueryParameter("redirect_uri", apiKeys.redirectURI)
                .addQueryParameter("response_type", "code")
                .addEncodedQueryParameter("scope", scopes.joinToString("+"))
                .build()
        return chain.proceed(Request.Builder().url(authenticityTokenUrl).build())
    }

    private fun requestLogin(chain: Interceptor.Chain, email: String, password: String, doc: Document): Response {
        val inputUTF8 = doc.selectFirst("input[name=utf8]").attr("value")
        val inputAuthenticityToken = doc.selectFirst("input[name=authenticity_token]").attr("value")
        val form = FormBody.Builder()
                .add("utf8", inputUTF8)
                .add("authenticity_token", inputAuthenticityToken)
                .add(LOGIN_FORM_EMAIL, email)
                .add(LOGIN_FORM_PASSWORD, password)
                .build()
        return chain.proceed(Request.Builder()
                .url(baseAuthHttpUrl
                        .newBuilder()
                        .addPathSegment("login").build())
                .post(form)
                .build())
    }


    private fun requestGrant(chain: Interceptor.Chain, apiKeys: APIKeys, doc: Document): Response {
        val inputUTF8 = doc.selectFirst("input[name=utf8]").attr("value")
        val inputAuthenticityToken = doc.selectFirst("input[name=authenticity_token]").attr("value")
        val form = FormBody.Builder()
                .add("utf8", inputUTF8)
                .add("authenticity_token", inputAuthenticityToken)
                .add("client_id", apiKeys.accessKey)
                .add("redirect_uri", apiKeys.redirectURI)
                .add("state", "state")
                .add("response_type", "code")
                .add("scope", scopes.joinToString(" "))
                .build()
        return chain.proceed(Request.Builder()
                .url(baseAuthHttpUrl
                        .newBuilder()
                        .addPathSegment("authorize").build())
                .post(form)
                .build())
    }


    private fun requestToken(chain: Interceptor.Chain, apiKeys: APIKeys, authorizationCode: String): Response {
        val authTokenForm = FormBody.Builder()
                .add("client_id", apiKeys.accessKey)
                .add("client_secret", apiKeys.secretKey)
                .add("redirect_uri", apiKeys.redirectURI)
                .add("code", authorizationCode)
                .add("grant_type", "authorization_code")
                .build()
        return chain.proceed(Request.Builder()
                .url(baseAuthHttpUrl
                        .newBuilder()
                        .addPathSegment("token").build())
                .post(authTokenForm)
                .build())
    }


    private fun unauthorizedResponse(request: Request, message: String = "") =
            Response.Builder().code(401)
                    .body(ResponseBody.create(MediaType.get("text/plain"), message))
                    .request(request)
                    .message(message)
                    .protocol(Protocol.HTTP_1_1)
                    .build()

    private fun tryExtractAuthorizationCode(doc: Document): String? {
        return doc.selectFirst("code")?.text()
    }

}