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
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.serialization.JsonConvertException
import kotlinx.serialization.*
import kotlinx.serialization.json.Json

object ApiClient {

    private const val BASE_URL = "https://unfemale-scrawnily-elida.ngrok-free.dev"
    const val API_BASE_URL = BASE_URL  // Public access to base URL
    private const val PREFS_NAME = "xoxo_app_prefs"
    private const val PREF_ACCESS_TOKEN = "access_token"
    private const val PREF_REFRESH_TOKEN = "refresh_token"
    private const val PREF_USER_EMAIL = "user_email"
    private const val PREF_USER_FULLNAME = "user_fullname"
    private const val PREF_USER_IMAGE = "user_image"
    private const val PREF_LIFE_IMAGE_1 = "life_image_1"
    private const val PREF_LIFE_IMAGE_2 = "life_image_2"
    private const val PREF_LIFE_IMAGE_3 = "life_image_3"
    private const val PREF_LIFE_IMAGE_4 = "life_image_4"
    private const val PREF_LIFE_IMAGE_5 = "life_image_5"
    private const val PREF_LIFE_IMAGE_6 = "life_image_6"

    private var sharedPrefs: SharedPreferences? = null

    var accessToken: String? = null
    var refreshToken: String? = null

    fun initSharedPreferences(context: Context) {
        android.util.Log.d("ApiClient", "=== INIT SHARED PREFERENCES ===")
        
        if (sharedPrefs == null) {
            android.util.Log.d("ApiClient", "Creating new SharedPreferences instance")
            sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            // Load tokens from SharedPreferences on init
            accessToken = sharedPrefs?.getString(PREF_ACCESS_TOKEN, null)
            refreshToken = sharedPrefs?.getString(PREF_REFRESH_TOKEN, null)
            
            val hasAccessToken = accessToken != null
            val hasRefreshToken = refreshToken != null
            android.util.Log.d("ApiClient", "Tokens loaded - AccessToken exists: $hasAccessToken, RefreshToken exists: $hasRefreshToken")
        } else {
            android.util.Log.d("ApiClient", "SharedPreferences already initialized, skipping")
            
            // Verify what's actually in SharedPreferences
            val email = sharedPrefs?.getString(PREF_USER_EMAIL, null)
            val fullname = sharedPrefs?.getString(PREF_USER_FULLNAME, null)
            android.util.Log.d("ApiClient", "Current SharedPreferences user: Email=$email, Fullname=$fullname")
        }
        
        android.util.Log.d("ApiClient", "=== INIT SHARED PREFERENCES END ===")
    }

    fun saveLoginData(response: LoginResponse) {
        accessToken = response.accessToken
        refreshToken = response.refreshToken
        
        android.util.Log.d("ApiClient", "=== SAVE LOGIN DATA START ===")
        android.util.Log.d("ApiClient", "User: ${response.fullname} (${response.email})")
        android.util.Log.d("ApiClient", "SharedPrefs is null: ${sharedPrefs == null}")
        
        if (sharedPrefs == null) {
            android.util.Log.e("ApiClient", "ERROR: sharedPrefs is NULL! Cannot save login data!")
            return
        }
        
        try {
            sharedPrefs?.edit()?.apply {
                putString(PREF_ACCESS_TOKEN, response.accessToken)
                putString(PREF_REFRESH_TOKEN, response.refreshToken)
                putString(PREF_USER_EMAIL, response.email)
                putString(PREF_USER_FULLNAME, response.fullname)
                putString(PREF_USER_IMAGE, response.image ?: "default.jpg")
                apply()
            }
            
            // Verify data was actually saved
            val verifyEmail = sharedPrefs?.getString(PREF_USER_EMAIL, null)
            val verifyName = sharedPrefs?.getString(PREF_USER_FULLNAME, null)
            android.util.Log.d("ApiClient", "Saved to SharedPreferences - Email: $verifyEmail, Name: $verifyName")
            
            // CRITICAL: Recreate HTTP client so it picks up new tokens!
            android.util.Log.d("ApiClient", "Recreating HTTP client to reload tokens...")
            _client?.close()
            _client = null
            
            android.util.Log.d("ApiClient", "=== SAVE LOGIN DATA END ===")
        } catch (e: Exception) {
            android.util.Log.e("ApiClient", "ERROR saving login data: ${e.message}")
        }
    }

