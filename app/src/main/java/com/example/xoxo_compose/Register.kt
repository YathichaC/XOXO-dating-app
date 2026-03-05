package com.example.xoxo_compose

import android.content.Intent
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xoxo_compose.network.ApiClient
import com.example.xoxo_compose.ui.theme.ClickableActionText
import com.example.xoxo_compose.ui.theme.Dropdown
import com.example.xoxo_compose.ui.theme.InputText
import com.example.xoxo_compose.ui.theme.SubTitleText
import com.example.xoxo_compose.ui.theme.Title
import com.example.xoxo_compose.ui.theme.XOXO_composeTheme
import com.example.xoxo_compose.ui.theme.button
import kotlinx.coroutines.launch

class Register : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Initialize SharedPreferences
        ApiClient.initSharedPreferences(this)
        setContent {
            XOXO_composeTheme {
                register()
            }
        }
    }
}

@Composable
fun register() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val countries = stringArrayResource(R.array.country_list).toList()
    
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

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
                    label = "Full Name",
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
                    enabled = isFormValid && !isLoading,
                    onClick = {
                        scope.launch {
                            isLoading = true
                            val monthNumber = (listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec").indexOf(month) + 1)
                                .toString().padStart(2, '0')
                            
                            ApiClient.registerUser(
                                fullname = username,
                                email = email,
                                password = password,
                                day = day,
                                month = monthNumber,
                                year = year,
                                country = country
                            ).onSuccess { response ->
                                isLoading = false
                                android.util.Log.d("Register", "Registration successful: $response")
                                android.widget.Toast.makeText(
                                    context,
                                    "Registration successful! Please login.",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                context.startActivity(Intent(context, Login::class.java))
                                (context as? ComponentActivity)?.finish()
                            }.onFailure { error ->
                                isLoading = false
                                android.util.Log.e("Register", "Registration failed: ${error.message}")
                                android.widget.Toast.makeText(
                                    context,
                                    "Registration failed: ${error.message}",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                )
                
                ClickableActionText(
                    text1 = "Already have an account? ",
                    text2 = "Login",
                    onClick = {
                        context.startActivity(Intent(context, Login::class.java))
                    }
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
