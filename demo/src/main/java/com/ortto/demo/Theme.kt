package com.ortto.demo

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object OrttoColors {
    val Ink = Color(0xFF20212A)
    val Lilac = Color(0xFF8A62F4)
    val Blue = Color(0xFF2979FF)
    val Orange = Color(0xFFFF8B42)
    val Yellow = Color(0xFFFFD45C)
    val Green = Color(0xFF159B68)
    val Coral = Color(0xFFE84B5F)
    val Canvas = Color(0xFFF7F6FB)
    val Matrix = Color(0xFF07110B)
    val MatrixGreen = Color(0xFF5CFF93)
}

private val LightColors = lightColorScheme(
    primary = OrttoColors.Lilac,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9DEFF),
    onPrimaryContainer = Color(0xFF2A135E),
    secondary = OrttoColors.Blue,
    tertiary = OrttoColors.Orange,
    error = OrttoColors.Coral,
    background = OrttoColors.Canvas,
    surface = Color.White,
    onSurface = OrttoColors.Ink,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFCDB9FF),
    secondary = Color(0xFF9BC2FF),
    tertiary = Color(0xFFFFB486),
)

@Composable
fun OrttoDemoTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> dynamicDarkColorScheme(context)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography.copy(
            headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp),
            headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
            titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
            titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
            bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
            bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
            labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
        ),
        content = content,
    )
}
