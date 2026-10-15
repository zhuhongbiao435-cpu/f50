package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CellTower
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Speed
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
import com.example.model.DeviceStatus
import com.example.ui.components.RadioMetricChip
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CellularDetailScreen(
    metrics: CellularMetrics,
    status: DeviceStatus,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("5G/4G 射频基站指标", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = "${metrics.provider} · ${metrics.band}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.testTag("cellular_detail_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Main Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = metrics.networkType,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = CyanNeon
                                )
                                Text(
                                    text = "主载波频段: ${metrics.band} (${metrics.frequency})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SignalExcellent.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (metrics.caActive) "载波聚合 CA 生效" else "单载波",
                                    color = SignalExcellent,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // RF Param Grid
            item {
                Text(
                    text = "核心射频参数 (Radio Frequency)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RadioMetricChip(
                        label = "RSRP (参考信号接收功率)",
                        value = "${metrics.rsrp}",
                        unit = "dBm",
                        statusColor = if (metrics.rsrp >= -85) SignalExcellent else SignalFair,
                        modifier = Modifier.weight(1f)
                    )
                    RadioMetricChip(
                        label = "SINR (信噪比质量)",
                        value = "${metrics.sinr}",
                        unit = "dB",
                        statusColor = if (metrics.sinr >= 15f) SignalExcellent else SignalFair,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RadioMetricChip(
                        label = "RSRQ (接收质量)",
                        value = "${metrics.rsrq}",
                        unit = "dB",
                        statusColor = BlueAccent,
                        modifier = Modifier.weight(1f)
                    )
                    RadioMetricChip(
                        label = "RSSI (全通道强度)",
                        value = "${metrics.rssi}",
                        unit = "dBm",
                        statusColor = BlueAccent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Cell Identity
            item {
                Text(
                    text = "基站身份与物理小区",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailInfoRow("物理小区标识 (PCI)", metrics.pci)
                        DetailInfoRow("基站小区 ID (Cell ID)", metrics.cellId)
                        DetailInfoRow("调制方式", "256QAM 下行 / 64QAM 上行")
                        DetailInfoRow("固件版本", status.firmwareVersion)
                        DetailInfoRow("设备 IMEI", status.imei)
                        DetailInfoRow("SIM 卡状态", status.simStatus)
                    }
                }
            }

            // Signal Range Guideline Table
            item {
                Text(
                    text = "信号质量与摆位参考指南",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SignalStandardItem(
                            level = "极佳 (优异)",
                            rsrp = "> -85 dBm",
                            sinr = "> 20 dB",
                            color = SignalExcellent,
                            desc = "速率可达到峰值，测速最快，适合日常摆放位置。"
                        )
                        Divider(color = Color(0x1AFFFFFF))
                        SignalStandardItem(
                            level = "良好 (顺畅)",
                            rsrp = "-85 ~ -95 dBm",
                            sinr = "13 ~ 20 dB",
                            color = SignalGood,
                            desc = "网络连接稳定，日常观影、游戏流畅无卡顿。"
                        )
                        Divider(color = Color(0x1AFFFFFF))
                        SignalStandardItem(
                            level = "一般 (可用)",
                            rsrp = "-95 ~ -105 dBm",
                            sinr = "0 ~ 13 dB",
                            color = SignalFair,
                            desc = "受墙壁阻挡或距离较远，建议移至靠窗通风处。"
                        )
                        Divider(color = Color(0x1AFFFFFF))
                        SignalStandardItem(
                            level = "较弱 (重连)",
                            rsrp = "< -105 dBm",
                            sinr = "< 0 dB",
                            color = SignalPoor,
                            desc = "信号边缘，极易降速或掉落4G，请调整摆放朝向。"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SignalStandardItem(
    level: String,
    rsrp: String,
    sinr: String,
    color: Color,
    desc: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = level, fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
            }
            Text(
                text = "RSRP $rsrp · SINR $sinr",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
