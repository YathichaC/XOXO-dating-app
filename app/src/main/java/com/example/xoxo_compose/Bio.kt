package com.example.xoxo_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xoxo_compose.ui.theme.InputText
import com.example.xoxo_compose.ui.theme.NormalText
import com.example.xoxo_compose.ui.theme.Title
import com.example.xoxo_compose.ui.theme.XOXO_composeTheme
import com.example.xoxo_compose.ui.theme.button

class Bio : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XOXO_composeTheme {
                BioScreen()
            }
        }
    }
}

@Composable
fun BioScreen() {
    var bioText by remember { mutableStateOf("") }
    val isFormValid = bioText.isNotEmpty()

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp, vertical = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Title(
                    title = "Bio",
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                    bottomPadding = 8.dp
                )
                NormalText(
                    text = "Add your profile to personalize your account",
                    color = Color.LightGray,
                    textAlign = TextAlign.Start,
                    // ปรับให้ชิดกับ InputText เลย
                    modifier = Modifier.padding(bottom = 0.dp)
                )

                InputText(
                    label = "",
                    value = bioText,
                    onValueChange = { bioText = it },
                    modifier = Modifier.fillMaxWidth(),
                    height = 200.dp,
                    singleLine = false
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                button(
                    label = "Done",
                    enabled = isFormValid,

                    modifier = Modifier.padding(top = 30.dp, bottom = 30.dp),
                    onClick = { /* TODO: Finish Registration */ }
                )

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
                            .background(Color(0xFFD60C0C), shape = RoundedCornerShape(2.dp))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(Color(0xFFD60C0C), shape = RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BioPreview() {
    XOXO_composeTheme {
        BioScreen()
    }
}
