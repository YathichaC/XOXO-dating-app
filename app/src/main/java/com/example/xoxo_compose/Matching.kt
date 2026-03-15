package com.example.xoxo_compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.example.xoxo_compose.ui.theme.XOXO_composeTheme
import com.example.xoxo_compose.ui.theme.button
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Matching : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XOXO_composeTheme {
                MatchingScreen(
                    onMessageClick = {
                        val intent = Intent(this, Chatroom::class.java)
                        intent.putExtra("user_name", "Samantha")
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun MatchingScreen(onMessageClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Animation States
    val screenAlpha = remember { Animatable(0f) }
    val imageOffset = remember { Animatable(0f) }
    val imageRotation = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val buttonAlpha = remember { Animatable(0f) }

    // Lottie setup
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.matching))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LaunchedEffect(Unit) {
        // 1. Fade In Screen
        screenAlpha.animateTo(1f, tween(1000))
        delay(500)

        // 2. Unfold Images (Slide and Rotate)
        launch {
            imageOffset.animateTo(60f, tween(1200, easing = FastOutSlowInEasing))
        }
        launch {
            imageRotation.animateTo(15f, tween(1200, easing = FastOutSlowInEasing))
        }
        
        delay(800)
        // 3. Show "Match!" text
        textAlpha.animateTo(1f, tween(500))
        
        // 4. Fade in Button
        delay(200)
        buttonAlpha.animateTo(1f, tween(500))
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(screenAlpha.value),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.height(300.dp).fillMaxWidth()
                ) {
                    // Background Lottie Animation
                    if (composition != null) {
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier.requiredSize(550.dp),
                            contentScale = ContentScale.FillBounds
                        )
                    }

                    // Left Image (Target User)
                    Image(
                        painter = painterResource(id = R.drawable.user),
                        contentDescription = null,
                        modifier = Modifier
                            .size(150.dp)
                            .graphicsLayer {
                                translationX = -imageOffset.value
                                rotationZ = -imageRotation.value
                            }
                            .clip(CircleShape)
                            .background(Color.DarkGray),
                        contentScale = ContentScale.Crop
                    )

                    // Right Image (Current User)
                    Image(
                        painter = painterResource(id = R.drawable.user),
                        contentDescription = null,
                        modifier = Modifier
                            .size(150.dp)
                            .graphicsLayer {
                                translationX = imageOffset.value
                                rotationZ = imageRotation.value
                            }
                            .clip(CircleShape)
                            .background(Color.DarkGray),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "IT'S A MATCH!",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.alpha(textAlpha.value)
                )
                
                Text(
                    text = "You and Samantha liked each other",
                    color = Color.LightGray,
                    fontSize = 16.sp,
                    modifier = Modifier.alpha(textAlpha.value).padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))
                
                Box(modifier = Modifier.alpha(buttonAlpha.value)) {
                    button(
                        label = "Message",
                        onClick = {
                            scope.launch {
                                // Fade out before navigating
                                screenAlpha.animateTo(0f, tween(800))
                                onMessageClick()
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
fun MatchingPreview() {
    XOXO_composeTheme {
        MatchingScreen(onMessageClick = {})
    }
}
