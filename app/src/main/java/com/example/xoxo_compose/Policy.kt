package com.example.xoxo_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xoxo_compose.ui.theme.Title
import com.example.xoxo_compose.ui.theme.XOXO_composeTheme
import com.example.xoxo_compose.ui.theme.button

class Policy : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XOXO_composeTheme {
                PolicyScreen()
            }
        }
    }
}

@Composable
fun PolicyScreen() {
    val scrollState = rememberScrollState()
    val isAtEnd by remember {
        derivedStateOf {
            // Check if the user has scrolled to the very bottom
            // If the content is not scrollable (maxValue is 0), it's considered "at end"
            scrollState.value >= scrollState.maxValue
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp, vertical = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Title(
                title = "Privacy & Policy",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                bottomPadding = 20.dp
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF1F1F1F), shape = RoundedCornerShape(10.dp))
                    .padding(15.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.privacy_policy_text),
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                )
            }

            button(
                label = "Agree & Continue",
                modifier = Modifier.padding(top = 20.dp),
                containerColor = if (isAtEnd) Color(0xFFD60C0C) else Color(0xFF1F1F1F),
                onClick = {
                    if (isAtEnd) {
                        // Handle navigation or agreement action here
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PolicyPreview() {
    XOXO_composeTheme {
        PolicyScreen()
    }
}
