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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.xoxo_compose.ui.theme.MainActionButtons
import com.example.xoxo_compose.ui.theme.XOXO_composeTheme
import com.example.xoxo_compose.ui.theme.ui.theme.CircleNumberBadge
import kotlinx.coroutines.launch

class Main : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                Image(
                    painter = painterResource(id = R.drawable.user),
                    contentDescription = "User Profile",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            context.startActivity(Intent(context, Profile::class.java))
                        }
                )
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
                                .clickable { /* TODO: Chat Action */ }
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
                                        Toast.makeText(context, "Liked!", Toast.LENGTH_SHORT).show()
                                        offsetX.snapTo(0f)
                                        offsetY.snapTo(0f)
                                    } else if (offsetX.value < -400f) {
                                        // Swipe Left -> Dislike
                                        offsetX.animateTo(-1000f, tween(300))
                                        Toast.makeText(context, "Disliked", Toast.LENGTH_SHORT).show()
                                        offsetX.snapTo(0f)
                                        offsetY.snapTo(0f)
                                    } else {
                                        // Return to center
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
                Image(
                    painter = painterResource(id = R.drawable.user), 
                    contentDescription = "Person Photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            context.startActivity(Intent(context, ImageActivity::class.java))
                        },
                    contentScale = ContentScale.Crop,
                    alpha = 0.6f
                )
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
                                Toast.makeText(context, "Disliked", Toast.LENGTH_SHORT).show()
                                offsetX.snapTo(0f)
                                offsetY.snapTo(0f)
                            }
                        },
                        onLikeClick = {
                            scope.launch {
                                offsetX.animateTo(1000f, tween(300))
                                Toast.makeText(context, "Liked!", Toast.LENGTH_SHORT).show()
                                offsetX.snapTo(0f)
                                offsetY.snapTo(0f)
                            }
                        }
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Samantha",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .background(Color.Red, RoundedCornerShape(10.dp))
                            .clickable {
                                context.startActivity(Intent(context, Report::class.java))
                            }
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Report",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "24 years old, Thailand",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Love traveling and discovering new food places. Looking for someone to share adventures with!",
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.navigationBarsPadding())
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
