package com.example.xoxo_compose

import android.content.Intent
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xoxo_compose.network.ApiClient
import com.example.xoxo_compose.ui.theme.Title
import com.example.xoxo_compose.ui.theme.XOXO_composeTheme
import com.example.xoxo_compose.ui.theme.button
import kotlinx.coroutines.launch

class term_of_service : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XOXO_composeTheme {
                TermOfServiceScreen()
            }
        }
    }
}

@Composable
fun TermOfServiceScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val isAtEnd by remember {
        derivedStateOf {
            scrollState.value >= scrollState.maxValue
        }
    }

    // Get registration data from intent
    val fullname = (context as? ComponentActivity)?.intent?.getStringExtra("fullname") ?: ""
    val email = (context as? ComponentActivity)?.intent?.getStringExtra("email") ?: ""
    val password = (context as? ComponentActivity)?.intent?.getStringExtra("password") ?: ""
    val day = (context as? ComponentActivity)?.intent?.getStringExtra("day") ?: ""
    val month = (context as? ComponentActivity)?.intent?.getStringExtra("month") ?: ""
    val year = (context as? ComponentActivity)?.intent?.getStringExtra("year") ?: ""
    val country = (context as? ComponentActivity)?.intent?.getStringExtra("country") ?: ""
    var isLoading by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp, vertical = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Title(
                title = "Term of service",
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
                    text = stringResource(id = R.string.term_of_service_text),
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
                enabled = !isLoading,
                onClick = {
                    if (isAtEnd) {
                        scope.launch {
                            isLoading = true
                            ApiClient.registerUser(
                                fullname = fullname,
                                email = email,
                                password = password,
                                day = day,
                                month = month,
                                year = year,
                                country = country
                            ).onSuccess { response ->
                                isLoading = false
                                android.util.Log.d("TermOfService", "Registration successful: $response")
                                android.widget.Toast.makeText(
                                    context,
                                    "Registration successful! Please login.",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                context.startActivity(Intent(context, Login::class.java))
                                (context as? ComponentActivity)?.finish()
                            }.onFailure { error ->
                                isLoading = false
                                android.util.Log.e("TermOfService", "Registration failed: ${error.message}")
                                android.widget.Toast.makeText(
                                    context,
                                    "Registration failed: ${error.message}",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TermOfServicePreview() {
    XOXO_composeTheme {
        TermOfServiceScreen()
    }
}