    fun clearLoginData() {
        accessToken = null
        refreshToken = null
        
        android.util.Log.d("ApiClient", "=== CLEAR LOGIN DATA START ===")
        
        val wasNull = sharedPrefs == null
        android.util.Log.d("ApiClient", "Before clear - sharedPrefs is null: $wasNull")
        
        if (sharedPrefs == null) {
            android.util.Log.e("ApiClient", "ERROR: Cannot clear - sharedPrefs is already null!")
            return
        }
        
        sharedPrefs?.edit()?.apply {
            remove(PREF_ACCESS_TOKEN)
            remove(PREF_REFRESH_TOKEN)
            remove(PREF_USER_EMAIL)
            remove(PREF_USER_FULLNAME)
            remove(PREF_USER_IMAGE)
            remove(PREF_LIFE_IMAGE_1)
            remove(PREF_LIFE_IMAGE_2)
            remove(PREF_LIFE_IMAGE_3)
            remove(PREF_LIFE_IMAGE_4)
            remove(PREF_LIFE_IMAGE_5)
            remove(PREF_LIFE_IMAGE_6)
            apply()
        }
        
        android.util.Log.d("ApiClient", "Cleared all 11 SharedPreferences entries")
        
        // Close and reset HTTP client
        android.util.Log.d("ApiClient", "Closing old HTTP client...")
        _client?.close()
        _client = null
        
        // Reset SharedPreferences singleton so next initSharedPreferences() will reload from disk
        sharedPrefs = null
        android.util.Log.d("ApiClient", "Reset sharedPrefs singleton to null")
        android.util.Log.d("ApiClient", "=== CLEAR LOGIN DATA END ===")
    }

    fun getUserData(): UserData? {
        return sharedPrefs?.let {
            val email = it.getString(PREF_USER_EMAIL, null)
            val fullname = it.getString(PREF_USER_FULLNAME, null)
            val image = it.getString(PREF_USER_IMAGE, "default.jpg") ?: "default.jpg"
            
            android.util.Log.d("ApiClient", "getUserData - Email: $email, Fullname: $fullname, Image: $image")
            
            if (email != null && fullname != null) {
                UserData(email = email, fullname = fullname, image = image)
            } else {
                android.util.Log.d("ApiClient", "getUserData - Email or Fullname is null, returning null")
                null
            }
        }
    }

    fun dumpAllPreferences(): String {
        return try {
            val dump = StringBuilder().apply {
                append("=== ALL SHARED PREFERENCES DATA ===\n")
                
                val email = sharedPrefs?.getString(PREF_USER_EMAIL, null) ?: "[NULL]"
                val fullname = sharedPrefs?.getString(PREF_USER_FULLNAME, null) ?: "[NULL]"
                val image = sharedPrefs?.getString(PREF_USER_IMAGE, null) ?: "[NULL]"
                val accessToken = sharedPrefs?.getString(PREF_ACCESS_TOKEN, null)?.let { it.take(20) + "..." } ?: "[NULL]"
                val refreshToken = sharedPrefs?.getString(PREF_REFRESH_TOKEN, null)?.let { it.take(20) + "..." } ?: "[NULL]"
                
                val image1 = sharedPrefs?.getString(PREF_LIFE_IMAGE_1, null)?.take(30) ?: "[EMPTY]"
                val image2 = sharedPrefs?.getString(PREF_LIFE_IMAGE_2, null)?.take(30) ?: "[EMPTY]"
                val image3 = sharedPrefs?.getString(PREF_LIFE_IMAGE_3, null)?.take(30) ?: "[EMPTY]"
                val image4 = sharedPrefs?.getString(PREF_LIFE_IMAGE_4, null)?.take(30) ?: "[EMPTY]"
                val image5 = sharedPrefs?.getString(PREF_LIFE_IMAGE_5, null)?.take(30) ?: "[EMPTY]"
                val image6 = sharedPrefs?.getString(PREF_LIFE_IMAGE_6, null)?.take(30) ?: "[EMPTY]"
                
                append("User Email: $email\n")
                append("User Fullname: $fullname\n")
                append("User Image: $image\n")
                append("AccessToken: $accessToken\n")
                append("RefreshToken: $refreshToken\n")
                append("LifeImage1: $image1\n")
                append("LifeImage2: $image2\n")
                append("LifeImage3: $image3\n")
                append("LifeImage4: $image4\n")
                append("LifeImage5: $image5\n")
                append("LifeImage6: $image6\n")
                append("========================\n")
            }
            dump.toString()
        } catch (e: Exception) {
            "ERROR reading SharedPreferences: ${e.message}"
        }
    }

