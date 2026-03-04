package com.example.xoxo_compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xoxo_compose.ui.theme.AddImageScreen
import com.example.xoxo_compose.ui.theme.Title
import com.example.xoxo_compose.ui.theme.XOXO_composeTheme
import com.example.xoxo_compose.ui.theme.button

class Changeimg : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp, vertical = 60.dp)
        ) {
            // ปรับหัวข้อให้อยู่ตรงกลาง
            Title(
                title = "Change Picture",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // ปรับเป็น 2 คอลัมน์ (จะทำให้ได้ 3 แถวสำหรับ 6 รูป)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 20.dp)
            ) {
                // กล่องแรกเป็นรูป User โดยปรับขนาดและโครงสร้างให้เท่ากับ AddImageScreen
                item {
                    UserImageBox(
                        imageRes = R.drawable.user,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    )
                }
                // กล่องที่เหลือเรียกใช้จาก Addimg.kt
                items(5) {
                    AddImageScreen(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    )
                }
            }
            // เพิ่มปุ่ม Cancel และ Save ด้านล่าง
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
                    onClick = {
                        // TODO: บันทึกรูปภาพ
                        context.startActivity(Intent(context, Profile::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun UserImageBox(imageRes: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black)
                .border(
                    BorderStroke(2.dp, Color.White),
                    shape = RoundedCornerShape(10.dp)
                )
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "User Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Image(
                painter = painterResource(id = R.drawable.delete),
                contentDescription = "Delete",
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .clickable { /* TODO: ลบรูป */ }
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
