package com.example.xoxo_compose.ui.theme

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Title(
    title: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    bottomPadding: Dp = 30.dp
) {
    Text(
        text = title,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = textAlign,
        modifier = modifier.padding(bottom = bottomPadding)
    )
}

@Composable
fun SubTitleText(text: String, modifier: Modifier = Modifier, textAlign: TextAlign? = null) {
    Text(
        text = text,
        fontSize = 16.sp,
        color = Color.White,
        textAlign = textAlign,
        modifier = modifier
    )
}

@Composable
fun NormalText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = color,
        textAlign = textAlign,
        modifier = modifier
    )
}

@Composable
fun ColumnScope.ActionText(text: String, color: Color = Color.White, fontWeight: FontWeight? = null) {
    Text(
        text = text,
        color = color,
        fontSize = 14.sp,
        fontWeight = fontWeight,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )
}

@Composable
fun ColumnScope.ClickableActionText(
    text1: String,
    text2: String,
    color1: Color = Color.White,
    color2: Color = Color(0xFFD60C0C),
    fontWeight: FontWeight? = FontWeight.Bold,
    onClick: () -> Unit
) {
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = color1, fontSize = 14.sp)) {
            append(text1)
        }
        withStyle(style = SpanStyle(color = color2, fontSize = 14.sp, fontWeight = fontWeight)) {
            pushStringAnnotation(tag = text2, annotation = text2)
            append(text2)
            pop()
        }
    }

    ClickableText(
        text = annotatedString,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = text2, start = offset, end = offset)
                .firstOrNull()?.let { 
                    onClick()
                }
        },
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputText(
    label: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    onValueChange: ((String) -> Unit)? = null
) {
    var internalTextValue by remember { mutableStateOf("") }
    val displayValue = value ?: internalTextValue
    val interactionSource = remember { MutableInteractionSource() }

    Column(modifier = modifier) {
        SubTitleText(
            text = label,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1F1F1F),
            unfocusedContainerColor = Color(0xFF1F1F1F),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        )

        BasicTextField(
            value = displayValue,
            onValueChange = { 
                if (onValueChange != null) {
                    onValueChange(it)
                } else {
                    internalTextValue = it
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 16.sp
            ),
            interactionSource = interactionSource,
            cursorBrush = SolidColor(Color.White),
            decorationBox = { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = displayValue,
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    colors = colors,
                    container = {
                        TextFieldDefaults.Container(
                            enabled = true,
                            isError = false,
                            interactionSource = interactionSource,
                            colors = colors,
                            shape = TextFieldDefaults.shape,
                        )
                    }
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dropdown(
    hint: String,
    items: List<String>,
    modifier: Modifier = Modifier,
    value: String? = null,
    onValueChange: ((String) -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var internalSelectedText by remember { mutableStateOf("") }
    val displayValue = value ?: internalSelectedText
    val interactionSource = remember { MutableInteractionSource() }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        val colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1F1F1F),
            unfocusedContainerColor = Color(0xFF1F1F1F),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTrailingIconColor = Color.White,
            unfocusedTrailingIconColor = Color.White
        )

        BasicTextField(
            value = displayValue,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true)
                .fillMaxWidth()
                .height(40.dp),
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 16.sp
            ),
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = displayValue,
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    placeholder = {
                        Text(
                            text = hint,
                            color = Color(0xFF464646),
                            fontSize = 12.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Visible
                        )
                    },
                    trailingIcon = {
                        CompositionLocalProvider(LocalContentColor provides Color.White) {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        }
                    },
                    colors = colors,
                    container = {
                        TextFieldDefaults.Container(
                            enabled = true,
                            isError = false,
                            interactionSource = interactionSource,
                            colors = colors,
                            shape = TextFieldDefaults.shape,
                        )
                    }
                )
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item) },
                    onClick = {
                        if (onValueChange != null) {
                            onValueChange(item)
                        } else {
                            internalSelectedText = item
                        }
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
fun button(
    label: String,
    modifier: Modifier = Modifier
        .padding(top = 25.dp, bottom = 15.dp),
    containerColor: Color = Color(0xFFD60C0C),
    contentColor: Color = Color.White,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(10.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = Color(0xFF1F1F1F),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}
