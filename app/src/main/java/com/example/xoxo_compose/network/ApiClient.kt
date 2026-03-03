package com.example.xoxo_compose.network

import android.content.Context
import android.content.SharedPreferences
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.*
import kotlinx.serialization.json.Json

object ApiClient {

    private const val BASE_URL = "https://unfemale-scrawnily-elida.ngrok-free.dev"
    private const val PREFS_NAME = "xoxo_app_prefs"
    private const val PREF_ACCESS_TOKEN = "access_token"
    private const val PREF_REFRESH_TOKEN = "refresh_token"
    private const val PREF_USER_EMAIL = "user_email"
    private const val PREF_USER_FULLNAME = "user_fullname"
    private const val PREF_USER_IMAGE = "user_image"

    private var sharedPrefs: SharedPreferences? = null

    var accessToken: String? = null
    var refreshToken: String? = null

    fun initSharedPreferences(context: Context) {
        if (sharedPrefs == null) {
            sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            // Load tokens from SharedPreferences on init
            accessToken = sharedPrefs?.getString(PREF_ACCESS_TOKEN, null)
            refreshToken = sharedPrefs?.getString(PREF_REFRESH_TOKEN, null)
        }
    }

    fun saveLoginData(response: LoginResponse) {
        accessToken = response.accessToken
        refreshToken = response.refreshToken
        
        sharedPrefs?.edit()?.apply {
            putString(PREF_ACCESS_TOKEN, response.accessToken)
            putString(PREF_REFRESH_TOKEN, response.refreshToken)
            putString(PREF_USER_EMAIL, response.email)
            putString(PREF_USER_FULLNAME, response.fullname)
            putString(PREF_USER_IMAGE, response.image ?: "default.jpg")
            apply()
        }
    }

    fun clearLoginData() {
        accessToken = null
        refreshToken = null
        
        sharedPrefs?.edit()?.apply {
            remove(PREF_ACCESS_TOKEN)
            remove(PREF_REFRESH_TOKEN)
            remove(PREF_USER_EMAIL)
            remove(PREF_USER_FULLNAME)
            remove(PREF_USER_IMAGE)
            apply()
        }
    }

    fun getUserData(): UserData? {
        return sharedPrefs?.let {
            UserData(
                email = it.getString(PREF_USER_EMAIL, null) ?: return null,
                fullname = it.getString(PREF_USER_FULLNAME, null) ?: return null,
                image = it.getString(PREF_USER_IMAGE, "default.jpg") ?: "default.jpg"
            )
        }
    }

    val client = HttpClient(CIO) {

        defaultRequest {
            url(BASE_URL)
            contentType(ContentType.Application.Json)
        }

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                coerceInputValues = true
            })
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    android.util.Log.d("API", message)
                }
            }
            level = LogLevel.ALL
        }

        install(Auth) {
            bearer {

                // Load current tokens
                loadTokens {
                    if (accessToken != null && refreshToken != null) {
                        BearerTokens(accessToken!!, refreshToken!!)
                    } else {
                        null
                    }
                }

                // Called automatically on 401
                refreshTokens {

                    val response: RefreshTokenResponse = client.post("/auth/refresh") {
                        markAsRefreshTokenRequest()
                        setBody(
                            RefreshRequest(refreshToken!!)
                        )
                    }.body()

                    // Save new tokens
                    if (response.status) {
                        accessToken = response.accessToken
                        refreshToken = response.refreshToken
                        
                        // Update SharedPreferences with new tokens
                        sharedPrefs?.edit()?.apply {
                            putString(PREF_ACCESS_TOKEN, response.accessToken)
                            putString(PREF_REFRESH_TOKEN, response.refreshToken)
                            apply()
                        }
                    }

                    BearerTokens(
                        response.accessToken,
                        response.refreshToken
                    )
                }
            }
        }
    }

    suspend fun registerUser(
        fullname: String,
        email: String,
        password: String,
        day: String,
        month: String,
        year: String,
        country: String
    ): Result<RegisterResponse> = runCatching {
        val birthdate = "$year-${month.padStart(2, '0')}-${day.padStart(2, '0')}"
        
        val request = RegisterRequest(
            fullname = fullname,
            email = email,
            password = password,
            birthdate = birthdate,
            country = country
        )

        client.post("/auth/register") {
            setBody(request)
        }.body()
    }

    suspend fun loginUser(
        email: String,
        password: String
    ): Result<LoginResponse> = runCatching {
        val request = LoginRequest(
            email = email,
            password = password
        )

        val response = client.post("/auth/login") {
            setBody(request)
        }.body<LoginResponse>()

        // Save login data and tokens if login successful
        if (response.status) {
            saveLoginData(response)
        }

        response
    }
}


@Serializable
data class RefreshRequest(
    val refreshToken: String
)

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class RefreshTokenResponse(
    val status: Boolean,
    val msg: String,
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class RegisterRequest(
    val fullname: String,
    val email: String,
    val password: String,
    val birthdate: String,
    val country: String
)

@Serializable
data class RegisterResponse(
    val status: Boolean,
    val msg: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val status: Boolean,
    val msg: String,
    val refreshToken: String,
    val accessToken: String,
    val image: String? = "default.jpg",
    val fullname: String,
    val email: String
)

data class UserData(
    val email: String,
    val fullname: String,
    val image: String
)