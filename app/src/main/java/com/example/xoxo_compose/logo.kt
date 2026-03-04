package com.example.xoxo_compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.example.xoxo_compose.network.ApiClient
import com.example.xoxo_compose.ui.theme.XOXO_composeTheme
import kotlinx.coroutines.delay

class Logo : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialise SharedPreferences immediately
        ApiClient.initSharedPreferences(this)
        
        // 2. Pre-check login status before animation finishes
        val userData = ApiClient.getUserData()
        android.util.Log.d("Logo", "Initial check - userData: ${userData?.fullname ?: "NULL"}")

        setContent {
            XOXO_composeTheme {
                SplashScreen(
                    onAnimationEnd = {
                        // 3. Final check and navigate
                        val finalUserData = ApiClient.getUserData()
                        android.util.Log.d("Logo", "Animation end - userData: ${finalUserData?.fullname ?: "NULL"}")
                        
                        val targetActivity = if (finalUserData != null) {
                            Main::class.java
                        } else {
                            Login::class.java
                        }
                        
                        startActivity(Intent(this, targetActivity))
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun SplashScreen(onAnimationEnd: () -> Unit) {
    var isAnimationFinished by remember { mutableStateOf(false) }
    var startFadeOutScreen by remember { mutableStateOf(false) }

    val screenAlpha by animateFloatAsState(
        targetValue = if (startFadeOutScreen) 0f else 1f,
        animationSpec = tween(durationMillis = 800),
        label = "ScreenFadeOut"
    )

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.xoxologo)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        isPlaying = true
    )

    LaunchedEffect(progress) {
        if (progress >= 1f && composition != null) {
            isAnimationFinished = true
            delay(500)
            startFadeOutScreen = true
            delay(800)
            onAnimationEnd()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(screenAlpha)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (composition != null) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .size(400.dp)
                    .alpha(if (isAnimationFinished) 0f else 1f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LogoPreview() {
    XOXO_composeTheme {
        SplashScreen(onAnimationEnd = {})
    }
}
