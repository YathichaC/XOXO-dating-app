package com.example.xoxo_compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Title(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = modifier.padding(bottom = 30.dp)
    )
}

@Composable
fun NormalText(text: String, modifier: Modifier = Modifier, color: Color = Color.White) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = color,
        modifier = modifier
    )
}

@Composable
fun InputText(label: String, modifier: Modifier = Modifier) {
    var textValue by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        TextField(
            value = textValue,
            onValueChange = { textValue = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1F1F1F),
                unfocusedContainerColor = Color(0xFF1F1F1F),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
fun button(
    label: String,
    modifier: Modifier = Modifier
        .padding(top = 25.dp),
    containerColor: Color = Color(0xFFD60C0C),
    contentColor: Color = Color.White
) {
    Button(
        onClick = { /* TODO */ },
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}
