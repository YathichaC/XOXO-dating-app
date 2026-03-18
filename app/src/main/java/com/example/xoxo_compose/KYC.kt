package com.example.xoxo_compose

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.xoxo_compose.ui.theme.XOXO_composeTheme
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class KYC : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XOXO_composeTheme {
                KYCScreen()
            }
        }
    }
}

// ── Verify state ──────────────────────────────────────────────────────────────
sealed class KYCVerifyState {
    object Idle : KYCVerifyState()
    object Loading : KYCVerifyState()
    data class Success(
        val idCardConfidence: Double,
        val selfieConfidence: Double,
        val totalConfidence: Double,
        val isSamePerson: Boolean
    ) : KYCVerifyState()
    data class Error(val message: String, val tips: List<String> = emptyList()) : KYCVerifyState()
}

enum class PickerTarget { NONE, ID_CARD, SELFIE }

// ── Re-compress image to JPEG at 90% quality, max 1600px wide ────────────────
// This fixes issues where camera saves huge files or gallery gives oddly small ones
private fun recompressImageFile(file: File) {
    try {
        val original = BitmapFactory.decodeFile(file.absolutePath) ?: return
        // Scale down if too large (keeps aspect ratio)
        val maxDim = 1600
        val scaled = if (original.width > maxDim || original.height > maxDim) {
            val ratio = minOf(maxDim.toFloat() / original.width, maxDim.toFloat() / original.height)
            Bitmap.createScaledBitmap(
                original,
                (original.width * ratio).toInt(),
                (original.height * ratio).toInt(),
                true
            )
        } else original

        FileOutputStream(file).use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        android.util.Log.d("KYC", "Recompressed ${file.name}: ${file.length()} bytes, ${scaled.width}x${scaled.height}px")

        if (scaled !== original) scaled.recycle()
        original.recycle()
    } catch (e: Exception) {
        android.util.Log.e("KYC", "Recompress failed: ${e.message}")
    }
}

// ── Copy gallery URI → local File ─────────────────────────────────────────────
private fun copyUriToFile(context: android.content.Context, uri: Uri, destFile: File) {
    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(destFile).use { output -> input.copyTo(output) }
    }
}


// ── Convert File → pure base64 string ────────────────────────────────────────
private fun fileToBase64(file: File): String {
    return Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
}

