package com.example.xoxo_compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xoxo_compose.network.ApiClient
import com.example.xoxo_compose.ui.theme.ActionText
import com.example.xoxo_compose.ui.theme.ClickableActionText
import com.example.xoxo_compose.ui.theme.InputText
import com.example.xoxo_compose.ui.theme.Title
import com.example.xoxo_compose.ui.theme.XOXO_composeTheme
import com.example.xoxo_compose.ui.theme.button
import kotlinx.coroutines.launch

class Login : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Initialize SharedPreferences
        ApiClient.initSharedPreferences(this)
        setContent {
            XOXO_composeTheme {
                LoginScreen()
            }
        }
    }
}

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val isFormValid = email.isNotEmpty() && password.isNotEmpty()

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

                button(
                    label = "Login",
                    enabled = isFormValid && !isLoading,
                    onClick = {
                        scope.launch {
                            isLoading = true
                            android.util.Log.d("Login", "=== LOGIN CLICK START ===")
                            android.util.Log.d("Login", "Email: $email")
                            ApiClient.loginUser(
                                email = email,
                                password = password
                            ).onSuccess { response ->
                                isLoading = false
                                android.util.Log.d("Login", "Login API response status: ${response.status}")
                                if (response.status) {
                                    android.util.Log.d("Login", "✓ Login successful for: ${response.fullname}")
                                    
                                    // Dump all saved data
                                    val savedData = ApiClient.dumpAllPreferences()
                                    android.util.Log.d("Login", savedData)
                                    
                                    android.util.Log.d("Login", "=== LOGIN CLICK END ===")
                                    
                                    // Show toast with saved user info
                                    android.widget.Toast.makeText(
                                        context,
                                        "Saved: ${response.fullname} (${response.email})",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                    
                                    // Navigate to Main screen
                                    context.startActivity(Intent(context, Main::class.java))
                                    (context as? ComponentActivity)?.finish()
                                } else {
                                    android.util.Log.e("Login", "Login failed: ${response.msg}")
                                }
                            }.onFailure { error ->
                                isLoading = false
                                android.util.Log.e("Login", "Login failed: ${error.message}")
                            }
                        }
                    }
                )

                ActionText(
                    text = "Forgot Password?",
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(context, forgot_password::class.java))
                    }
                )

                ClickableActionText(
                    text1 = "Don't have an account? ",
                    text2 = "Register",
                    onClick = {
                        context.startActivity(Intent(context, Register::class.java))
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    XOXO_composeTheme {
        LoginScreen()
    }
}
