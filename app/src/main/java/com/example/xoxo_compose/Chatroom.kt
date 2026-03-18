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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xoxo_compose.network.ApiClient
import com.example.xoxo_compose.ui.theme.XOXO_composeTheme

// ✅ FIX 1: Use aliases to avoid conflict between io.socket.client.IO and kotlinx.coroutines.Dispatchers.IO
import io.socket.client.IO as SocketIO
import io.socket.client.Socket as SocketClient

// ✅ FIX 2: Removed conflicting imports:
//   - REMOVED: import kotlinx.coroutines.Dispatchers.IO  (was shadowing io.socket.client.IO)
//   - REMOVED: import java.net.Socket                    (was shadowing io.socket.client.Socket)
//   - REMOVED: import java.net.URISyntaxException        (replaced with java.net below)
import java.net.URISyntaxException
import org.json.JSONObject

class Chatroom : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize SharedPreferences first
        ApiClient.initSharedPreferences(this)
        
        val matchesID = intent.getIntExtra("matchesID", 0)
        val matchedUserId = intent.getIntExtra("matchedUserId", 0)
        val userName = intent.getStringExtra("name") ?: "User"
        val currentUserID = intent.getIntExtra("currentUserID", 0)  // ✅ Get from intent

        setContent {
            XOXO_composeTheme {
                ChatroomScreen(
                    matchesID = matchesID,
                    matchedUserId = matchedUserId,
                    userName = userName,
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
    currentUserID: Int = 0  // ✅ New parameter
) {
    val context = LocalContext.current
    var messageText by remember { mutableStateOf("") }

    val messages = remember { mutableStateListOf<Message>() }
    val loading = remember { mutableStateOf(true) }

    // ✅ FIX 3: Type is now SocketClient (aliased io.socket.client.Socket) instead of java.net.Socket
    val socket = remember { mutableStateOf<SocketClient?>(null) }

    // ✅ Get userID - Try multiple sources in order of preference
    val userID = remember {
        // 1. Try intent parameter first (most reliable)
        if (currentUserID > 0) {
            Log.d("Socket", "✓ Using userID from intent: $currentUserID")
            return@remember currentUserID
        }
        
        // 2. Try from SharedPreferences
        val sharedPrefs = context.getSharedPreferences("xoxo_app_prefs", android.content.Context.MODE_PRIVATE)
        var id = sharedPrefs.getInt("user_id", 0)
        if (id > 0) {
            Log.d("Socket", "✓ Using userID from SharedPreferences: $id")
            return@remember id
        }
        
        // 3. Try to get from all saved preferences
        val allPrefs = sharedPrefs.all
        Log.d("Socket", "⚠️ SharedPreferences contents: $allPrefs")
        
        // 4. Last resort - try to extract from email hash or other means
        val email = sharedPrefs.getString("user_email", "")
        if (!email.isNullOrEmpty()) {
            Log.d("Socket", "⚠️ Found email but no user_id: $email")
        }
        
        Log.e("Socket", "❌ Could not find valid userID in any source!")
        0
    }

    // Initialize Socket.io
    LaunchedEffect(matchesID, userID) {
        if (userID <= 0) {
            Log.e("Socket", "❌ Invalid userID: $userID - Socket connection aborted")
            return@LaunchedEffect
        }

        Log.d("Socket", "✓ Starting Socket connection with userID: $userID")

        try {
            // ✅ FIX 4: Use SocketIO alias instead of IO (which was resolving to Dispatchers.IO)
            val opts = SocketIO.Options()
            opts.reconnection = true
            opts.reconnectionDelay = 1000
            opts.reconnectionDelayMax = 5000
            opts.reconnectionAttempts = 5

            val baseUrl = ApiClient.API_BASE_URL

            // ✅ FIX 5: Use SocketIO alias for socket factory call
            val newSocket = SocketIO.socket(baseUrl, opts)

            // ✅ FIX 6: Use SocketClient.EVENT_CONNECT (aliased io.socket.client.Socket)
            newSocket.on(SocketClient.EVENT_CONNECT) {
                Log.d("Socket", "✅ Socket connected successfully with ID: ${newSocket.id()}")

                val joinObj = JSONObject()
                joinObj.put("matchesID", matchesID)
                joinObj.put("userID", userID)
                Log.d("Socket", "📤 Emitting join-room with: matchesID=$matchesID, userID=$userID")
                newSocket.emit("join-room", joinObj)

                val histObj = JSONObject()
                histObj.put("matchesID", matchesID)
                histObj.put("limit", 50)
                Log.d("Socket", "📤 Emitting load-chat-history with: matchesID=$matchesID, limit=50")
                newSocket.emit("load-chat-history", histObj)
            }

            // Chat history event
            newSocket.on("chat-history") { args: Array<Any> ->
                try {
                    if (args.isNotEmpty()) {
                        val data = args[0] as? JSONObject
                        if (data != null) {
                            val messagesArray = data.optJSONArray("messages")
                            val loadedMessages = mutableListOf<Message>()

                            if (messagesArray != null) {
                                for (i in 0 until messagesArray.length()) {
                                    val msgObj = messagesArray.getJSONObject(i)
                                    loadedMessages.add(
                                        Message(
                                            id = msgObj.optInt("id", 0),
                                            text = msgObj.optString("content", ""),
                                            isMe = msgObj.optInt("userID", 0) == userID,
                                            userID = msgObj.optInt("userID", 0),
                                            timestamp = msgObj.optString("create_at", "")
                                        )
                                    )
                                }
                            }

                            messages.clear()
                            messages.addAll(loadedMessages)
                            loading.value = false
                            Log.d("Socket", "✅ Loaded ${loadedMessages.size} messages")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Socket", "❌ Error loading chat history: ${e.message}")
                }
            }

            // Incoming message event
            newSocket.on("chat-$matchesID") { args: Array<Any> ->
                try {
                    if (args.isNotEmpty()) {
                        val msgData = args[0] as? JSONObject
                        if (msgData != null) {
                            messages.add(
                                Message(
                                    id = msgData.optInt("id", 0),
                                    text = msgData.optString("content", ""),
                                    isMe = msgData.optInt("userID", 0) == userID,
                                    userID = msgData.optInt("userID", 0),
                                    timestamp = msgData.optString("create_at", "")
                                )
                            )
                            Log.d("Socket", "📨 Received new message from user ${msgData.optInt("userID", 0)}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Socket", "❌ Error receiving message: ${e.message}")
                }
            }

            // Error handler
            newSocket.on("chat-error") { args: Array<Any> ->
                if (args.isNotEmpty()) {
                    val err = args[0] as? JSONObject
                    Log.e("Socket", "Error: ${err?.optString("msg", "unknown")}")
                }
            }

            // ✅ FIX 7: Use SocketClient.EVENT_CONNECT_ERROR instead of Socket.EVENT_CONNECT_ERROR
            newSocket.on(SocketClient.EVENT_CONNECT_ERROR) { args: Array<Any> ->
                Log.e("Socket", "Connection error: ${args.joinToString()}")
            }

            newSocket.connect()
            socket.value = newSocket
            Log.d("Socket", "✓ Socket.connect() called - awaiting connection...")

        } catch (e: URISyntaxException) {
            Log.e("Socket", "❌ URI Syntax error: ${e.message}")
        } catch (e: Exception) {
            Log.e("Socket", "❌ Socket initialization error: ${e.message}", e)
        }
    }

    // Cleanup — FIX 8: socket.value is now SocketClient so .emit() and .disconnect() resolve correctly
    DisposableEffect(Unit) {
        onDispose {
            socket.value?.let { s ->
                val leaveObj = JSONObject()
                leaveObj.put("matchesID", matchesID)
                leaveObj.put("userID", userID)
                s.emit("leave-room", leaveObj)
                s.disconnect()
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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp, bottom = 16.dp),
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
                Spacer(modifier = Modifier.width(16.dp))
                Image(
                    painter = painterResource(id = R.drawable.user),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = userName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Messages
            if (loading.value) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading messages...",
                        color = Color.LightGray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(messages) { message ->
                        ChatMessageBubble(message)
                    }
                }
            }

            // Input area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ChatTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f)
                )
                Image(
                    painter = painterResource(id = R.drawable.send),
                    contentDescription = "Send",
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            // ✅ FIX 9: socket.value is SocketClient so .emit() resolves correctly
                            if (messageText.trim().isNotEmpty() && socket.value != null) {
                                val msgObj = JSONObject()
                                msgObj.put("matchesID", matchesID)
                                msgObj.put("content", messageText)
                                msgObj.put("type", "text")
                                msgObj.put("userID", userID)
                                Log.d("Socket", "📤 Sending message: '$messageText' (userID=$userID, matchesID=$matchesID)")
                                socket.value?.emit("chat", msgObj)
                                messageText = ""
                            }
                        }
                )
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: Message) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (message.isMe) Color(0xFFDD4F4F) else Color.DarkGray,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.widthIn(max = 250.dp)
        ) {
            Text(
                text = message.text,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun ChatTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.DarkGray,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier.height(40.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = "Type message...",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.White,
                    fontSize = 14.sp
                ),
                modifier = Modifier.fillMaxSize(),
                singleLine = true,
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White)
            )
        }
    }
}