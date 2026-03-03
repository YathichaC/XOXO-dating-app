package com.example.xoxo_compose.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xoxo_compose.R

@Composable
fun MainActionButtons(
    onWrongClick: () -> Unit = {},
    onLikeClick: () -> Unit = {}
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.wrong),
            contentDescription = "Wrong",
            modifier = Modifier
                .size(64.dp)
                .clickable { onWrongClick() }
        )
        Image(
            painter = painterResource(id = R.drawable.like),
            contentDescription = "Like",
            modifier = Modifier
                .size(64.dp)
                .clickable { onLikeClick() }
        )
    }
}

@Preview(showBackground = false)
@Composable
fun MainActionButtonsPreview() {
    MainActionButtons()
}
