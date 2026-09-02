package com.example.entrenamientos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FullColorPicker(
    colorHex: String,
    onColorChanged: (String) -> Unit
) {
    val initialColorInt = try { android.graphics.Color.parseColor(colorHex) } catch (e: Exception) { android.graphics.Color.parseColor("#2196F3") }
    val hsl = FloatArray(3)
    androidx.core.graphics.ColorUtils.colorToHSL(initialColorInt, hsl)

    var hue by remember { mutableFloatStateOf(hsl[0]) }
    var lightness by remember { mutableFloatStateOf(hsl[2]) }

    val argb = androidx.core.graphics.ColorUtils.HSLToColor(floatArrayOf(hue, 1f, lightness))
    val currentColor = Color(argb)
    val pureArgb = androidx.core.graphics.ColorUtils.HSLToColor(floatArrayOf(hue, 1f, 0.5f))
    val pureColor = Color(pureArgb)

    val hex = String.format("#%06X", 0xFFFFFF and argb)

    LaunchedEffect(hex) {
        if (hex != colorHex) {
            onColorChanged(hex)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(MaterialTheme.shapes.small)
                .background(currentColor)
                .border(1.dp, Color.Black, MaterialTheme.shapes.small)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text("Color", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
                        )
                    )
            )
            Slider(
                value = hue,
                onValueChange = { hue = it },
                valueRange = 0f..360f,
                colors = SliderDefaults.colors(
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    thumbColor = Color.DarkGray
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Luminosidad", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(Color.Black, pureColor, Color.White)
                        )
                    )
                    .border(0.5.dp, Color.Gray, CircleShape)
            )
            Slider(
                value = lightness,
                onValueChange = { lightness = it },
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    thumbColor = Color.Gray
                )
            )
        }
    }
}