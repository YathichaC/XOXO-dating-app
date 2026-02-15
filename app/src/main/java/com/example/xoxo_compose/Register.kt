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
import androidx.compose.material3.Scaffold
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

class Register : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XOXO_composeTheme {
                register()
            }
        }
    }
}

@Composable
fun register() {
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
                Title("Register")
                InputText(label = "Username")
                InputText(label = "Email")
                InputText(label = "Password")
                Text(
                    text = "Birth Date",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Text(
                    text = "Forgot Password?",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                button(label = "Register")
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
fun RegisterScreen() {
    XOXO_composeTheme {
        register()
    }
}