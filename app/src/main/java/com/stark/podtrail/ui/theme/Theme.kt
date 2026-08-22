package com.stark.podtrail.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import androidx.compose.ui.graphics.Color

val TealPrimary = Color(0xFF0F5A56) // Deep Teal
val CreamBackground = Color(0xFFF7F4EB) // Cream/Off-white
val SurfaceCream = Color(0xFFFFFDF5) // Lighter Cream for cards
val DarkText = Color(0xFF1A1C19)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    secondary = TealPrimary,
    onSecondary = Color.White,
    tertiary = Color(0xFFD0BCFF),
    background = CreamBackground,
    onBackground = DarkText,
    surface = SurfaceCream,
    onSurface = DarkText,
)

private val DarkColorScheme = darkColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    secondary = TealPrimary,
    onSecondary = Color.White,
    background = Color(0xFF101412), // Darker fallback for pure dark mode
    surface = Color(0xFF1E2321)
)

@Composable
fun PodTrailTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = Shapes,
        typography = Typography,
        content = content
    )
}

@Composable
fun PodTrailTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    amoled: Boolean,
    customColor: Int,
    content: @Composable () -> Unit
) {
    val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        customColor != 0 && customColor != -1 -> {
            val seed = Color(customColor)
            baseScheme.copy(primary = seed, secondary = seed)
        }
        else -> baseScheme
    }
    
    // Apply AMOLED black if requested
    val finalScheme = if (darkTheme && amoled) {
        colorScheme.copy(
            background = Color.Black,
            surface = Color.Black
        )
    } else colorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = finalScheme,
        shapes = Shapes,
        typography = Typography,
        content = content
    )
}

