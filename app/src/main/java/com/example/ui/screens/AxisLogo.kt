package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

@Composable
fun AxisCompassLogo(
    modifier: Modifier = Modifier,
    sizeDp: Int = 120,
    animateNeedle: Boolean = true
) {
    // Elegant system leveling and alignment physical settling animation
    var isStarted by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isStarted = true
    }

    // Rather than spinning like a 360-degree compass or clock, it levels out on launch
    // settling from -25° (tilted) directly to perfect 0° (upright and square) with dynamic spring dampening.
    val targetLevel = 0f
    val alignmentLevel by animateFloatAsState(
        targetValue = if (isStarted && animateNeedle) targetLevel else -25f,
        animationSpec = if (animateNeedle) {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        } else {
            snap()
        },
        label = "SystemAlignmentLevel"
    )

    Canvas(
        modifier = modifier.size(sizeDp.dp)
    ) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f)
        
        // Dimensions calculated dynamically to keep the design fully container-responsive
        val maxBound = width * 0.42f
        val innerBound = width * 0.22f
        val bracketOffset = width * 0.28f
        val bracketLen = width * 0.08f

        // Slate/Silver color scheme matching the premium AXIS design identity
        val colorBackdropDiamond = Color(0xFF111827) // OLED Ground
        val colorOuterBrackets = Color(0xFF94A3B8)   // Slate 300 (Silver Framing)
        val colorInnerAnchor = Color(0xFFE2E8F0)     // Slate 200 (Bright Base)
        val colorMainAxes = Color(0xFF475569)        // Slate 600 (Base Coordinates)
        val colorCenterCore = Color(0xFF38BDF8)       // Celestial Cyan (Central Core Focus Node)
        val colorGuideMarks = Color(0xFF334155)      // Slate 700 (Structural gridlines)

        // 1. Grid Axes Lines (Primary crosshair coordinate lines)
        // These stay perfectly fixed to ground the visual structure
        drawLine(
            color = colorMainAxes.copy(alpha = 0.5f),
            start = Offset(center.x - maxBound, center.y),
            end = Offset(center.x + maxBound, center.y),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = colorMainAxes.copy(alpha = 0.5f),
            start = Offset(center.x, center.y - maxBound),
            end = Offset(center.x, center.y + maxBound),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )

        // 2. Precision Framing Brackets (Calibration corners representing boundary safety)
        // Top-Left
        drawPath(
            path = Path().apply {
                moveTo(center.x - bracketOffset, center.y - bracketOffset + bracketLen)
                lineTo(center.x - bracketOffset, center.y - bracketOffset)
                lineTo(center.x - bracketOffset + bracketLen, center.y - bracketOffset)
            },
            color = colorOuterBrackets,
            style = Stroke(width = 3.5f, cap = StrokeCap.Square)
        )
        // Top-Right
        drawPath(
            path = Path().apply {
                moveTo(center.x + bracketOffset, center.y - bracketOffset + bracketLen)
                lineTo(center.x + bracketOffset, center.y - bracketOffset)
                lineTo(center.x + bracketOffset - bracketLen, center.y - bracketOffset)
            },
            color = colorOuterBrackets,
            style = Stroke(width = 3.5f, cap = StrokeCap.Square)
        )
        // Bottom-Left
        drawPath(
            path = Path().apply {
                moveTo(center.x - bracketOffset, center.y + bracketOffset - bracketLen)
                lineTo(center.x - bracketOffset, center.y + bracketOffset)
                lineTo(center.x - bracketOffset + bracketLen, center.y + bracketOffset)
            },
            color = colorOuterBrackets,
            style = Stroke(width = 3.5f, cap = StrokeCap.Square)
        )
        // Bottom-Right
        drawPath(
            path = Path().apply {
                moveTo(center.x + bracketOffset, center.y + bracketOffset - bracketLen)
                lineTo(center.x + bracketOffset, center.y + bracketOffset)
                lineTo(center.x + bracketOffset - bracketLen, center.y + bracketOffset)
            },
            color = colorOuterBrackets,
            style = Stroke(width = 3.5f, cap = StrokeCap.Square)
        )

        // 3. Coordinate Calibration Guide Marks
        // Microscopic outer tick lines displaying structured engineering logic
        val guideOffset = bracketOffset * 1.3f
        val guideTick = width * 0.02f
        drawLine(colorGuideMarks, Offset(center.x - guideOffset, center.y - guideOffset), Offset(center.x - guideOffset + guideTick, center.y - guideOffset), strokeWidth = 1.5f)
        drawLine(colorGuideMarks, Offset(center.x - guideOffset, center.y - guideOffset), Offset(center.x - guideOffset, center.y - guideOffset + guideTick), strokeWidth = 1.5f)
        drawLine(colorGuideMarks, Offset(center.x + guideOffset, center.y - guideOffset), Offset(center.x + guideOffset - guideTick, center.y - guideOffset), strokeWidth = 1.5f)
        drawLine(colorGuideMarks, Offset(center.x + guideOffset, center.y - guideOffset), Offset(center.x + guideOffset, center.y - guideOffset + guideTick), strokeWidth = 1.5f)
        drawLine(colorGuideMarks, Offset(center.x - guideOffset, center.y + guideOffset), Offset(center.x - guideOffset + guideTick, center.y + guideOffset), strokeWidth = 1.5f)
        drawLine(colorGuideMarks, Offset(center.x - guideOffset, center.y + guideOffset), Offset(center.x - guideOffset, center.y + guideOffset - guideTick), strokeWidth = 1.5f)
        drawLine(colorGuideMarks, Offset(center.x + guideOffset, center.y + guideOffset), Offset(center.x + guideOffset - guideTick, center.y + guideOffset), strokeWidth = 1.5f)
        drawLine(colorGuideMarks, Offset(center.x + guideOffset, center.y + guideOffset), Offset(center.x + guideOffset, center.y + guideOffset - guideTick), strokeWidth = 1.5f)

        // 4. Central Aligned Elements (Performing subtle calibration swing to perfect level)
        rotate(degrees = alignmentLevel, pivot = center) {
            
            // Core backdrop stabilizing diamond
            drawPath(
                path = Path().apply {
                    moveTo(center.x, center.y - innerBound * 1.4f)
                    lineTo(center.x + innerBound * 1.4f, center.y)
                    lineTo(center.x, center.y + innerBound * 1.4f)
                    lineTo(center.x - innerBound * 1.4f, center.y)
                    close()
                },
                color = colorBackdropDiamond
            )

            // Primary vertical alignment axis spire (High-contrast direction)
            drawLine(
                color = colorInnerAnchor,
                start = Offset(center.x, center.y - innerBound * 1.5f),
                end = Offset(center.x, center.y + innerBound * 1.5f),
                strokeWidth = 3.5f,
                cap = StrokeCap.Round
            )

            // Primary horizontal alignment balance bar
            drawLine(
                color = colorMainAxes,
                start = Offset(center.x - innerBound * 1.5f, center.y),
                end = Offset(center.x + innerBound * 1.5f, center.y),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )

            // Core Balance Diamond Envelope (Anchors the system)
            drawPath(
                path = Path().apply {
                    moveTo(center.x, center.y - innerBound)
                    lineTo(center.x + innerBound, center.y)
                    lineTo(center.x, center.y + innerBound)
                    lineTo(center.x - innerBound, center.y)
                    close()
                },
                color = colorInnerAnchor,
                style = Stroke(width = 3.5f)
            )

            // Celestial Cyan (Central Alignment Pinpoint Node - representing perfect focal lock)
            drawCircle(
                color = colorCenterCore,
                radius = innerBound * 0.18f
            )
            // Tiny inner stability white light point
            drawCircle(
                color = Color.White,
                radius = innerBound * 0.06f
            )
        }
    }
}
