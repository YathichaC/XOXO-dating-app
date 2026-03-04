package com.example.xoxo_compose

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.xoxo_compose.network.ApiClient
import com.example.xoxo_compose.ui.theme.*
import kotlinx.coroutines.launch

class EditProfile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Initialize SharedPreferences
        ApiClient.initSharedPreferences(this)
        setContent {
            XOXO_composeTheme {
                EditProfileScreen()
            }
        }
    }
}

@Composable
fun EditProfileScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val countries = stringArrayResource(R.array.country_list).toList()
    val genderOptions = listOf("Male", "Female", "Other", "Not prefer to say")

    // Form state
    var username by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var image by remember { mutableStateOf("") }
    
    // Loading state
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Load profile data when screen loads
    LaunchedEffect(Unit) {
        scope.launch {
            ApiClient.getProfile()
                .onSuccess { response ->
                    if (response.status && response.user != null) {
                        username = response.user.fullname
                        selectedCountry = response.user.country
                        selectedGender = response.user.sex
                        bio = response.user.bio
                        image = response.user.image
                        isLoading = false
                    } else {
                        errorMessage = response.msg ?: "Failed to load profile"
                        isLoading = false
                    }
                }
                .onFailure { error ->
                    isLoading = false
                    errorMessage = error.message ?: "Unknown error occurred"
                    android.util.Log.e("EditProfile", "Failed to load profile: ${error.message}")
                }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        if (isLoading) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else if (errorMessage.isNotEmpty()) {
            // Error state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(30.dp)
                ) {
                    Text(
                        text = "Error Loading Profile",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    button(
                        label = "Back",
                        onClick = {
                            context.startActivity(Intent(context, Profile::class.java))
                        }
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 30.dp)
            ) {
            // Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Title(
                    title = "Edit",
                    modifier = Modifier.padding(bottom = 0.dp),
                    bottomPadding = 0.dp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Profile Image with Edit Button
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(100.dp)
            ) {
                val userImageUrl = if (image.isNotEmpty()) {
                    "${ApiClient.API_BASE_URL}/images/$image"
                } else {
                    null
                }

                if (userImageUrl != null) {
                    AsyncImage(
                        model = userImageUrl,
                        contentDescription = "User Photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.user),
                        contentDescription = "User Photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }
                EditButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 8.dp),
                    onClick = {
                        context.startActivity(Intent(context, Changeimg::class.java))
                    }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Username
            InputText(
                label = "Username",
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Country
            Column(modifier = Modifier.fillMaxWidth()) {
                SubTitleText(text = "Country", modifier = Modifier.padding(bottom = 8.dp))
                Dropdown(
                    hint = "Select Country",
                    items = countries,
                    value = selectedCountry,
                    onValueChange = { selectedCountry = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Gender
            Column(modifier = Modifier.fillMaxWidth()) {
                SubTitleText(text = "Gender", modifier = Modifier.padding(bottom = 8.dp))
                Dropdown(
                    hint = "Select Gender",
                    items = genderOptions,
                    value = selectedGender,
                    onValueChange = { selectedGender = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bio
            InputText(
                label = "Bio",
                value = bio,
                onValueChange = { bio = it },
                modifier = Modifier.fillMaxWidth(),
                height = 150.dp,
                singleLine = false
            )

            Spacer(modifier = Modifier.weight(1f))

            // Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                button(
                    label = "Cancel",
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        context.startActivity(Intent(context, Profile::class.java))
                    }
                )
                button(
                    label = "Save",
                    containerColor = Color(0xFFD60C0C),
                    contentColor = Color.White,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving && username.isNotEmpty() && selectedCountry.isNotEmpty() && selectedGender.isNotEmpty(),
                    onClick = {
                        scope.launch {
                            isSaving = true
                            ApiClient.updateProfile(
                                fullname = username,
                                country = selectedCountry,
                                gender = selectedGender,
                                bio = bio
                            ).onSuccess { response ->
                                isSaving = false
                                if (response.status) {
                                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                    android.util.Log.d("EditProfile", "Profile updated: ${response.msg}")
                                    // Navigate back to profile
                                    context.startActivity(Intent(context, Profile::class.java))
                                    (context as? ComponentActivity)?.finish()
                                } else {
                                    Toast.makeText(context, "Failed to update: ${response.msg}", Toast.LENGTH_SHORT).show()
                                    android.util.Log.e("EditProfile", "Update failed: ${response.msg}")
                                }
                            }.onFailure { error ->
                                isSaving = false
                                val errorMsg = error.message ?: "Unknown error occurred"
                                
                                // Check for unauthorized/token expiry
                                if (errorMsg.contains("401", ignoreCase = true) || 
                                    errorMsg.contains("unauthorized", ignoreCase = true) ||
                                    errorMsg.contains("token", ignoreCase = true)) {
                                    ApiClient.clearLoginData()
                                    Toast.makeText(context, "Session expired. Please login again", Toast.LENGTH_SHORT).show()
                                    context.startActivity(Intent(context, Login::class.java))
                                    (context as? ComponentActivity)?.finish()
                                } else {
                                    Toast.makeText(context, "Error: $errorMsg", Toast.LENGTH_SHORT).show()
                                }
                                android.util.Log.e("EditProfile", "Update failed: $errorMsg")
                            }
                        }
                    }
                )
            }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfilePreview() {
    XOXO_composeTheme {
        EditProfileScreen()
    }
}