// ── iApp eKYC API call ────────────────────────────────────────────────────────
private suspend fun callIAppEKYC(idCardFile: File, selfieFile: File): KYCVerifyState {
    return withContext(Dispatchers.IO) {
        val ktorClient = HttpClient(CIO)
        try {
            if (!idCardFile.exists() || !selfieFile.exists()) {
                return@withContext KYCVerifyState.Error("ไม่พบไฟล์ภาพ กรุณาเพิ่มภาพใหม่")
            }

            // Re-compress before sending to ensure good quality & correct format
            recompressImageFile(idCardFile)
            recompressImageFile(selfieFile)

            android.util.Log.d("KYC", "Calling iApp eKYC API via Ktor...")
            android.util.Log.d("KYC", "file1 (ID card) size: ${idCardFile.length()} bytes")
            android.util.Log.d("KYC", "file0 (selfie) size:  ${selfieFile.length()} bytes")

            val httpResponse: HttpResponse = ktorClient.submitFormWithBinaryData(
                url = "https://api.iapp.co.th/v3/store/ekyc/face-and-id-card-verification",
                formData = formData {
                    append("file0", selfieFile.readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"${selfieFile.name}\"")
                    })
                    append("file1", idCardFile.readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"${idCardFile.name}\"")
                    })
                }
            ) {
                header("apikey", "iapp_live_f1b13c58c66812fb754129aba102a87b1cdbff137ee534e8f28b42c2395ce67f")
            }

            val rawBody    = httpResponse.bodyAsText()
            val statusCode = httpResponse.status.value
            android.util.Log.d("KYC", "iApp HTTP status: $statusCode")
            android.util.Log.d("KYC", "iApp raw response: $rawBody")

            if (!httpResponse.status.isSuccess()) {
                return@withContext when (statusCode) {
                    421 -> KYCVerifyState.Error(
                        message = "ไม่พบบัตรประชาชนหรือใบหน้าในภาพ",
                        tips = listOf(
                            "ตรวจสอบว่าภาพ file0 เป็นบัตรประชาชน ไม่ใช่เซลฟี่",
                            "ถ่ายภาพบัตรให้ชัด ไม่มีแสงสะท้อน และมองเห็นข้อความ",
                            "วางบัตรบนพื้นเรียบ ถ่ายตรงๆ ไม่เอียง",
                            "ถ่ายใหม่ในที่แสงสว่างเพียงพอ"
                        )
                    )
                    422 -> KYCVerifyState.Error(
                        message = "ไม่พบใบหน้าบนบัตรหรือในเซลฟี่",
                        tips = listOf(
                            "ภาพยื่น: ต้องเห็นใบหน้าคุณและบัตรประชาชนชัดเจน",
                            "ตรวจสอบว่าบัตรมีรูปถ่ายที่ชัดเจน",
                            "ถ่ายใหม่หันหน้าตรงกล้อง ไม่สวมแว่นดำ"
                        )
                    )
                    413 -> KYCVerifyState.Error("ไฟล์มีขนาดใหญ่เกินไป (เกิน 10 MB)")
                    415 -> KYCVerifyState.Error("รูปแบบไฟล์ไม่รองรับ")
                    420 -> KYCVerifyState.Error("ไม่พบพารามิเตอร์ที่ต้องการ")
                    461 -> KYCVerifyState.Error("ไม่ได้แนบไฟล์")
                    else -> KYCVerifyState.Error("เกิดข้อผิดพลาด ($statusCode)")
                }
            }

            val json   = JSONObject(rawBody)

            // Some error responses return 200 with a "message" field instead of idcard/selfie/total
            if (json.has("message") && !json.has("total")) {
                val apiMsg = json.optString("message", "")
                android.util.Log.e("KYC", "API returned error message: $apiMsg")
                return@withContext when {
                    apiMsg.contains("id card not found", ignoreCase = true) ->
                        KYCVerifyState.Error(
                            message = "ไม่พบบัตรประชาชนในภาพ (file0)",
                            tips = listOf(
                                "ภาพ Step 1 ต้องเป็นภาพบัตรประชาชนเท่านั้น ไม่ใช่เซลฟี่",
                                "วางบัตรให้เห็นทั้งใบ ถ่ายตรงๆ ไม่เอียง",
                                "หลีกเลี่ยงแสงสะท้อนบนบัตร",
                                "ถ่ายใหม่ในที่สว่าง หรือลองเลือกภาพจากคลังที่ชัดขึ้น"
                            )
                        )
                    apiMsg.contains("face not found", ignoreCase = true) ||
                            apiMsg.contains("selfie", ignoreCase = true) ->
                        KYCVerifyState.Error(
                            message = "ไม่พบใบหน้าในภาพ (file1)",
                            tips = listOf(
                                "ภาพ Step 2 ต้องเห็นใบหน้าคุณพร้อมถือบัตรประชาชน",
                                "หันหน้าตรงกล้อง อย่าสวมแว่นดำ",
                                "ถ่ายในที่แสงสว่าง"
                            )
                        )
                    else ->
                        KYCVerifyState.Error(
                            message = "API แจ้งข้อผิดพลาด: $apiMsg",
                            tips = listOf("ลองถ่ายภาพใหม่ในที่แสงสว่างเพียงพอ")
                        )
                }
            }

            val total  = json.getJSONObject("total")
            val idcard = json.getJSONObject("idcard")
            val selfie = json.getJSONObject("selfie")

            android.util.Log.d("KYC", "Parsed — total: ${total.getDouble("confidence")}, isSamePerson: ${total.getString("isSamePerson")}")

            KYCVerifyState.Success(
                idCardConfidence = idcard.getDouble("confidence"),
                selfieConfidence = selfie.getDouble("confidence"),
                totalConfidence  = total.getDouble("confidence"),
                isSamePerson     = total.getString("isSamePerson") == "true"
            )
        } catch (e: Exception) {
            android.util.Log.e("KYC", "iApp eKYC error: ${e.message}", e)
            KYCVerifyState.Error("เกิดข้อผิดพลาด: ${e.message}")
        } finally {
            ktorClient.close()
        }
    }
}

