package com.example.xoxo_compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import com.example.xoxo_compose.ui.theme.XOXO_composeTheme

class ImageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val fullname = intent.getStringExtra("fullname") ?: "User"
        val lifeImages = intent.getStringArrayListExtra("lifeImages") ?: arrayListOf()
        setContent {
            XOXO_composeTheme {
                ImageScreen(name = fullname, lifeImages = lifeImages)
            }
        }
    }
}

@Composable
fun ImageScreen(name: String, lifeImages: List<String> = emptyList()) {
    val context = LocalContext.current
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp, bottom = 16.dp)
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.arrow),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterStart)
                        .clickable {
                            context.startActivity(Intent(context, Main::class.java))
                        }
                )
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp)
            ) {
                items(lifeImages.size) { index ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.DarkGray)
                    ) {
                        if (lifeImages.isNotEmpty() && index < lifeImages.size) {
                            AsyncImage(
                                model = "${com.example.xoxo_compose.network.ApiClient.API_BASE_URL}/images/${lifeImages[index]}",
                                contentDescription = "Lifestyle Photo ${index + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = R.drawable.user),
                                contentDescription = "Person Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                alpha = 0.8f
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ImageScreenPreview() {
    XOXO_composeTheme {
        ImageScreen(name = "Samantha")
    }
}
