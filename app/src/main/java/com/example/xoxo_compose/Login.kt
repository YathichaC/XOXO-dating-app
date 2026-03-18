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
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
                    onValueChange = { password = it },
                    visualTransformation = PasswordVisualTransformation()
                )

                button(
                    label = if (isLoading) "Loading..." else "Login",
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
                                android.util.Log.d("Login", "Login API response status: ${response.status}")

                                if (response.status) {
                                    android.util.Log.d("Login", "✓ Login successful for: ${response.fullname}")

                                    val savedData = ApiClient.dumpAllPreferences()
                                    android.util.Log.d("Login", savedData)

                                    // ── Check KYC status ──────────────────────────────
                                    android.util.Log.d("Login", "Checking KYC status...")
                                    ApiClient.checkKYC()
                                        .onSuccess { kycResponse ->
                                            isLoading = false
                                            android.util.Log.d("Login", "KYC check - verified: ${kycResponse.verified}, msg: ${kycResponse.msg}")

                                            if (kycResponse.verified) {
                                                // KYC already verified → go to Main
                                                android.util.Log.d("Login", "KYC verified → navigating to Main")
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "ยินดีต้อนรับ ${response.fullname}",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                                context.startActivity(Intent(context, Main::class.java))
                                                (context as? ComponentActivity)?.finish()
                                            } else {
                                                // KYC not verified → go to KYC screen
                                                android.util.Log.d("Login", "KYC not verified → navigating to KYC")
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "กรุณายืนยันตัวตนก่อนใช้งาน",
                                                    android.widget.Toast.LENGTH_LONG
                                                ).show()
                                                context.startActivity(Intent(context, KYC::class.java))
                                                (context as? ComponentActivity)?.finish()
                                            }
                                        }
                                        .onFailure { kycError ->
                                            isLoading = false
                                            // KYC check failed (network error etc.) → still go to Main
                                            // to avoid blocking user on a KYC API issue
                                            android.util.Log.e("Login", "KYC check failed: ${kycError.message} → proceeding to Main")
                                            android.widget.Toast.makeText(
                                                context,
                                                "ยินดีต้อนรับ ${response.fullname}",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                            context.startActivity(Intent(context, Main::class.java))
                                            (context as? ComponentActivity)?.finish()
                                        }
                                    // ─────────────────────────────────────────────────

                                } else {
                                    isLoading = false
                                    android.util.Log.e("Login", "Login failed: ${response.msg}")
                                    android.widget.Toast.makeText(
                                        context,
                                        "Login failed: ${response.msg}",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            }.onFailure { error ->
                                isLoading = false
                                android.util.Log.e("Login", "Login error: ${error.message}")
                                android.widget.Toast.makeText(
                                    context,
                                    "Login error: ${error.message}",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }

                            android.util.Log.d("Login", "=== LOGIN CLICK END ===")
                        }
                    }
                )

                ActionText(
                    text = "Forgot Password?",
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(context, KYC::class.java))
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