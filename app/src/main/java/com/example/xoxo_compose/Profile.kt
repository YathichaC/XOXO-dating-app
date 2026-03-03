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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xoxo_compose.ui.theme.*

class Profile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

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

            Image(
                painter = painterResource(id = R.drawable.user),
                contentDescription = "User Photo",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                SubTitleText(text = "Username")
                Spacer(modifier = Modifier.height(8.dp))
                ReadOnlyField(text = "Samantha")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                SubTitleText(text = "Country")
                Spacer(modifier = Modifier.height(8.dp))
                ReadOnlyField(text = "Thailand", isDropdown = true)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                SubTitleText(text = "Gender")
                Spacer(modifier = Modifier.height(8.dp))
                ReadOnlyField(text = "Female", isDropdown = true)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                SubTitleText(text = "Bio")
                Spacer(modifier = Modifier.height(8.dp))
                ReadOnlyField(text = "Love traveling and discovering new food places.", height = 120)
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
                    context.startActivity(Intent(context, Login::class.java))
                }
            )
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
