package com.codershubinc.abt.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DotGridBackground(
    modifier: Modifier = Modifier,
    dotColor: Color = Color.White.copy(alpha = 0.04f)
) {
    Canvas(modifier = modifier) {
        val step = 32.dp.toPx()
        val radius = 1.dp.toPx()

        var x = 0f
        while (x < size.width) {
            var y = 0f
            while (y < size.height) {
                drawCircle(color = dotColor, radius = radius, center = Offset(x, y))
                y += step
            }
            x += step
        }
    }
}
