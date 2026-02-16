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
import com.example.xoxo_compose.ui.theme.ClickableActionText
import com.example.xoxo_compose.ui.theme.InputText
import com.example.xoxo_compose.ui.theme.NormalText
import com.example.xoxo_compose.ui.theme.Title
import com.example.xoxo_compose.ui.theme.XOXO_composeTheme
import com.example.xoxo_compose.ui.theme.button

class Reset_password : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XOXO_composeTheme {
                ResetPasswordScreen()
            }
        }
    }
}

@Composable
fun ResetPasswordScreen() {
    var password by remember { mutableStateOf("") }
    val isFormValid = password.isNotEmpty()

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
                Title(
                    title = "Reset Password",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    bottomPadding = 5.dp
                )

                NormalText(
                    text = "Enter a new password to continue",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    textAlign = TextAlign.Center
                )

                InputText(
                    label = "Password",
                    value = password,
                    onValueChange = { password = it }
                )

                button(
                    label = "Submit",
                    enabled = isFormValid,
                    onClick = { /* TODO */ }
                )
                
                ClickableActionText(
                    text1 = "Go back to ",
                    text2 = "Login",
                    onClick = { /* TODO: Navigate to Login */ }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResetPasswordPreview() {
    XOXO_composeTheme {
        ResetPasswordScreen()
    }
}
