package com.seeker.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Colori fissi di Seeker (fallback quando i colori dinamici non sono disponibili).
 * Palette ispirata a WiFi-Widget: teal/cyan come primario, toni neutri per lo sfondo.
 */

// ── Light Theme ──
val LightColors = lightColorScheme(
    primary = Color(0xFF00696F),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF79F5FF),
    onPrimaryContainer = Color(0xFF002022),
    secondary = Color(0xFF4A6365),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCCE8EA),
    onSecondaryContainer = Color(0xFF051F21),
    tertiary = Color(0xFF944170),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E8),
    onTertiaryContainer = Color(0xFF3C0028),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFAFDFC),
    onBackground = Color(0xFF191C1C),
    surface = Color(0xFFFAFDFC),
    onSurface = Color(0xFF191C1C),
    surfaceVariant = Color(0xFFDAE4E5),
    onSurfaceVariant = Color(0xFF3F4849),
    outline = Color(0xFF6F797A),
    inverseOnSurface = Color(0xFFEFF1F1),
    inverseSurface = Color(0xFF2D3131),
    inversePrimary = Color(0xFF4DD9E4),
    surfaceTint = Color(0xFF00696F),
    outlineVariant = Color(0xFFBEC8C9),
    scrim = Color(0xFF000000)
)

// ── Dark Theme ──
val DarkColors = darkColorScheme(
    primary = Color(0xFF4DD9E4),
    onPrimary = Color(0xFF00363A),
    primaryContainer = Color(0xFF004F54),
    onPrimaryContainer = Color(0xFF79F5FF),
    secondary = Color(0xFFB1CBCE),
    onSecondary = Color(0xFF1B3436),
    secondaryContainer = Color(0xFF324B4D),
    onSecondaryContainer = Color(0xFFCCE8EA),
    tertiary = Color(0xFFCA4F91),
    onTertiary = Color(0xFF5B113F),
    tertiaryContainer = Color(0xFF772957),
    onTertiaryContainer = Color(0xFFFFD8E8),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF191C1C),
    onBackground = Color(0xFFE0E3E3),
    surface = Color(0xFF191C1C),
    onSurface = Color(0xFFE0E3E3),
    surfaceVariant = Color(0xFF3F4849),
    onSurfaceVariant = Color(0xFFBEC8C9),
    outline = Color(0xFF899393),
    inverseOnSurface = Color(0xFF191C1C),
    inverseSurface = Color(0xFFE0E3E3),
    inversePrimary = Color(0xFF00696F),
    surfaceTint = Color(0xFF4DD9E4),
    outlineVariant = Color(0xFF3F4849),
    scrim = Color(0xFF000000)
)

// ── Colori per l'indicatore di segnale ──
object SignalColors {
    val excellent = Color(0xFF4CAF50)  // Verde
    val good = Color(0xFF8BC34A)       // Verde chiaro
    val fair = Color(0xFFFFC107)       // Giallo
    val weak = Color(0xFFFF9800)       // Arancione
    val veryWeak = Color(0xFFF44336)   // Rosso
    val disconnected = Color(0xFF9E9E9E) // Grigio
}

// ── Colori per bande (visibili sia su sfondo chiaro che scuro) ──
object BandColors {
    val ghz24 = Color(0xFF42A5F5)      // Azzurro brillante
    val ghz5 = Color(0xFFFF8A65)       // Arancione/corallo
    val ghz6 = Color(0xFF4DD0E1)       // Ciano brillante
}

// ── Estensione per accesso comodo ──
val ColorScheme.cardContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = surfaceContainer

val ColorScheme.cardContainerElevated: Color
    @Composable
    @ReadOnlyComposable
    get() = surfaceContainerLowest
