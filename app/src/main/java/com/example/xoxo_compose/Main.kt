package com.example.xoxo_compose

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.xoxo_compose.network.ApiClient
import com.example.xoxo_compose.network.DiscoverUser
import com.example.xoxo_compose.ui.theme.MainActionButtons
import com.example.xoxo_compose.ui.theme.XOXO_composeTheme
import com.example.xoxo_compose.ui.theme.ui.theme.CircleNumberBadge
import kotlinx.coroutines.launch

class Main : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        android.util.Log.d("Main", "=== MAIN ACTIVITY CREATED ===")
        
        // Initialize SharedPreferences
        ApiClient.initSharedPreferences(this)
        android.util.Log.d("Main", "SharedPreferences initialized")
        
        // Dump all saved data
        val allData = ApiClient.dumpAllPreferences()
        android.util.Log.d("Main", allData)
        
        // Check if user is still logged in
        val userData = ApiClient.getUserData()
        android.util.Log.d("Main", "After init - userData: ${userData?.fullname ?: "NULL"}")
        
        if (userData == null) {
            // User is not logged in, redirect to login
            android.util.Log.e("Main", "✗ User not logged in, redirecting to Login")
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }
        
        android.util.Log.d("Main", "✓ User logged in as: ${userData.fullname}")
        
        setContent {
            XOXO_composeTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Reload user data from SharedPreferences on every recomposition
    var userData by remember { mutableStateOf(ApiClient.getUserData()) }

    // State for swipeable profiles
    var profiles by remember { mutableStateOf<List<DiscoverUser>>(emptyList()) }
    var currentProfileIndex by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    // Load profiles on startup
    LaunchedEffect(Unit) {
        scope.launch {
            android.util.Log.d("MainScreen", "=== MAINSCREEN LAUNCHED EFFECT ===")
            
            // Reload user data to ensure we have the latest
            val freshUserData = ApiClient.getUserData()
            android.util.Log.d("MainScreen", "Fresh user data: ${freshUserData?.fullname ?: "NULL"}")
            
            // Dump all saved data
            val allData = ApiClient.dumpAllPreferences()
            android.util.Log.d("MainScreen", allData)
            
            userData = freshUserData
            
            // Check if there are saved filter preferences
            val savedFilter = ApiClient.getFilterPreferences()
            
            if (savedFilter != null) {
                android.util.Log.d("MainScreen", "Using saved filter: age=${savedFilter.ageRange}, country=${savedFilter.country}, gender=${savedFilter.gender}")
                
                // Use discoverMatches with saved filters
                ApiClient.discoverMatches(
                    limit = "20",
                    ageRange = savedFilter.ageRange,
                    country = savedFilter.country,
                    gender = savedFilter.gender
                ).onSuccess { response ->
                    if (response.status) {
                        profiles = response.users
                        isLoading = false
                        android.util.Log.d("MainScreen", "✓ Loaded ${response.count} profiles with filters")
                    } else {
                        errorMessage = response.msg
                        isLoading = false
                    }
                }.onFailure { error ->
                    errorMessage = error.message ?: "Failed to load profiles"
                    isLoading = false
                    android.util.Log.e("MainScreen", "✗ Error loading profiles: ${error.message}")
                }
            } else {
                android.util.Log.d("MainScreen", "No saved filter, loading all profiles")
                
                // Fall back to loading all profiles
                ApiClient.getDiscoverProfiles(limit = 20)
                    .onSuccess { response ->
                        if (response.status) {
                            profiles = response.users
                            isLoading = false
                            android.util.Log.d("MainScreen", "✓ Loaded ${response.count} profiles")
                        } else {
                            errorMessage = response.msg
                            isLoading = false
                        }
                    }
                    .onFailure { error ->
                        errorMessage = error.message ?: "Failed to load profiles"
                        isLoading = false
                        android.util.Log.e("MainScreen", "✗ Error loading profiles: ${error.message}")
                    }
            }
        }
    }

    // Swipe state
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val rotation = (offsetX.value / 20f)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 30.dp)
        ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (userData != null) {
                        AsyncImage(
                            model = "${ApiClient.API_BASE_URL}/images/${userData!!.image}",
                            contentDescription = "User Profile",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    context.startActivity(Intent(context, Profile::class.java))
                                },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.user),
                            contentDescription = "User Profile",
                            modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    context.startActivity(Intent(context, Profile::class.java))
                                }
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.filter),
                            contentDescription = "Filter",
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    context.startActivity(Intent(context, Filter::class.java))
                                }
                        )
                        Box(modifier = Modifier.wrapContentSize()) {
                            Image(
                                painter = painterResource(id = R.drawable.chat),
                                contentDescription = "Chat",
                                modifier = Modifier
                                    .size(30.dp)
                                    .clickable {
                                        context.startActivity(Intent(context, Chatlist::class.java))
                                    }
                            )
                            CircleNumberBadge(
                                number = "3",
                                size = 20,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 6.dp, y = (-6).dp)
                            )
                        }
                    }
                }

            if (isLoading) {
                    Spacer(modifier = Modifier.height(40.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(color = Color.White)
                    }
                } else if (profiles.isEmpty()) {
                    Spacer(modifier = Modifier.height(40.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(30.dp)
                        ) {
                            Text(
                                text = "No more profiles",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    val currentProfile = profiles.getOrNull(currentProfileIndex)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp)
                            .padding(vertical = 10.dp)
                            .graphicsLayer(
                                translationX = offsetX.value,
                                translationY = offsetY.value,
                                rotationZ = rotation
                            )
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragEnd = {
                                        scope.launch {
                                            if (offsetX.value > 400f) {
                                                // Swipe Right -> Like
                                                offsetX.animateTo(1000f, tween(300))
                                                
                                                currentProfile?.let {
                                                    ApiClient.recordSwipe(it.id, "like")
                                                        .onSuccess { result ->
                                                            Toast.makeText(
                                                                context,
                                                                if (result.matched) "Match! 🎉" else "Liked!",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            android.util.Log.d("MainScreen", "Like recorded for user ${it.id}")
                                                        }
                                                        .onFailure { error ->
                                                            Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                                                            android.util.Log.e("MainScreen", "Swipe error: ${error.message}")
                                                        }
                                                }
                                                
                                                offsetX.snapTo(0f)
                                                offsetY.snapTo(0f)
                                                currentProfileIndex++
                                            } else if (offsetX.value < -400f) {
                                                // Swipe Left -> Dislike
                                                offsetX.animateTo(-1000f, tween(300))
                                                
                                                currentProfile?.let {
                                                    ApiClient.recordSwipe(it.id, "dislike")
                                                        .onSuccess {
                                                            Toast.makeText(context, "Disliked", Toast.LENGTH_SHORT).show()
                                                            //android.util.Log.d("MainScreen", "Dislike recorded for user ${it.id}")
                                                        }
                                                        .onFailure { error ->
                                                            Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                                                        }
                                                }
                                                
                                                offsetX.snapTo(0f)
                                                offsetY.snapTo(0f)
                                                currentProfileIndex++
                                            } else {
                                                // Return to center if not swiped far enough
                                                launch { offsetX.animateTo(0f, tween(300)) }
                                                launch { offsetY.animateTo(0f, tween(300)) }
                                            }
                                        }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        scope.launch {
                                            offsetX.snapTo(offsetX.value + dragAmount.x)
                                            offsetY.snapTo(offsetY.value + dragAmount.y)
                                        }
                                    }
                                )
                            }
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.DarkGray)
                    ) {
                    // Display current profile image
                    if (currentProfile != null) {
                        if (currentProfile.profileImage != null) {
                            AsyncImage(
                                model = "${ApiClient.API_BASE_URL}/images/${currentProfile.profileImage}",
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {
                                        val intent = Intent(context, ImageActivity::class.java)
                                        intent.putExtra("fullname", currentProfile.fullname)
                                        intent.putStringArrayListExtra("lifeImages", ArrayList(currentProfile.lifeImages))
                                        context.startActivity(intent)
                                    },
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.user),
                                contentDescription = "Person Photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {
                                        context.startActivity(Intent(context, ImageActivity::class.java))
                                    },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Action Buttons - Bottom Center
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 20.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        MainActionButtons(
                            onWrongClick = {
                                scope.launch {
                                    offsetX.animateTo(-1000f, tween(300))
                                    currentProfile?.let {
                                        ApiClient.recordSwipe(it.id, "dislike").onSuccess {
                                            Toast.makeText(context, "Disliked", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    offsetX.snapTo(0f)
                                    offsetY.snapTo(0f)
                                    currentProfileIndex++
                                }
                            },
                            onLikeClick = {
                                scope.launch {
                                    offsetX.animateTo(1000f, tween(300))
                                    currentProfile?.let {
                                        ApiClient.recordSwipe(it.id, "like").onSuccess {
                                            Toast.makeText(context, "Liked!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    offsetX.snapTo(0f)
                                    offsetY.snapTo(0f)
                                    currentProfileIndex++
                                }
                            }
                        )
                    }
                }

                currentProfile?.let {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = it.fullname,
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = it.country,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val intent = Intent(context, Report::class.java)
                                        intent.putExtra("reported_user_name", it.fullname)
                                        intent.putExtra("reported_user_id", it.id)
                                        context.startActivity(intent)
                                    },
                                color = Color(0xFFDC143C)
                            ) {
                                Text(
                                    text = "Report",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(8.dp, 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = it.bio,
                            color = Color.White,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    XOXO_composeTheme {
        MainScreen()
    }
}