// ── Main Screen ───────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KYCScreen() {
    val context     = LocalContext.current
    val scope       = rememberCoroutineScope()

    var idCardUri   by remember { mutableStateOf<Uri?>(null) }
    var selfieUri   by remember { mutableStateOf<Uri?>(null) }
    var verifyState by remember { mutableStateOf<KYCVerifyState>(KYCVerifyState.Idle) }
    var pickerTarget by remember { mutableStateOf(PickerTarget.NONE) }
    val sheetState  = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val idCardTempUri = remember {
        FileProvider.getUriForFile(context, "${context.packageName}.provider",
            File(context.cacheDir, "id_card_photo.jpg"))
    }
    val selfieTempUri = remember {
        FileProvider.getUriForFile(context, "${context.packageName}.provider",
            File(context.cacheDir, "selfie_photo.jpg"))
    }

    val idCardCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) { idCardUri = idCardTempUri; verifyState = KYCVerifyState.Idle }
    }
    val selfieCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) { selfieUri = selfieTempUri; verifyState = KYCVerifyState.Idle }
    }
    val idCardGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            copyUriToFile(context, it, File(context.cacheDir, "id_card_photo.jpg"))
            idCardUri = it; verifyState = KYCVerifyState.Idle
        }
    }
    val selfieGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            copyUriToFile(context, it, File(context.cacheDir, "selfie_photo.jpg"))
            selfieUri = it; verifyState = KYCVerifyState.Idle
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    val bothCaptured = idCardUri != null && selfieUri != null
    val isLoading    = verifyState is KYCVerifyState.Loading

    // ── Bottom sheet ──────────────────────────────────────────────
    if (pickerTarget != PickerTarget.NONE) {
        ModalBottomSheet(
            onDismissRequest = { pickerTarget = PickerTarget.NONE },
            sheetState = sheetState,
            containerColor = Color(0xFF1A1A1A),
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 36.dp)
            ) {
                Text(
                    text = when (pickerTarget) {
                        PickerTarget.ID_CARD -> "🪪  เลือกภาพบัตรประชาชน"
                        PickerTarget.SELFIE  -> "🤳  เลือกภาพยื่นกับบัตร"
                        else -> "เลือกภาพ"
                    },
                    color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                PickerOption(
                    icon = "📷", label = "ถ่ายภาพด้วยกล้อง",
                    subtitle = "เปิดกล้องเพื่อถ่ายภาพทันที",
                    onClick = {
                        val target = pickerTarget
                        pickerTarget = PickerTarget.NONE
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                        when (target) {
                            PickerTarget.ID_CARD -> idCardCameraLauncher.launch(idCardTempUri)
                            PickerTarget.SELFIE  -> selfieCameraLauncher.launch(selfieTempUri)
                            else -> {}
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                PickerOption(
                    icon = "🖼️", label = "เลือกจากคลังภาพ",
                    subtitle = "เลือกภาพที่มีอยู่ในเครื่อง",
                    onClick = {
                        val target = pickerTarget
                        pickerTarget = PickerTarget.NONE
                        when (target) {
                            PickerTarget.ID_CARD -> idCardGalleryLauncher.launch("image/*")
                            PickerTarget.SELFIE  -> selfieGalleryLauncher.launch("image/*")
                            else -> {}
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { pickerTarget = PickerTarget.NONE }, modifier = Modifier.fillMaxWidth()) {
                    Text("ยกเลิก", color = Color(0xFF888888), fontSize = 14.sp)
                }
            }
        }
    }

    // ── Content ───────────────────────────────────────────────────
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0A0A0A)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.arrow),
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp)
                        .clickable { if (!isLoading) context.startActivity(Intent(context, Main::class.java)) }
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text("ยืนยันตัวตน (KYC)", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "อัปโหลดภาพบัตรประชาชนและภาพยื่นกับบัตร เพื่อยืนยันตัวตน",
                color = Color(0xFF9A9A9A), fontSize = 13.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            CameraUploadSection(
                stepNumber = "1", title = "บัตรประชาชน",
                subtitle = "ถ่ายภาพหรือเลือกภาพบัตรประชาชนด้านหน้า",
                icon = "🪪", capturedUri = idCardUri, enabled = !isLoading,
                onTap = { pickerTarget = PickerTarget.ID_CARD }
            )

            Spacer(modifier = Modifier.height(20.dp))

            CameraUploadSection(
                stepNumber = "2", title = "ภาพยื่นถ่ายกับบัตรประชาชน",
                subtitle = "ถ่ายภาพหรือเลือกภาพตนเองพร้อมถือบัตร",
                icon = "🤳", capturedUri = selfieUri, enabled = !isLoading,
                onTap = { pickerTarget = PickerTarget.SELFIE }
            )

            Spacer(modifier = Modifier.height(28.dp))

            when (val s = verifyState) {
                is KYCVerifyState.Success -> VerifySuccessCard(s)
                is KYCVerifyState.Error   -> VerifyErrorCard(s.message, s.tips)
                else                      -> GuidelinesBox()
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    val idFile     = File(context.cacheDir, "id_card_photo.jpg")
                    val selfieFile = File(context.cacheDir, "selfie_photo.jpg")
                    scope.launch {
                        verifyState = KYCVerifyState.Loading
                        val result = callIAppEKYC(idFile, selfieFile)
                        verifyState = result

                        if (result is KYCVerifyState.Success && result.totalConfidence >= 30.0) {
                            // Convert images to pure base64
                            val idCardBase64  = fileToBase64(idFile)
                            val selfieBase64  = fileToBase64(selfieFile)

                            android.util.Log.d("KYC", "Submitting KYC images to backend...")

                            // POST to /kyc/submit
                            com.example.xoxo_compose.network.ApiClient
                                .submitKYCImages(
                                    internationalCardImage = idCardBase64,
                                    personAndCardImage     = selfieBase64
                                )
                                .onSuccess { submitResponse ->
                                    android.util.Log.d("KYC", "KYC submit: ${submitResponse.status} — ${submitResponse.msg}")
                                }
                                .onFailure { err ->
                                    android.util.Log.e("KYC", "KYC submit failed: ${err.message}")
                                }

                            delay(2000)
                            context.startActivity(Intent(context, Main::class.java))
                            (context as? ComponentActivity)?.finish()
                        }
                    }
                },
                enabled = bothCaptured && !isLoading,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDD4F4F),
                    disabledContainerColor = Color(0xFF4A2020)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("กำลังตรวจสอบ...", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text(
                        text = when {
                            !bothCaptured                       -> "กรุณาเพิ่มภาพให้ครบก่อน"
                            verifyState is KYCVerifyState.Error -> "ลองใหม่อีกครั้ง"
                            else                                -> "ยืนยันตัวตน (Verify)"
                        },
                        color = if (bothCaptured) Color.White else Color(0xFF9A6A6A),
                        fontSize = 16.sp, fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "🔒  ข้อมูลของคุณถูกเข้ารหัสและปลอดภัย",
                color = Color(0xFF666666), fontSize = 12.sp,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

// ── Picker option row ─────────────────────────────────────────────────────────
@Composable
fun PickerOption(icon: String, label: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        color = Color(0xFF2A2A2A), shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(46.dp).background(Color(0xFF3A3A3A), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) { Text(icon, fontSize = 22.sp) }
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = Color(0xFF777777), fontSize = 12.sp)
            }
            Text("›", color = Color(0xFF555555), fontSize = 22.sp)
        }
    }
}

