package com.example.xoxo_compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xoxo_compose.ui.theme.*

class EditProfile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XOXO_composeTheme {
                EditProfileScreen()
            }
        }
    }
}

@Composable
fun EditProfileScreen() {
    val context = LocalContext.current
    val countries = stringArrayResource(R.array.country_list).toList()

    var username by remember { mutableStateOf("Samantha") }
    var selectedCountry by remember { mutableStateOf("Thailand") }
    var selectedGender by remember { mutableStateOf("Female") }
    var bio by remember { mutableStateOf("Love traveling and discovering new food places.") }

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
            // Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Title(
                    title = "Edit",
                    modifier = Modifier.padding(bottom = 0.dp),
                    bottomPadding = 0.dp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Profile Image with Edit Button
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(100.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.user),
                    contentDescription = "User Photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
                EditButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 8.dp),
                    onClick = {
                        context.startActivity(Intent(context, Changeimg::class.java))
                    }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Username
            InputText(
                label = "Username",
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Country
            Column(modifier = Modifier.fillMaxWidth()) {
                SubTitleText(text = "Country", modifier = Modifier.padding(bottom = 8.dp))
                Dropdown(
                    hint = "Select Country",
                    items = countries,
                    value = selectedCountry,
                    onValueChange = { selectedCountry = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Gender
            Column(modifier = Modifier.fillMaxWidth()) {
                SubTitleText(text = "Gender", modifier = Modifier.padding(bottom = 8.dp))
                Dropdown(
                    hint = "Select Gender",
                    items = listOf("Male", "Female", "Other", "Not prefer to say"),
                    value = selectedGender,
                    onValueChange = { selectedGender = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bio
            InputText(
                label = "Bio",
                value = bio,
                onValueChange = { bio = it },
                modifier = Modifier.fillMaxWidth(),
                height = 150.dp,
                singleLine = false
            )

            Spacer(modifier = Modifier.weight(1f))

            // Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                button(
                    label = "Cancel",
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        context.startActivity(Intent(context, Profile::class.java))
                    }
                )
                button(
                    label = "Save",
                    containerColor = Color(0xFFD60C0C),
                    contentColor = Color.White,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        // TODO: Save logic
                        context.startActivity(Intent(context, Profile::class.java))
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfilePreview() {
    XOXO_composeTheme {
        EditProfileScreen()
    }
}
