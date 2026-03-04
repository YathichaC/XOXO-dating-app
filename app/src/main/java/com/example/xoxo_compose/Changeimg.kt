package com.example.xoxo_compose

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.graphics.BitmapFactory
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xoxo_compose.ui.theme.Title
import com.example.xoxo_compose.ui.theme.XOXO_composeTheme
import com.example.xoxo_compose.ui.theme.button
import coil.compose.AsyncImage
import com.example.xoxo_compose.network.ApiClient
import kotlinx.coroutines.launch

class Changeimg : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ApiClient.initSharedPreferences(this)
        setContent {
            XOXO_composeTheme {
                ChangeImageScreen()
            }
        }
    }
}

@Composable
fun ChangeImageScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State for 6 lifestyle images (can be filename from DB or base64 from user selection)
    var image1Base64 by remember { mutableStateOf("") }
    var image2Base64 by remember { mutableStateOf("") }
    var image3Base64 by remember { mutableStateOf("") }
    var image4Base64 by remember { mutableStateOf("") }
    var image5Base64 by remember { mutableStateOf("") }
    var image6Base64 by remember { mutableStateOf("") }
    
    // Track which images are newly selected (user picked) vs loaded from DB
    // If image is selected by user, it will contain "data:image/jpeg;base64,"
    // If image is from DB, it will be just a filename or remain empty
    var image1Modified by remember { mutableStateOf(false) }
    var image2Modified by remember { mutableStateOf(false) }
    var image3Modified by remember { mutableStateOf(false) }
    var image4Modified by remember { mutableStateOf(false) }
    var image5Modified by remember { mutableStateOf(false) }
    var image6Modified by remember { mutableStateOf(false) }
    
    var isUploading by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    // Load profile images when screen loads
    LaunchedEffect(Unit) {
        scope.launch {
            ApiClient.getProfile()
                .onSuccess { response ->
                    if (response.status && response.user != null) {
                        image1Base64 = response.image1
                        image2Base64 = response.image2
                        image3Base64 = response.image3
                        image4Base64 = response.image4
                        image5Base64 = response.image5
                        image6Base64 = response.image6
                        android.util.Log.d("Changeimg", "Loaded images - Image1: ${response.image1.take(50)}, Image2: ${response.image2.take(50)}, Image3: ${response.image3.take(50)}, Image4: ${response.image4.take(50)}, Image5: ${response.image5.take(50)}, Image6: ${response.image6.take(50)}")
                        isLoading = false
                    } else {
                        errorMessage = response.msg ?: "Failed to load images"
                        isLoading = false
                    }
                }
                .onFailure { error ->
                    isLoading = false
                    errorMessage = error.message ?: "Unknown error occurred"
                    android.util.Log.e("Changeimg", "Failed to load profile: ${error.message}")
                }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        if (isLoading) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else if (errorMessage.isNotEmpty()) {
            // Error state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(30.dp)
                ) {
                    Text(
                        text = "Error Loading Images",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    button(
                        label = "Back",
                        onClick = {
                            context.startActivity(Intent(context, Profile::class.java))
                        }
                    )
                }
            }
        } else {
            ChangeImageContent(
                image1Base64 = image1Base64,
                image2Base64 = image2Base64,
                image3Base64 = image3Base64,
                image4Base64 = image4Base64,
                image5Base64 = image5Base64,
                image6Base64 = image6Base64,
                image1Modified = image1Modified,
                image2Modified = image2Modified,
                image3Modified = image3Modified,
                image4Modified = image4Modified,
                image5Modified = image5Modified,
                image6Modified = image6Modified,
                onImage1Change = { newImage ->
                    image1Base64 = newImage
                    image1Modified = true
                },
                onImage2Change = { newImage ->
                    image2Base64 = newImage
                    image2Modified = true
                },
                onImage3Change = { newImage ->
                    image3Base64 = newImage
                    image3Modified = true
                },
                onImage4Change = { newImage ->
                    image4Base64 = newImage
                    image4Modified = true
                },
                onImage5Change = { newImage ->
                    image5Base64 = newImage
                    image5Modified = true
                },
                onImage6Change = { newImage ->
                    image6Base64 = newImage
                    image6Modified = true
                },
                isUploading = isUploading,
                onUploadStart = { isUploading = true },
                onUploadEnd = { isUploading = false },
                scope = scope,
                context = context
            )
        }
    }
}