// ── Upload section ────────────────────────────────────────────────────────────
@Composable
fun CameraUploadSection(
    stepNumber: String, title: String, subtitle: String,
    icon: String, capturedUri: Uri?, enabled: Boolean = true,
    onTap: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
            Box(
                modifier = Modifier.size(28.dp).background(Color(0xFFDD4F4F), RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) { Text(stepNumber, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("$icon  $title", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color(0xFF888888), fontSize = 12.sp)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth().height(190.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF1A1A1A))
                .border(1.5.dp,
                    if (capturedUri != null) Color(0xFF4CAF50) else Color(0xFF333333),
                    RoundedCornerShape(14.dp))
                .clickable(enabled = enabled) { onTap() },
            contentAlignment = Alignment.Center
        ) {
            if (capturedUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(capturedUri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
                        .size(28.dp).background(Color(0xFF4CAF50), RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) { Text("✓", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                if (enabled) {
                    Box(
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                            .background(Color(0xCC000000)).padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("แตะเพื่อเปลี่ยนภาพ", color = Color(0xFFCCCCCC), fontSize = 12.sp) }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📷", fontSize = 28.sp)
                        Text("🖼️", fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("แตะเพื่อเพิ่มภาพ", color = Color(0xFF888888), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("ถ่ายภาพหรือเลือกจากคลัง", color = Color(0xFF555555), fontSize = 11.sp)
                }
            }
        }
    }
}

// ── Success card ──────────────────────────────────────────────────────────────
@Composable
fun VerifySuccessCard(state: KYCVerifyState.Success) {
    val passed = state.totalConfidence >= 30.0
    Surface(
        color = if (passed) Color(0xFF1A2E1A) else Color(0xFF2E1A1A),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (passed) "✅" else "❌",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (passed) "ยืนยันตัวตนสำเร็จ!" else "ยืนยันตัวตนไม่ผ่าน",
                color = if (passed) Color(0xFF76EE76) else Color(0xFFFF6B6B),
                fontSize = 20.sp, fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (passed)
                    "ตัวตนของคุณได้รับการยืนยันเรียบร้อยแล้วกำลังพาคุณไปยังหน้าหลัก..."
                else
                    "ไม่สามารถยืนยันตัวตนได้กรุณาถ่ายภาพใหม่ในที่แสงสว่างเพียงพอ",
            color = Color(0xFF999999), fontSize = 13.sp,
            textAlign = TextAlign.Center, lineHeight = 18.sp
            )
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
}


// ── Error card (with optional tips) ──────────────────────────────────────────
@Composable
fun VerifyErrorCard(message: String, tips: List<String> = emptyList()) {
    Surface(
        color = Color(0xFF2A1515), shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("⚠️", fontSize = 18.sp)
                Column {
                    Text("เกิดข้อผิดพลาด", color = Color(0xFFFF6B6B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(message, color = Color(0xFFCCCCCC), fontSize = 13.sp, lineHeight = 18.sp)
                }
            }
            if (tips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFF3A2020), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("💡  วิธีแก้ไข", color = Color(0xFFFFD700), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                tips.forEach { tip ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("•", color = Color(0xFFFFD700), fontSize = 12.sp)
                        Text(tip, color = Color(0xFFAAAAAA), fontSize = 12.sp, lineHeight = 16.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
}

// ── Guidelines ────────────────────────────────────────────────────────────────
@Composable
fun GuidelinesBox() {
    Surface(
        color = Color(0xFF141A20), shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("📋  คำแนะนำการอัปโหลดภาพ", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            GuidelineRow("✅", "บัตรต้องมองเห็นข้อความชัดเจน ไม่มีแสงสะท้อน")
            GuidelineRow("✅", "ถ่ายภาพในที่แสงสว่างเพียงพอ")
            GuidelineRow("✅", "ภาพยื่น: ถือบัตรให้เห็นทั้งหน้าคนและบัตร")
            GuidelineRow("❌", "ห้ามใช้ภาพที่ถ่ายจากหน้าจออื่น")
            GuidelineRow("❌", "ห้ามใช้บัตรหมดอายุ")
        }
    }
}

@Composable
fun GuidelineRow(icon: String, text: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
        Text(text = icon, fontSize = 12.sp, modifier = Modifier.width(22.dp))
        Text(text = text, color = Color(0xFFAAAAAA), fontSize = 12.sp, lineHeight = 16.sp, modifier = Modifier.weight(1f))
    }
}