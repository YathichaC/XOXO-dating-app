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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xoxo_compose.ui.theme.ClickableActionText
import com.example.xoxo_compose.ui.theme.Dropdown
import com.example.xoxo_compose.ui.theme.InputText
import com.example.xoxo_compose.ui.theme.SubTitleText
import com.example.xoxo_compose.ui.theme.Title
import com.example.xoxo_compose.ui.theme.XOXO_composeTheme
import com.example.xoxo_compose.ui.theme.button

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
    
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }

    val isFormValid = username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() &&
            day.isNotEmpty() && month.isNotEmpty() && year.isNotEmpty() && country.isNotEmpty()
    
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

                InputText(
                    label = "Username",
                    value = username,
                    onValueChange = { username = it }
                )

                InputText(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it }
                )

                InputText(
                    label = "Password",
                    value = password,
                    onValueChange = { password = it }
                )
                
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
                            modifier = Modifier.weight(1f),
                            value = day,
                            onValueChange = { day = it }
                        )
                        Dropdown(
                            hint = "Month",
                            items = listOf(
                                "Jan",
                                "Feb",
                                "Mar",
                                "Apr",
                                "May",
                                "Jun",
                                "Jul",
                                "Aug",
                                "Sep",
                                "Oct",
                                "Nov",
                                "Dec"
                            ),
                            modifier = Modifier.weight(1.5f),
                            value = month,
                            onValueChange = { month = it }
                        )
                        Dropdown(
                            hint = "Year",
                            items = (1900..2024).reversed().map { it.toString() },
                            modifier = Modifier.weight(1f),
                            value = year,
                            onValueChange = { year = it }
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
                        modifier = Modifier.fillMaxWidth(),
                        value = country,
                        onValueChange = { country = it }
                    )
                }

                button(
                    label = "Register",
                    enabled = isFormValid,
                    onClick = { /* Action */ }
                )
                
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
