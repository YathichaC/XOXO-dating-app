package com.example.xoxo_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xoxo_compose.ui.theme.XOXO_composeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XOXO_composeTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Title("Login")
                
                InputText(label = "Email")
                
                InputText(label = "Password")
                
                button(label = "Login")
                
                Text(
                    text = "Forgot Password?",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Text(
                    text = "Don't have an account",
                    color = Color(0xFFD60C0C),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    XOXO_composeTheme {
        MainScreen()
    }
}
