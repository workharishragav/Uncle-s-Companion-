package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AxisSplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        // Let the gyro needle settle and display branding elegantly
        delay(1800)
        visible = false
        delay(300) // allow fade out animation to finish smoothly
        onSplashFinished()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(600, easing = EaseInOutCubic)),
        exit = fadeOut(animationSpec = tween(400, easing = EaseInOutCubic))
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF030712)), // Ultimate OLED Black
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Centered Minimalist Compass AXIS Logo with premium dynamic needle
                AxisCompassLogo(
                    sizeDp = 180,
                    animateNeedle = true
                )

                Spacer(modifier = Modifier.height(32.dp))

                // High-End Minimalist Silver Branding
                Text(
                    text = "A X I S",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    color = Color(0xFFF8FAFC), // Pure silver white
                    letterSpacing = 8.sp,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "CENTRAL LIFE ALIGNMENT BASE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B), // Slate silver/grey
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