    fun saveProfileUpdate(response: UpdateProfileResponse) {
        sharedPrefs?.edit()?.apply {
            putString(PREF_USER_FULLNAME, response.fullname)
            putString(PREF_USER_IMAGE, response.image)
            apply()
        }
        android.util.Log.d("ApiClient", "Profile updated in SharedPreferences: ${response.fullname}, ${response.image}")
    }

    fun saveLifestyleImages(response: UploadLifestyleImagesResponse) {
        sharedPrefs?.edit()?.apply {
            putString(PREF_LIFE_IMAGE_1, response.image1)
            putString(PREF_LIFE_IMAGE_2, response.image2)
            putString(PREF_LIFE_IMAGE_3, response.image3)
            putString(PREF_LIFE_IMAGE_4, response.image4)
            putString(PREF_LIFE_IMAGE_5, response.image5)
            putString(PREF_LIFE_IMAGE_6, response.image6)
            apply()
        }
        android.util.Log.d("ApiClient", "Lifestyle images saved to SharedPreferences")
    }

    fun getLifestyleImages(): LifestyleImagesData? {
        return sharedPrefs?.let {
            LifestyleImagesData(
                image1 = it.getString(PREF_LIFE_IMAGE_1, "") ?: "",
                image2 = it.getString(PREF_LIFE_IMAGE_2, "") ?: "",
                image3 = it.getString(PREF_LIFE_IMAGE_3, "") ?: "",
                image4 = it.getString(PREF_LIFE_IMAGE_4, "") ?: "",
                image5 = it.getString(PREF_LIFE_IMAGE_5, "") ?: "",
                image6 = it.getString(PREF_LIFE_IMAGE_6, "") ?: ""
            )
        }
    }

    private var _client: HttpClient? = null

