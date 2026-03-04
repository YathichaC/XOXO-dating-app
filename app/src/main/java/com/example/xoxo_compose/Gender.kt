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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xoxo_compose.ui.theme.NormalText
import com.example.xoxo_compose.ui.theme.Title
import com.example.xoxo_compose.ui.theme.XOXO_composeTheme
import com.example.xoxo_compose.ui.theme.button

class Gender : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XOXO_composeTheme {
                GenderScreen()
            }
        }
    }
}

@Composable
fun GenderScreen() {
    val context = LocalContext.current
    var selectedGender by remember { mutableStateOf("") }
    val isFormValid = selectedGender.isNotEmpty()

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp, vertical = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Content Area (Centered vertically, left-aligned content)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Title(
                    title = "Select your gender",
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                    bottomPadding = 8.dp
                )
                NormalText(
                    text = "Select the gender that represents you",
                    color = Color.LightGray,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(bottom = 40.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GenderBox(
                            label = "male",
                            iconBlack = R.drawable.male_black,
                            iconWhite = R.drawable.male_white,
                            isSelected = selectedGender == "male",
                            onClick = { selectedGender = "male" },
                            modifier = Modifier.weight(1f)
                        )
                        GenderBox(
                            label = "female",
                            iconBlack = R.drawable.female_black,
                            iconWhite = R.drawable.female_white,
                            isSelected = selectedGender == "female",
                            onClick = { selectedGender = "female" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GenderBox(
                            label = "other",
                            iconBlack = R.drawable.other_black,
                            iconWhite = R.drawable.other_white,
                            isSelected = selectedGender == "other",
                            onClick = { selectedGender = "other" },
                            modifier = Modifier.weight(1f)
                        )
                        GenderBox(
                            label = "not prefer to say",
                            iconBlack = R.drawable.cross_black,
                            iconWhite = R.drawable.cross_white,
                            isSelected = selectedGender == "not",
                            onClick = { selectedGender = "not" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Footer Area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                button(
                    label = "Next",
                    enabled = isFormValid,
                    onClick = {
                        context.startActivity(Intent(context, Uploadpic::class.java))
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Progress Indicators (Expanded to fill width)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(Color(0xFFD60C0C), shape = RoundedCornerShape(2.dp))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(Color.White, shape = RoundedCornerShape(2.dp))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(Color.White, shape = RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun GenderBox(
    label: String,
    iconBlack: Int,
    iconWhite: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(140.dp)
            .background(
                color = if (isSelected) Color(0xFFD60C0C) else Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = if (isSelected) iconWhite else iconBlack),
                contentDescription = label,
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                color = if (isSelected) Color.White else Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GenderPreview() {
    XOXO_composeTheme {
        GenderScreen()
    }
}
