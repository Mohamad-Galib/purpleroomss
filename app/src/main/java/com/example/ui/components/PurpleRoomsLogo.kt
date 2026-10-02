package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.TextPrimary

@Composable
fun PurpleRoomsLogoIcon(
    size: Dp = 36.dp,
    tint: Color = PurplePrimary,
    windowColor: Color = Color.White
) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Outer hexagon/house shape resembling a stylized P
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.05f)
            lineTo(w * 0.88f, h * 0.28f)
            lineTo(w * 0.88f, h * 0.65f)
            cubicTo(w * 0.88f, h * 0.78f, w * 0.75f, h * 0.85f, w * 0.58f, h * 0.85f)
            lineTo(w * 0.38f, h * 0.85f)
            lineTo(w * 0.38f, h * 0.95f)
            lineTo(w * 0.18f, h * 0.95f)
            lineTo(w * 0.18f, h * 0.28f)
            close()
        }

        drawPath(
            path = path,
            color = tint
        )

        // 4 window panes in upper part of P
        val paneSize = w * 0.11f
        val gap = w * 0.04f
        val startX = w * 0.44f
        val startY = h * 0.35f

        // Top-left
        drawRoundRect(
            color = windowColor,
            topLeft = Offset(startX, startY),
            size = Size(paneSize, paneSize),
            cornerRadius = CornerRadius(2f, 2f)
        )
        // Top-right
        drawRoundRect(
            color = windowColor,
            topLeft = Offset(startX + paneSize + gap, startY),
            size = Size(paneSize, paneSize),
            cornerRadius = CornerRadius(2f, 2f)
        )
        // Bottom-left
        drawRoundRect(
            color = windowColor,
            topLeft = Offset(startX, startY + paneSize + gap),
            size = Size(paneSize, paneSize),
            cornerRadius = CornerRadius(2f, 2f)
        )
        // Bottom-right
        drawRoundRect(
            color = windowColor,
            topLeft = Offset(startX + paneSize + gap, startY + paneSize + gap),
            size = Size(paneSize, paneSize),
            cornerRadius = CornerRadius(2f, 2f)
        )
    }
}

@Composable
fun PurpleRoomsBrand(
    modifier: Modifier = Modifier,
    isLarge: Boolean = false,
    textColor: Color = TextPrimary,
    subtextColor: Color = PurplePrimary
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PurpleRoomsLogoIcon(size = if (isLarge) 54.dp else 34.dp)
        Spacer(modifier = Modifier.width(if (isLarge) 12.dp else 8.dp))
        Column {
            Text(
                text = "PURPLE ROOMS",
                fontSize = if (isLarge) 24.sp else 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "FIND. BOOK. MOVE IN. ONLINE. —",
                fontSize = if (isLarge) 10.sp else 7.5.sp,
                fontWeight = FontWeight.Bold,
                color = subtextColor,
                letterSpacing = 0.8.sp
            )
        }
    }
}
