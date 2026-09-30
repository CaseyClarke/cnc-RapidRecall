package com.example.cnc_rapidrecall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ScreenColumn(
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        horizontalAlignment = horizontalAlignment,
        modifier = Modifier
            .background(backgroundColor)
            .safeDrawingPadding()
            .fillMaxSize()
            .clip(RoundedCornerShape(48.dp))
            .then(modifier),
        content = content
    )
}
