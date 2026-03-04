package com.example.xoxo_compose

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import com.example.xoxo_compose.network.ApiClient
import com.example.xoxo_compose.ui.theme.*
import kotlinx.coroutines.launch

class Profile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Initialize SharedPreferences
        ApiClient.initSharedPreferences(this)
        setContent {
            XOXO_composeTheme {
                ProfileScreen()
            }
        }
    }
}

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    
    // Profile data state
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var fullname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var birthdate by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("") }
    var image by remember { mutableStateOf("") }

    // Load profile data when screen loads
    LaunchedEffect(Unit) {
        scope.launch {
            ApiClient.getProfile()
                .onSuccess { response ->
                    if (response.status && response.user != null) {
                        fullname = response.user.fullname
                        email = response.user.email
                        country = response.user.country
                        birthdate = response.user.birthdate
                        bio = response.user.bio
                        sex = response.user.sex
                        image = response.user.image
                        isLoading = false
                    } else {
                        errorMessage = response.msg ?: "Failed to load profile"
                        isLoading = false
                    }
                }
                .onFailure { error ->
                    isLoading = false
                    val errorMsg = error.message ?: "Unknown error occurred"
                    
                    // Check for unauthorized/token expiry
                    if (errorMsg.contains("401", ignoreCase = true) || 
                        errorMsg.contains("unauthorized", ignoreCase = true) ||
                        errorMsg.contains("token", ignoreCase = true)) {
                        ApiClient.clearLoginData()
                        // Navigate to login
                        context.startActivity(Intent(context, Login::class.java))
                        (context as? ComponentActivity)?.finish()
                    } else {
                        errorMessage = errorMsg
                    }
                    
                    android.util.Log.e("Profile", "Failed to load profile: $errorMsg")
                }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(text = "Delete Account")
            },
            text = {
                Text(text = "Are you sure you want to delete this account?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        // TODO: Add logic to delete account
                    }
                ) {
                    Text("Yes", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("No", color = Color.Black)
                }
            }
        )
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
                        label = "SignOut",
                        onClick = {
                            android.util.Log.d("Profile", "=== SIGNOUT CLICKED (ERROR STATE) ===")
                            ApiClient.clearLoginData()
                            android.util.Log.d("Profile", "✓ Data cleared, navigating to Login")
                            context.startActivity(Intent(context, Login::class.java))
                            (context as? ComponentActivity)?.finish()
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
                    .verticalScroll(scrollState)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.arrow),
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                context.startActivity(Intent(context, Main::class.java))
                            }
                    )
                    Text(
                        text = "Profile",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Image(
                        painter = painterResource(id = R.drawable.edit),
                        contentDescription = "Edit",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                context.startActivity(Intent(context, EditProfile::class.java))
                            }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // User Image from API
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
                            .size(100.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterHorizontally),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.user),
                        contentDescription = "User Photo",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    SubTitleText(text = "Full Name")
                    Spacer(modifier = Modifier.height(8.dp))
                    ReadOnlyField(text = fullname)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    SubTitleText(text = "Email")
                    Spacer(modifier = Modifier.height(8.dp))
                    ReadOnlyField(text = email)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    SubTitleText(text = "Birth Date")
                    Spacer(modifier = Modifier.height(8.dp))
                    ReadOnlyField(text = birthdate)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    SubTitleText(text = "Country")
                    Spacer(modifier = Modifier.height(8.dp))
                    ReadOnlyField(text = country, isDropdown = true)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    SubTitleText(text = "Gender")
                    Spacer(modifier = Modifier.height(8.dp))
                    ReadOnlyField(text = sex, isDropdown = true)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    SubTitleText(text = "Bio")
                    Spacer(modifier = Modifier.height(8.dp))
                    ReadOnlyField(text = bio, height = 120)
                }

                Spacer(modifier = Modifier.height(20.dp))

                button(
                    label = "Delete this account",
                    containerColor = Color(0xFFD60C0C),
                    contentColor = Color.White,
                    onClick = { showDeleteDialog = true }
                )
                Spacer(modifier = Modifier.height(10.dp))
                button(
                    label = "Sign out",
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    modifier = Modifier.padding(bottom = 30.dp),
                    onClick = {
                        android.util.Log.d("Profile", "=== SIGNOUT CLICKED ===")
                        ApiClient.clearLoginData()
                        android.util.Log.d("Profile", "✓ Data cleared, navigating to Login")
                        context.startActivity(Intent(context, Login::class.java))
                        (context as? ComponentActivity)?.finish()
                    }
                )
            }
        }
    }
}

@Composable
fun ReadOnlyField(text: String, height: Int = 45, isDropdown: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .background(Color(0xFF1F1F1F), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = if (height > 45) 12.dp else 0.dp),
        contentAlignment = if (height > 45) Alignment.TopStart else Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = Color.Gray,
            fontSize = 16.sp
        )
        if (isDropdown) {
            Text(
                text = "▼",
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfilePreview() {
    XOXO_composeTheme {
        ProfileScreen()
    }
}
