package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ThermalLevel
import com.example.ui.theme.*

@Composable
fun ThermalGaugeCard(
    temperature: Float,
    thermalLevel: ThermalLevel,
    highTempThreshold: Float,
    tempHistory: List<Float>,
    modifier: Modifier = Modifier
) {
    val isAlert = temperature >= highTempThreshold

    val levelColor by animateColorAsState(
        targetValue = when (thermalLevel) {
            ThermalLevel.NORMAL -> ThermalSafe
            ThermalLevel.WARM -> ThermalWarm
            ThermalLevel.HOT -> ThermalWarning
            ThermalLevel.CRITICAL -> ThermalDanger
        },
        animationSpec = tween(500),
        label = "thermalColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("thermal_gauge_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(20.dp),
        border = if (isAlert) CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(ThermalDanger, ThermalWarning))) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(levelColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeviceThermostat,
                            contentDescription = "温度图标",
                            tint = levelColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "F50 设备实时温度",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "预警阈值: ${highTempThreshold}°C",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = levelColor.copy(alpha = 0.18f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(levelColor.copy(0.4f), levelColor))
                    )
                ) {
                    Text(
                        text = when (thermalLevel) {
                            ThermalLevel.NORMAL -> "正常运转"
                            ThermalLevel.WARM -> "温升正常"
                            ThermalLevel.HOT -> "高温发热"
                            ThermalLevel.CRITICAL -> "严重过热!"
                        },
                        color = levelColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Circular Arc Gauge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(210.dp)
                    .padding(8.dp)
            ) {
                val animatedAngle by animateFloatAsState(
                    targetValue = ((temperature - 30f) / 40f).coerceIn(0.05f, 1f),
                    animationSpec = tween(600),
                    label = "gaugeAngle"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 16.dp.toPx()
                    val arcSize = size.minDimension - strokeWidth
                    val topLeft = Offset(
                        (size.width - arcSize) / 2f,
                        (size.height - arcSize) / 2f
                    )

                    // Track background (240 degrees arc)
                    drawArc(
                        color = Color(0xFF22314C),
                        startAngle = 150f,
                        sweepAngle = 240f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active temperature arc with gradient
                    val sweep = 240f * animatedAngle
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                ThermalSafe,
                                ThermalWarm,
                                ThermalWarning,
                                ThermalDanger
                            )
                        ),
                        startAngle = 150f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Center readout
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format("%.1f", temperature),
                            fontSize = 44.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = " °C",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = levelColor,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Text(
                        text = if (temperature > 50f) "建议加强散热底座" else "F50温控状态良好",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // High temp alert banner
            if (isAlert) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ThermalDanger.copy(alpha = 0.15f))
                        .border(1.dp, ThermalDanger.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "警报",
                        tint = ThermalDanger,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "当前温度已超过预警阈值(${highTempThreshold}°C)，中兴F50可能降频降速，建议搭配散热风扇使用。",
                        style = MaterialTheme.typography.bodySmall,
                        color = ThermalDanger,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Mini History Sparkline
            if (tempHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "温度变化走势 (近30次刷新)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${tempHistory.minOrNull() ?: 0f}°C ~ ${tempHistory.maxOrNull() ?: 0f}°C",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0F1725))
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    val points = tempHistory
                    if (points.size < 2) return@Canvas

                    val min = (points.minOrNull() ?: 35f) - 1f
                    val max = (points.maxOrNull() ?: 55f) + 1f
                    val range = (max - min).coerceAtLeast(1f)

                    val stepX = size.width / (points.size - 1)
                    val path = Path()

                    points.forEachIndexed { i, t ->
                        val x = i * stepX
                        val y = size.height - ((t - min) / range) * size.height
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    drawPath(
                        path = path,
                        color = levelColor,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}
