package com.example.xoxo_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

class KYC : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XOXO_composeTheme {
                KYCScreen()
            }
        }
    }
}

@Composable
fun KYCScreen() {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp, vertical = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // This column will occupy the space above the button and center its content vertically
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.correct),
                    contentDescription = null,
                    modifier = Modifier
                        .size(121.dp)
                        .padding(bottom = 20.dp)
                )

                Title(
                    title = "Let's verify KYC",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    bottomPadding = 8.dp
                )

                NormalText(
                    text = "Identity verification is securely handled by DIDIT",
                    modifier = Modifier.padding(bottom = 40.dp),
                    textAlign = TextAlign.Center,
                    color = Color(0xFF8E8E8E)
                )

                // Instructions block
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Instruction 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 25.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.id_card),
                            contentDescription = null,
                            modifier = Modifier.size(45.dp)
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Column {
                            Text(
                                text = "1. Take a picture of your valid ID",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Take clear photos of both the front and back of your ID card.",
                                color = Color(0xFF8E8E8E),
                                fontSize = 14.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // Instruction 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.idenify),
                            contentDescription = null,
                            modifier = Modifier.size(45.dp)
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Column {
                            Text(
                                text = "2. Take a selfie of yourself",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Take a clear selfie to confirm that you match your ID and to prevent fake accounts.",
                                color = Color(0xFF8E8E8E),
                                fontSize = 14.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Button at the bottom
            button(
                label = "Let's verify",
                onClick = { /* Action */ }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun KYCPreview() {
    XOXO_composeTheme {
        KYCScreen()
    }
}
