package com.example.xoxo_compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xoxo_compose.network.ApiClient
import com.example.xoxo_compose.ui.theme.*
import kotlinx.coroutines.launch

class Report : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userName = intent.getStringExtra("reported_user_name") ?: "Samantha"
        val userId = intent.getIntExtra("reported_user_id", 0)
        setContent {
            XOXO_composeTheme {
                ReportScreen(reportedName = userName, reportedUserId = userId)
            }
        }
    }
}

@Composable
fun ReportScreen(reportedName: String, reportedUserId: Int = 0) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedReason by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    
    val reasons = listOf("Harassment", "Inappropriate content", "Fake account", "Scam or Spam", "Other")
    val isFormValid = selectedReason.isNotEmpty() && details.isNotEmpty()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 30.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.arrow),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            context.startActivity(Intent(context, Main::class.java))
                        }
                )
                
                Text(
                    text = "Report",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Box(modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                SubTitleText(text = "Reported user")
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                        .background(Color(0xFF1F1F1F), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = reportedName,
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                SubTitleText(text = "Reason for report")
                Spacer(modifier = Modifier.height(8.dp))
                Dropdown(
                    hint = "Select a reason",
                    items = reasons,
                    value = selectedReason,
                    onValueChange = { selectedReason = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            InputText(
                label = "Details",
                value = details,
                onValueChange = { details = it },
                modifier = Modifier.fillMaxWidth(),
                height = 150.dp,
                singleLine = false
            )

            Spacer(modifier = Modifier.weight(1f))

            button(
                label = "Confirm",
                enabled = isFormValid && !isSubmitting,
                onClick = {
                    isSubmitting = true
                    scope.launch {
                        // Submit report to API
                        val result = ApiClient.submitReport(
                            targetUserId = reportedUserId,
                            reason = selectedReason,
                            detail = details
                        )
                        
                        result.onSuccess { response ->
                            android.util.Log.d("Report", "✓ Report submitted: ${response.msg}")
                            android.widget.Toast.makeText(context, response.msg, android.widget.Toast.LENGTH_SHORT).show()
                            context.startActivity(Intent(context, Main::class.java))
                        }.onFailure { error ->
                            android.util.Log.e("Report", "✗ Report failed: ${error.message}")
                            android.widget.Toast.makeText(context, "Error: ${error.message}", android.widget.Toast.LENGTH_SHORT).show()
                            isSubmitting = false
                        }
                    }
                },
                modifier = Modifier.padding(bottom = 30.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReportScreenPreview() {
    XOXO_composeTheme {
        ReportScreen(reportedName = "Samantha")
    }
}