    private fun createHttpClient(): HttpClient {
        return HttpClient(CIO) {

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

            HttpResponseValidator {
                handleResponseException { exception ->
                    if (exception is ResponseException && exception.response.status.value == 401) {
                        android.util.Log.e("API", "401 Unauthorized - Token invalid or expired")
                        // Clear tokens on 401
                        clearLoginData()
                    }
                    throw exception
                }
            }

            install(Auth) {
                bearer {

                    // Load current tokens - always check in-memory variables
                    loadTokens {
                        val hasAccessToken = accessToken != null
                        val hasRefreshToken = refreshToken != null
                        
                        android.util.Log.d("API", "Auth loadTokens - AccessToken exists: $hasAccessToken, RefreshToken exists: $hasRefreshToken")
                        
                        if (hasAccessToken && hasRefreshToken) {
                            BearerTokens(accessToken!!, refreshToken!!)
                        } else {
                            android.util.Log.d("API", "No valid tokens - returning null")
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
    }

    val client: HttpClient
        get() {
            if (_client == null) {
                _client = createHttpClient()
            }
            return _client!!
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

    suspend fun getProfile(): Result<ProfileResponse> = runCatching {
        val response = client.get("/auth/profile").body<ProfileResponse>()
        
        android.util.Log.d("API", "getProfile - Response status: ${response.status}, User: ${response.user?.fullname ?: "NULL"}")
        
        // If token is invalid/expired, try to refresh and retry
        if (!response.status && response.msg.contains("token", ignoreCase = true)) {
            android.util.Log.d("API", "Token expired, attempting refresh...")
            
            if (refreshToken != null) {
                val refreshResult = refreshAccessToken()
                if (refreshResult) {
                    // Retry the request with new token
                    val retryResponse = client.get("/auth/profile").body<ProfileResponse>()
                    android.util.Log.d("API", "getProfile after refresh - User: ${retryResponse.user?.fullname ?: "NULL"}")
                    return@runCatching retryResponse
                }
            }
        }
        
        response
    }

    suspend fun updateProfile(
        fullname: String,
        country: String,
        gender: String,
        bio: String,
        image: String? = null
    ): Result<UpdateProfileResponse> = runCatching {
        val request = UpdateProfileRequest(
            fullname = fullname,
            country = country,
            gender = gender,
            bio = bio,
            image = image
        )

        val response = client.post("/auth/profile") {
            setBody(request)
        }.body<UpdateProfileResponse>()

        // If token is invalid/expired, try to refresh and retry
        if (!response.status && response.msg.contains("token", ignoreCase = true)) {
            android.util.Log.d("API", "Token expired, attempting refresh...")
            
            if (refreshToken != null) {
                val refreshResult = refreshAccessToken()
                if (refreshResult) {
                    // Retry the request with new token
                    return@runCatching client.post("/auth/profile") {
                        setBody(request)
                    }.body<UpdateProfileResponse>()
                }
            }
        }

        response
    }

    suspend fun uploadLifestyleImages(
        image1: String?,
        image2: String?,
        image3: String?,
        image4: String?,
        image5: String?,
        image6: String?
    ): Result<UploadLifestyleImagesResponse> = runCatching {
        val request = UploadLifestyleImagesRequest(
            image1 = image1,
            image2 = image2,
            image3 = image3,
            image4 = image4,
            image5 = image5,
            image6 = image6
        )

        val response = client.post("/auth/profile/life_images") {
            setBody(request)
        }.body<UploadLifestyleImagesResponse>()

        // If token is invalid/expired, try to refresh and retry
        if (!response.status && response.msg.contains("token", ignoreCase = true)) {
            android.util.Log.d("API", "Token expired, attempting refresh...")
            
            if (refreshToken != null) {
                val refreshResult = refreshAccessToken()
                if (refreshResult) {
                    // Retry the request with new token
                    return@runCatching client.post("/auth/profile/life_images") {
                        setBody(request)
                    }.body<UploadLifestyleImagesResponse>()
                }
            }
        }

        response
    }

    suspend fun getDiscoverProfiles(limit: Int = 10): Result<DiscoverResponse> = runCatching {
        val response = client.get("/auth/discover?limit=$limit").body<DiscoverResponse>()
        
        // If token is invalid/expired, try to refresh and retry
        if (!response.status && response.msg.contains("token", ignoreCase = true)) {
            android.util.Log.d("API", "Token expired, attempting refresh...")
            
            if (refreshToken != null) {
                val refreshResult = refreshAccessToken()
                if (refreshResult) {
                    // Retry the request with new token
                    return@runCatching client.get("/auth/discover?limit=$limit").body<DiscoverResponse>()
                }
            }
        }
        
        response
    }

    suspend fun recordSwipe(targetUserId: Int, swipeType: String): Result<SwipeResponse> = runCatching {
        val request = SwipeRequest(
            targetUserId = targetUserId,
            swipeType = swipeType
        )

        val response = client.post("/auth/swipe") {
            setBody(request)
        }.body<SwipeResponse>()

        // If token is invalid/expired, try to refresh and retry
        if (!response.status && response.msg.contains("token", ignoreCase = true)) {
            android.util.Log.d("API", "Token expired, attempting refresh...")
            
            if (refreshToken != null) {
                val refreshResult = refreshAccessToken()
                if (refreshResult) {
                    // Retry the request with new token
                    return@runCatching client.post("/auth/swipe") {
                        setBody(request)
                    }.body<SwipeResponse>()
                }
            }
        }

        response
    }

    private suspend fun refreshAccessToken(): Boolean = runCatching {
        if (refreshToken == null) return@runCatching false
        
        val response = client.post("/auth/refresh") {
            setBody(RefreshRequest(refreshToken!!))
        }.body<RefreshTokenResponse>()

        if (response.status) {
            accessToken = response.accessToken
            refreshToken = response.refreshToken
            
            // Update SharedPreferences
            sharedPrefs?.edit()?.apply {
                putString(PREF_ACCESS_TOKEN, response.accessToken)
                putString(PREF_REFRESH_TOKEN, response.refreshToken)
                apply()
            }
            
            android.util.Log.d("API", "Token refreshed successfully")
            true
        } else {
            android.util.Log.e("API", "Token refresh failed: ${response.msg}")
            clearLoginData()
            false
        }
    }.getOrElse { exception ->
        android.util.Log.e("API", "Token refresh error: ${exception.message}")
        clearLoginData()
        false
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

@Serializable
data class ProfileResponse(
    val status: Boolean,
    val msg: String,
    val user: User? = null,
    val image1: String = "",
    val image2: String = "",
    val image3: String = "",
    val image4: String = "",
    val image5: String = "",
    val image6: String = ""
)

@Serializable
data class User(
    val id: Int? = null,
    val fullname: String = "",
    val email: String = "",
    val birthdate: String = "",
    val country: String = "",
    val image: String = "",
    val bio: String = "",
    val sex: String = "",
    val image1: String = "",
    val image2: String = "",
    val image3: String = "",
    val image4: String = "",
    val image5: String = "",
    val image6: String = ""
)

@Serializable
data class UpdateProfileRequest(
    val fullname: String,
    val country: String,
    val gender: String,
    val bio: String,
    val image: String? = null
)

@Serializable
data class UpdateProfileResponse(
    val status: Boolean,
    val msg: String,
    val fullname: String = "",
    val country: String = "",
    val gender: String = "",
    val bio: String = "",
    val image: String = ""
)

@Serializable
data class UploadLifestyleImagesRequest(
    val image1: String? = null,
    val image2: String? = null,
    val image3: String? = null,
    val image4: String? = null,
    val image5: String? = null,
    val image6: String? = null
)

@Serializable
data class UploadLifestyleImagesResponse(
    val status: Boolean,
    val msg: String,
    val image1: String = "",
    val image2: String = "",
    val image3: String = "",
    val image4: String = "",
    val image5: String = "",
    val image6: String = ""
)

data class LifestyleImagesData(
    val image1: String,
    val image2: String,
    val image3: String,
    val image4: String,
    val image5: String,
    val image6: String
)

@Serializable
data class DiscoverResponse(
    val status: Boolean,
    val msg: String,
    val count: Int = 0,
    val users: List<DiscoverUser> = emptyList()
)

@Serializable
data class DiscoverUser(
    val id: Int,
    val fullname: String,
    val email: String,
    val bio: String = "",
    val sex: String = "",
    val country: String = "",
    val profileImage: String? = null,
    val lifeImages: List<String> = emptyList()
)

@Serializable
data class SwipeRequest(
    val targetUserId: Int,
    val swipeType: String  // "like" or "dislike"
)

@Serializable
data class SwipeResponse(
    val status: Boolean,
    val msg: String,
    val matched: Boolean = false
)