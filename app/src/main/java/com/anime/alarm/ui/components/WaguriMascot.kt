package com.anime.alarm.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.anime.alarm.data.model.CharacterAssets

// Warna terinspirasi Waguri (Cream/Blonde hair, soft vibes)
val WaguriHair = Color(0xFFFBE7B2) 
val SkinTone = Color(0xFFFFE0BD)
val BlushColor = Color(0xFFFFB7C5)

@Composable
fun WaguriMascot(
    modifier: Modifier = Modifier,
    emotion: MascotEmotion = MascotEmotion.HAPPY,
    assets: CharacterAssets? = null
) {
    if (assets != null) {
        val imageRes = when (emotion) {
            MascotEmotion.SLEEPY -> assets.sleepyImage
            MascotEmotion.HAPPY -> assets.happyImage
            MascotEmotion.ANGRY -> assets.normalImage // Fallback
        }
        
        // Cek jika resource valid (bukan 0/placeholder jika kita pakai int 0)
        // Di sini kita asumsikan CharacterRepository memberi ID drawable yang valid.
        // Jika placeholder adalah ic_launcher, kita tampilkan itu dulu.
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Mascot",
            modifier = modifier
        )
    } else {
        // Fallback: Code-drawn Chibi Face (Default Waguri)
        WaguriCanvas(modifier, emotion)
    }
}

@Composable
fun WaguriCanvas(
    modifier: Modifier = Modifier,
    emotion: MascotEmotion
) {
    Canvas(modifier = modifier.size(120.dp)) {
        val w = size.width
        val h = size.height
        
        // 1. Face Shape
        drawCircle(
            color = SkinTone,
            radius = w / 2.2f,
            center = center
        )

        // 2. Hair (Bangs - Simplified)
        drawArc(
            color = WaguriHair,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(x = w * 0.05f, y = h * 0.05f),
            size = Size(w * 0.9f, h * 0.8f),
            style = Stroke(width = w * 0.1f, cap = StrokeCap.Round)
        )

        // 3. Eyes
        val eyeColor = Color(0xFF5D4037) // Brown eyes
        val eyeY = h * 0.55f
        
        if (emotion == MascotEmotion.SLEEPY) {
            // Closed eyes (lines)
            drawLine(
                color = eyeColor,
                start = Offset(w * 0.3f, eyeY),
                end = Offset(w * 0.4f, eyeY),
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = eyeColor,
                start = Offset(w * 0.6f, eyeY),
                end = Offset(w * 0.7f, eyeY),
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )
        } else {
            // Open eyes (circles)
            drawCircle(color = eyeColor, radius = w * 0.06f, center = Offset(w * 0.35f, eyeY))
            drawCircle(color = eyeColor, radius = w * 0.06f, center = Offset(w * 0.65f, eyeY))
        }

        // 4. Blush
        drawCircle(color = BlushColor.copy(alpha = 0.6f), radius = w * 0.08f, center = Offset(w * 0.25f, h * 0.65f))
        drawCircle(color = BlushColor.copy(alpha = 0.6f), radius = w * 0.08f, center = Offset(w * 0.75f, h * 0.65f))

        // 5. Mouth
        if (emotion == MascotEmotion.HAPPY) {
            drawArc(
                color = Color(0xFFD81B60),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(w * 0.45f, h * 0.65f),
                size = Size(w * 0.1f, h * 0.05f),
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )
        }
    }
}

enum class MascotEmotion {
    HAPPY, SLEEPY, ANGRY
}
