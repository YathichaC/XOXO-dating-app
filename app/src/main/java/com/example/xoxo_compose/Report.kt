package com.example.xoxo_compose

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.xoxo_compose.network.ApiClient
import com.example.xoxo_compose.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Report : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userName = intent.getStringExtra("reported_user_name") ?: "Samantha"
        val userId = intent.getIntExtra("reported_user_id", 0)
        val fromChat = intent.getBooleanExtra("from_chat", false)
        setContent {
            XOXO_composeTheme {
                ReportScreen(reportedName = userName, reportedUserId = userId, fromChat = fromChat)
            }
        }
    }
}

@Composable
fun ReportScreen(reportedName: String, reportedUserId: Int = 0, fromChat: Boolean = false) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var selectedReason by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    // Evidence image URIs
    var evidenceUri1 by remember { mutableStateOf<Uri?>(null) }
    var evidenceUri2 by remember { mutableStateOf<Uri?>(null) }
    var evidenceUri3 by remember { mutableStateOf<Uri?>(null) }

    // Gallery launchers — one per slot
    val galleryLauncher1 = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { evidenceUri1 = it }
    }
    val galleryLauncher2 = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { evidenceUri2 = it }
    }
    val galleryLauncher3 = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { evidenceUri3 = it }
    }
    
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
                .verticalScroll(scrollState)
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
                            if (fromChat) {
                                context.startActivity(Intent(context, Chatlist::class.java))
                            } else {
                                context.startActivity(Intent(context, Main::class.java))
                            }
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
                height = 120.dp,
                singleLine = false
            )

            Spacer(modifier = Modifier.height(25.dp))

            // Evidence Section
            Column(modifier = Modifier.fillMaxWidth()) {
                SubTitleText(text = "Evidence")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EvidenceSlot(
                        uri = evidenceUri1,
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        onClick = { galleryLauncher1.launch("image/*") }
                    )
                    EvidenceSlot(
                        uri = evidenceUri2,
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        onClick = { galleryLauncher2.launch("image/*") }
                    )
                    EvidenceSlot(
                        uri = evidenceUri3,
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        onClick = { galleryLauncher3.launch("image/*") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            button(
                label = if (isSubmitting) "Submitting..." else "Confirm",
                enabled = isFormValid && !isSubmitting,
                onClick = {
                    isSubmitting = true
                    scope.launch {
                        // Convert evidence URIs to base64 (null if not selected)
                        val b64Image1 = evidenceUri1?.let { uriToBase64(context, it) }
                        val b64Image2 = evidenceUri2?.let { uriToBase64(context, it) }
                        val b64Image3 = evidenceUri3?.let { uriToBase64(context, it) }

                        // Submit report to API
                        val result = ApiClient.submitReport(
                            targetUserId = reportedUserId,
                            reason = selectedReason,
                            detail = details,
                            image1 = b64Image1,
                            image2 = b64Image2,
                            image3 = b64Image3
                        )

                        result.onSuccess { response ->
                            android.util.Log.d("Report", "✓ Report submitted: ${response.msg}")
                            android.widget.Toast.makeText(context, response.msg, android.widget.Toast.LENGTH_SHORT).show()
                            if (fromChat) {
                                context.startActivity(Intent(context, Chatlist::class.java))
                            } else {
                                context.startActivity(Intent(context, Main::class.java))
                            }
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

// ── URI → pure base64 string (runs on IO thread) ─────────────────────────
private suspend fun uriToBase64(context: android.content.Context, uri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bytes = stream.readBytes()
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            android.util.Log.e("Report", "Failed to convert URI to base64: ${e.message}")
            null
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

// ── Evidence image slot ───────────────────────────────────────────────────────
@Composable
fun EvidenceSlot(
    uri: Uri?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1F1F1F))
            .border(
                BorderStroke(
                    width = 1.5.dp,
                    color = if (uri != null) Color(0xFF4CAF50) else Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (uri != null) {
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Evidence image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
            // Small green checkmark badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(18.dp)
                    .background(Color(0xFF4CAF50), RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Image(
                painter = painterResource(id = R.drawable.add),
                contentDescription = "Add image",
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
