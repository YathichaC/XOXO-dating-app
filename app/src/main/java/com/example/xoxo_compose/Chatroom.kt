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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.xoxo_compose.network.ApiClient
import com.example.xoxo_compose.ui.theme.XOXO_composeTheme
import io.socket.client.IO as SocketIO
import io.socket.client.Socket as SocketClient
import java.net.URISyntaxException
import org.json.JSONObject

class Chatroom : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ApiClient.initSharedPreferences(this)

        val matchesID      = intent.getIntExtra("matchesID", 0)
        val matchedUserId  = intent.getIntExtra("matchedUserId", 0)
        val userName       = intent.getStringExtra("name") ?: "User"
        val userImage      = intent.getStringExtra("userImage") ?: ""   // ← NEW
        val currentUserID  = intent.getIntExtra("currentUserID", 0)

        setContent {
            XOXO_composeTheme {
                ChatroomScreen(
                    matchesID     = matchesID,
                    matchedUserId = matchedUserId,
                    userName      = userName,
                    userImage     = userImage,                          // ← NEW
                    currentUserID = currentUserID
                )
            }
        }
    }
}

data class Message(
    val id: Int = 0,
    val text: String,
    val isMe: Boolean,
    val userID: Int = 0,
    val timestamp: String = ""
)

@Composable
fun ChatroomScreen(
    matchesID: Int,
    matchedUserId: Int,
    userName: String,
    userImage: String = "",                                             // ← NEW
    currentUserID: Int = 0
) {
    val context     = LocalContext.current
    var messageText by remember { mutableStateOf("") }
    val messages    = remember { mutableStateListOf<Message>() }
    val loading     = remember { mutableStateOf(true) }
    val socket      = remember { mutableStateOf<SocketClient?>(null) }
    val listState   = rememberLazyListState()

    val userID = remember {
        if (currentUserID > 0) return@remember currentUserID
        val sharedPrefs = context.getSharedPreferences("xoxo_app_prefs", android.content.Context.MODE_PRIVATE)
        val id = sharedPrefs.getInt("user_id", 0)
        if (id > 0) return@remember id
        Log.e("Socket", "❌ Could not find valid userID!")
        0
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Socket.io setup
    LaunchedEffect(matchesID, userID) {
        if (userID <= 0) {
            Log.e("Socket", "❌ Invalid userID: $userID — aborted")
            return@LaunchedEffect
        }
        try {
            val opts = SocketIO.Options().apply {
                reconnection = true
                reconnectionDelay = 1000
                reconnectionDelayMax = 5000
                reconnectionAttempts = 5
            }
            val newSocket = SocketIO.socket(ApiClient.API_BASE_URL, opts)

            newSocket.on(SocketClient.EVENT_CONNECT) {
                Log.d("Socket", "✅ Connected: ${newSocket.id()}")
                newSocket.emit("join-room", JSONObject().apply {
                    put("matchesID", matchesID); put("userID", userID)
                })
                newSocket.emit("load-chat-history", JSONObject().apply {
                    put("matchesID", matchesID); put("limit", 50)
                })
            }

            newSocket.on("chat-history") { args ->
                try {
                    val data = args.getOrNull(0) as? JSONObject ?: return@on
                    val arr  = data.optJSONArray("messages") ?: return@on
                    val loaded = (0 until arr.length()).map { i ->
                        val m = arr.getJSONObject(i)
                        Message(
                            id        = m.optInt("id", 0),
                            text      = m.optString("content", ""),
                            isMe      = m.optInt("userID", 0) == userID,
                            userID    = m.optInt("userID", 0),
                            timestamp = m.optString("create_at", "")
                        )
                    }
                    messages.clear()
                    messages.addAll(loaded)
                    loading.value = false
                    Log.d("Socket", "✅ Loaded ${loaded.size} messages")
                } catch (e: Exception) {
                    Log.e("Socket", "❌ History error: ${e.message}")
                }
            }

            newSocket.on("chat-$matchesID") { args ->
                try {
                    val msgData = args.getOrNull(0) as? JSONObject ?: return@on
                    messages.add(Message(
                        id        = msgData.optInt("id", 0),
                        text      = msgData.optString("content", ""),
                        isMe      = msgData.optInt("userID", 0) == userID,
                        userID    = msgData.optInt("userID", 0),
                        timestamp = msgData.optString("create_at", "")
                    ))
                } catch (e: Exception) {
                    Log.e("Socket", "❌ Message error: ${e.message}")
                }
            }

            newSocket.on("chat-error") { args ->
                val err = args.getOrNull(0) as? JSONObject
                Log.e("Socket", "Error: ${err?.optString("msg", "unknown")}")
            }

            newSocket.on(SocketClient.EVENT_CONNECT_ERROR) { args ->
                Log.e("Socket", "Connect error: ${args.joinToString()}")
            }

            newSocket.connect()
            socket.value = newSocket
        } catch (e: URISyntaxException) {
            Log.e("Socket", "❌ URI error: ${e.message}")
        } catch (e: Exception) {
            Log.e("Socket", "❌ Socket error: ${e.message}", e)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            socket.value?.let { s ->
                s.emit("leave-room", JSONObject().apply {
                    put("matchesID", matchesID); put("userID", userID)
                })
                s.disconnect()
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ── Header ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.arrow),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            context.startActivity(Intent(context, Chatlist::class.java))
                        }
                )
                Spacer(modifier = Modifier.width(14.dp))

                // ── Profile image: Coil if URL available, fallback to drawable ──
                val imageUrl = if (userImage.isNotEmpty())
                    "${ApiClient.API_BASE_URL}/uploads/$userImage"
                else null



                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = userName,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ── Messages ──────────────────────────────────────────
            if (loading.value) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading messages...", color = Color.LightGray)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(messages) { message ->
                        ChatMessageBubble(message)
                    }
                }
            }

            // ── Input area ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Text input
                Surface(
                    color = Color(0xFF1E1E1E),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 46.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (messageText.isEmpty()) {
                            Text(
                                text = "Type a message...",
                                color = Color(0xFF666666),
                                fontSize = 14.sp
                            )
                        }
                        BasicTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White)
                        )
                    }
                }

                // Send button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (messageText.trim().isNotEmpty()) Color(0xFFDD4F4F)
                            else Color(0xFF2A2A2A)
                        )
                        .clickable(enabled = messageText.trim().isNotEmpty()) {
                            socket.value?.let { s ->
                                val msgObj = JSONObject().apply {
                                    put("matchesID", matchesID)
                                    put("content", messageText.trim())
                                    put("type", "text")
                                    put("userID", userID)
                                }
                                Log.d("Socket", "📤 Sending: '${messageText.trim()}'")
                                s.emit("chat", msgObj)
                                messageText = ""
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.send),
                        contentDescription = "Send",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: Message) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (message.isMe) Color(0xFFDD4F4F) else Color(0xFF2A2A2A),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isMe) 16.dp else 4.dp,
                bottomEnd = if (message.isMe) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 260.dp)
        ) {
            Text(
                text = message.text,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}