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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
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
import com.example.xoxo_compose.network.ApiClient
import com.example.xoxo_compose.network.ChatMatch
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    val matchesID: Int,
    val matchedUserId: Int,
    val name: String,
    val lastMessage: String,
    val imageUrl: String
)

@Composable
fun ChatListScreen() {
    val context = LocalContext.current
    val chats = remember { mutableStateOf<List<ChatItem>>(emptyList()) }
    val loading = remember { mutableStateOf(true) }
    val error = remember { mutableStateOf<String?>(null) }

    // ✅ Get current user ID from SharedPreferences
    val currentUserID = remember {
        val sharedPrefs = context.getSharedPreferences("xoxo_app_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.getInt("user_id", 0)
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Main) {
            val result = ApiClient.getChatList()
            result.onSuccess { response ->
                if (response.status) {
                    val chatList = response.matches.map { match ->
                        ChatItem(
                            matchesID = match.matchesID,
                            matchedUserId = match.matchedUserId,
                            name = match.fullname,
                            lastMessage = match.lastMessage ?: "No messages yet",
                            imageUrl = match.image
                        )
                    }
                    chats.value = chatList
                    error.value = null
                } else {
                    error.value = response.msg
                }
                loading.value = false
            }
            result.onFailure {
                error.value = it.message ?: "Failed to load chats"
                loading.value = false
            }
        }
    }

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

            when {
                loading.value -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFF1493))
                    }
                }
                error.value != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = error.value ?: "Unknown error",
                                color = Color.LightGray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
                chats.value.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    ) {
                        Text(
                            text = "No chats yet",
                            color = Color.LightGray,
                            fontSize = 16.sp
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(chats.value) { chat ->
                            ChatItemRow(chat, currentUserID)
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
    }
}

@Composable
fun ChatItemRow(chat: ChatItem, currentUserID: Int) {
    val context = LocalContext.current

    val dotIconResId = remember(context) {
        context.resources.getIdentifier("dot3", "drawable", context.packageName)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                val intent = Intent(context, Chatroom::class.java)
                intent.putExtra("matchesID", chat.matchesID)
                intent.putExtra("matchedUserId", chat.matchedUserId)
                intent.putExtra("name", chat.name)
                intent.putExtra("userImage", chat.imageUrl)      // ✅ Pass profile image
                intent.putExtra("currentUserID", currentUserID)  // ✅ Pass current user ID
                context.startActivity(intent)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (chat.imageUrl.isNotEmpty()) {
            AsyncImage(
                model = ApiClient.API_BASE_URL + "/images/" + chat.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.user),
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray),
                contentScale = ContentScale.Crop
            )
        }

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
                        intent.putExtra("reported_user_id", chat.matchedUserId)
                        intent.putExtra("from_chat", true)
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