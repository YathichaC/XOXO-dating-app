package com.example.xoxo_compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xoxo_compose.ui.theme.Dropdown
import com.example.xoxo_compose.ui.theme.SubTitleText
import com.example.xoxo_compose.ui.theme.Title
import com.example.xoxo_compose.ui.theme.XOXO_composeTheme

class Filter : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XOXO_composeTheme {
                FilterScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen() {
    val context = LocalContext.current
    val countries = stringArrayResource(R.array.country_list).toList()
    
    var age by remember { mutableStateOf(24f) }
    var selectedCountry by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp, vertical = 50.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
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
                
                Spacer(modifier = Modifier.weight(1f))
                
                Title(
                    title = "Filter",
                    modifier = Modifier.padding(bottom = 0.dp),
                    bottomPadding = 0.dp
                )
                
                Spacer(modifier = Modifier.weight(1.2f))
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Age Range
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SubTitleText(text = "Age Range")
                    Text(
                        text = "${age.toInt()}+",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Slider(
                    value = age,
                    onValueChange = { age = it },
                    valueRange = 18f..50f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFD60C0C),
                        activeTrackColor = Color(0xFFD60C0C),
                        inactiveTrackColor = Color.DarkGray
                    ),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(Color(0xFFD60C0C), CircleShape)
                        )
                    },
                    track = { sliderState ->
                        SliderDefaults.Track(
                            sliderState = sliderState,
                            modifier = Modifier.height(4.dp),
                            colors = SliderDefaults.colors(
                                activeTrackColor = Color(0xFFD60C0C),
                                inactiveTrackColor = Color.DarkGray
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Country
            Column(modifier = Modifier.fillMaxWidth()) {
                SubTitleText(
                    text = "Country",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Dropdown(
                    hint = "Select Country",
                    items = countries,
                    value = selectedCountry,
                    onValueChange = { selectedCountry = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Gender
            Column(modifier = Modifier.fillMaxWidth()) {
                SubTitleText(
                    text = "Gender",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Dropdown(
                    hint = "Select Gender",
                    items = listOf("Male", "Female", "Other", "Not prefer to say"),
                    value = selectedGender,
                    onValueChange = { selectedGender = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FilterScreenPreview() {
    XOXO_composeTheme {
        FilterScreen()
    }
}
