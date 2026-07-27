package com.seeker.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Forme smussate e morbide, ispirate a WiFi-Widget.
 * Le card hanno angoli pronunciati (16dp) per un look moderno.
 */
val SeekerShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

/**
 * Forma specifica per le card principali del dashboard.
 */
val CardShape = RoundedCornerShape(20.dp)

/**
 * Forma per le card secondarie / nested.
 */
val NestedCardShape = RoundedCornerShape(14.dp)

/**
 * Forma per i chip / indicatori.
 */
val ChipShape = RoundedCornerShape(8.dp)
