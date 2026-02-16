package com.example.xoxo_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    val countries = stringArrayResource(R.array.country_list).toList()
    
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
                
                Column {
                    SubTitleText(
                        text = "Birth Date",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Dropdown(
                            hint = "Day",
                            items = (1..31).map { it.toString() },
                            modifier = Modifier.weight(1f)
                        )
                        Dropdown(
                            hint = "Month",
                            items = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"),
                            modifier = Modifier.weight(1.5f)
                        )
                        Dropdown(
                            hint = "Year",
                            items = (1900..2024).reversed().map { it.toString() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Column {
                    SubTitleText(
                        text = "Country",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Dropdown(
                        hint = "",
                        items = countries,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                button(label = "Register")
                
                ClickableActionText(
                    text1 = "Already have an account? ",
                    text2 = "Login",
                    onClick = { /* TODO: Navigate to Login */ }
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