@Composable
fun ChangeImageContent(
    image1Base64: String,
    image2Base64: String,
    image3Base64: String,
    image4Base64: String,
    image5Base64: String,
    image6Base64: String,
    image1Modified: Boolean,
    image2Modified: Boolean,
    image3Modified: Boolean,
    image4Modified: Boolean,
    image5Modified: Boolean,
    image6Modified: Boolean,
    onImage1Change: (String) -> Unit,
    onImage2Change: (String) -> Unit,
    onImage3Change: (String) -> Unit,
    onImage4Change: (String) -> Unit,
    onImage5Change: (String) -> Unit,
    onImage6Change: (String) -> Unit,
    isUploading: Boolean,
    onUploadStart: () -> Unit,
    onUploadEnd: () -> Unit,
    scope: kotlinx.coroutines.CoroutineScope,
    context: android.content.Context
) {
    val imagePickerLauncher1 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                
                if (bytes != null) {
                    // Store with data URL prefix so we can distinguish from DB filenames
                    onImage1Change("data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.DEFAULT))
                    Toast.makeText(context, "Image 1 selected", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val imagePickerLauncher2 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                
                if (bytes != null) {
                    onImage2Change("data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.DEFAULT))
                    Toast.makeText(context, "Image 2 selected", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val imagePickerLauncher3 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                
                if (bytes != null) {
                    onImage3Change("data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.DEFAULT))
                    Toast.makeText(context, "Image 3 selected", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val imagePickerLauncher4 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                
                if (bytes != null) {
                    onImage4Change("data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.DEFAULT))
                    Toast.makeText(context, "Image 4 selected", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val imagePickerLauncher5 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                
                if (bytes != null) {
                    onImage5Change("data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.DEFAULT))
                    Toast.makeText(context, "Image 5 selected", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val imagePickerLauncher6 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                
                if (bytes != null) {
                    onImage6Change("data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.DEFAULT))
                    Toast.makeText(context, "Image 6 selected", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp, vertical = 60.dp)
    ) {
        Title(
            title = "Change Picture",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            items(6) { index ->
                val base64Image = when (index) {
                    0 -> image1Base64
                    1 -> image2Base64
                    2 -> image3Base64
                    3 -> image4Base64
                    4 -> image5Base64
                    5 -> image6Base64
                    else -> ""
                }
                
                val pickImage = when (index) {
                    0 -> { { imagePickerLauncher1.launch("image/*") } }
                    1 -> { { imagePickerLauncher2.launch("image/*") } }
                    2 -> { { imagePickerLauncher3.launch("image/*") } }
                    3 -> { { imagePickerLauncher4.launch("image/*") } }
                    4 -> { { imagePickerLauncher5.launch("image/*") } }
                    5 -> { { imagePickerLauncher6.launch("image/*") } }
                    else -> { { } }
                }
                
                ImageBox(
                    base64Image = base64Image,
                    onImageClick = pickImage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            button(
                label = "Cancel",
                containerColor = Color.White,
                contentColor = Color.Black,
                modifier = Modifier.weight(1f),
                onClick = {
                    context.startActivity(Intent(context, Profile::class.java))
                }
            )
            button(
                label = "Save",
                containerColor = Color(0xFFD60C0C),
                contentColor = Color.White,
                modifier = Modifier.weight(1f),
                enabled = (!isUploading),
                onClick = {
                    scope.launch {
                        onUploadStart()
                        
                        // Only send images that were modified by user (contain base64 data)
                        // For unmodified images (filenames from DB), send null so backend keeps them
                        val formattedImage1 = if (image1Modified && image1Base64.startsWith("data:")) image1Base64 else null
                        val formattedImage2 = if (image2Modified && image2Base64.startsWith("data:")) image2Base64 else null
                        val formattedImage3 = if (image3Modified && image3Base64.startsWith("data:")) image3Base64 else null
                        val formattedImage4 = if (image4Modified && image4Base64.startsWith("data:")) image4Base64 else null
                        val formattedImage5 = if (image5Modified && image5Base64.startsWith("data:")) image5Base64 else null
                        val formattedImage6 = if (image6Modified && image6Base64.startsWith("data:")) image6Base64 else null
                        
                        android.util.Log.d("Changeimg", "Sending to backend - Image1 Modified: $image1Modified, HasBase64: ${image1Base64.startsWith("data:")}, Sending: ${formattedImage1 != null}")
                        
                        ApiClient.uploadLifestyleImages(
                            image1 = formattedImage1,
                            image2 = formattedImage2,
                            image3 = formattedImage3,
                            image4 = formattedImage4,
                            image5 = formattedImage5,
                            image6 = formattedImage6
                        ).onSuccess { response ->
                            onUploadEnd()
                            if (response.status) {
                                // Save lifestyle images to SharedPreferences
                                ApiClient.saveLifestyleImages(response)
                                Toast.makeText(context, "Lifestyle images updated successfully", Toast.LENGTH_SHORT).show()
                                android.util.Log.d("Changeimg", "Upload successful: ${response.msg}")
                                
                                // Reload profile to get latest data
                                ApiClient.getProfile()
                                    .onSuccess { profileResponse ->
                                        android.util.Log.d("Changeimg", "Profile reloaded after upload")
                                        context.startActivity(Intent(context, Profile::class.java))
                                        (context as? ComponentActivity)?.finish()
                                    }
                                    .onFailure { error ->
                                        android.util.Log.e("Changeimg", "Failed to reload profile: ${error.message}")
                                        context.startActivity(Intent(context, Profile::class.java))
                                        (context as? ComponentActivity)?.finish()
                                    }
                            } else {
                                Toast.makeText(context, "Upload failed: ${response.msg}", Toast.LENGTH_SHORT).show()
                                android.util.Log.e("Changeimg", "Upload failed: ${response.msg}")
                            }
                        }.onFailure { error ->
                            onUploadEnd()
                            val errorMsg = error.message ?: "Unknown error occurred"
                            Toast.makeText(context, "Error: $errorMsg", Toast.LENGTH_SHORT).show()
                            android.util.Log.e("Changeimg", "Upload error: $errorMsg")
                        }
                    }
                }
            )
        }
        
        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

@Composable
fun ImageBox(
    base64Image: String,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    android.util.Log.d("ImageBox", "Displaying image box - Image empty: ${base64Image.isEmpty()}, Image length: ${base64Image.length}")
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF2A2A2A))
            .border(
                BorderStroke(2.dp, Color.White),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onImageClick() },
        contentAlignment = Alignment.Center
    ) {
        if (base64Image.isNotEmpty()) {
            // Check if it's a base64 string (starts with data:) or a filename/URL
            if (base64Image.startsWith("data:") || (base64Image.length > 100 && !base64Image.contains("."))) {
                // It's base64 - decode and display
                val decodedBytes = try {
                    Base64.decode(base64Image.substringAfter("base64,"), Base64.DEFAULT)
                } catch (e: Exception) {
                    null
                }
                
                val bitmap = decodedBytes?.let {
                    BitmapFactory.decodeByteArray(it, 0, it.size)
                }
                
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        "Tap to add image",
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // It's a URL/filename - load via Coil
                val imageUrl = "${ApiClient.API_BASE_URL}/images/$base64Image"
                android.util.Log.d("ImageBox", "Loading image from URL: $imageUrl")
                
                var loadingState by remember { mutableStateOf(true) }
                var errorState by remember { mutableStateOf(false) }
                
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Loaded Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onLoading = {
                        loadingState = true
                        errorState = false
                        android.util.Log.d("ImageBox", "Loading image: $imageUrl")
                    },
                    onSuccess = {
                        loadingState = false
                        errorState = false
                        android.util.Log.d("ImageBox", "Successfully loaded image: $imageUrl")
                    },
                    onError = {
                        loadingState = false
                        errorState = true
                        android.util.Log.e("ImageBox", "Failed to load image: $imageUrl - ${it.result.throwable.message}")
                    }
                )
                
                // Show loading indicator
                if (loadingState) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // Show placeholder on error
                if (errorState) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                "Image failed",
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp
                            )
                            Text(
                                "Tap to replace",
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        } else {
            Text(
                "Tap to add image",
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChangeImageScreenPreview() {
    XOXO_composeTheme {
        ChangeImageScreen()
    }
}
