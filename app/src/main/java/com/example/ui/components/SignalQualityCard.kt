package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CellularMetrics
import com.example.ui.theme.*

@Composable
fun SignalQualityCard(
    metrics: CellularMetrics,
    modifier: Modifier = Modifier,
    onViewDetailClick: (() -> Unit)? = null
) {
    val signalColor = when {
        metrics.rsrp >= -85 -> SignalExcellent
        metrics.rsrp >= -95 -> SignalGood
        metrics.rsrp >= -105 -> SignalFair
        else -> SignalPoor
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("signal_quality_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row
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
                            .background(signalColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CellTower,
                            contentDescription = "基站信号",
                            tint = signalColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${metrics.provider} · ${metrics.networkType}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "频段: ${metrics.band} (${metrics.frequency})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 5-bar signal indicator
                SignalBarsRow(bars = metrics.signalBars, activeColor = signalColor)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Radio Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RadioMetricChip(
                    label = "RSRP 接收电平",
                    value = "${metrics.rsrp}",
                    unit = "dBm",
                    statusColor = signalColor,
                    modifier = Modifier.weight(1f)
                )
                RadioMetricChip(
                    label = "SINR 信噪比",
                    value = "${metrics.sinr}",
                    unit = "dB",
                    statusColor = if (metrics.sinr >= 15f) SignalExcellent else SignalFair,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RadioMetricChip(
                    label = "物理小区 PCI",
                    value = metrics.pci,
                    unit = "",
                    statusColor = CyanNeon,
                    modifier = Modifier.weight(1f)
                )
                RadioMetricChip(
                    label = "基站 Cell ID",
                    value = metrics.cellId,
                    unit = "",
                    statusColor = BlueAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Signal evaluation summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F1725))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(signalColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "信号质量: ${metrics.signalQualityDescription}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (onViewDetailClick != null) {
                    TextButton(
                        onClick = onViewDetailClick,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "查看射频详情 >",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyanNeon
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RadioMetricChip(
    label: String,
    value: String,
    unit: String,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F1725)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = statusColor
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = " $unit",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SignalBarsRow(
    bars: Int,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 1..5) {
            val height = (i * 4 + 4).dp
            val isActive = i <= bars
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(height)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isActive) activeColor else Color(0xFF334155))
            )
        }
    }
}
