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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xoxo_compose.ui.theme.XOXO_composeTheme

class Chatlist : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XOXO_composeTheme {
                ChatListScreen()
            }
        }
    }
}

data class ChatItem(
    val id: Int,
    val name: String,
    val lastMessage: String,
    val imageRes: Int
)

@Composable
fun ChatListScreen() {
    val context = LocalContext.current
    
    val chats = listOf(
        ChatItem(1, "Samantha", "How are you today?", R.drawable.user),
        ChatItem(2, "Jessica", "Let's meet up this weekend!", R.drawable.user),
        ChatItem(3, "Emily", "Haha that's so funny 😂", R.drawable.user),
        ChatItem(4, "Ashley", "I'm heading to work now.", R.drawable.user),
        ChatItem(5, "Amanda", "Good morning!", R.drawable.user),
        ChatItem(6, "Sarah", "See you later.", R.drawable.user)
    )

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
                Image(
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
                    text = "Chat",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(chats) { chat ->
                    ChatItemRow(chat)
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun ChatItemRow(chat: ChatItem) {
    val context = LocalContext.current

    val dotIconResId = remember(context) {
        context.resources.getIdentifier("dot3", "drawable", context.packageName)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { /* TODO: Open Chat */ },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = chat.imageRes),
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.DarkGray),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = chat.name,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = chat.lastMessage,
                color = Color.LightGray,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (dotIconResId != 0) {
            Image(
                painter = painterResource(id = dotIconResId),
                contentDescription = "More",
                modifier = Modifier
                    .size(16.dp)
                    .clickable {
                        val intent = Intent(context, Report::class.java)
                        intent.putExtra("reported_user_name", chat.name)
                        intent.putExtra("from_chat", true) // ระบุว่ามาจากหน้า Chatlist
                        context.startActivity(intent)
                    }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatListPreview() {
    XOXO_composeTheme {
        ChatListScreen()
    }
}
