package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.SignalQualityCard
import com.example.ui.components.SpeedWaveformCard
import com.example.ui.components.ThermalGaugeCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    status: DeviceStatus,
    metrics: CellularMetrics,
    traffic: TrafficStats,
    connectedDevicesCount: Int,
    connectionState: RouterConnectionState,
    settings: RouterSettings,
    onNavigateToCellular: () -> Unit,
    onNavigateToDevices: () -> Unit,
    onOpenSettings: () -> Unit,
    onRefresh: () -> Unit,
    onReboot: () -> Unit,
    onRestartNetwork: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "中兴 F50 5G",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 19.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Connection Status Chip
                                ConnectionStatusBadge(connectionState = connectionState)
                            }
                            Text(
                                text = "网关: http://${settings.gatewayIp}:${settings.port}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToDevices,
                        modifier = Modifier.testTag("devices_button")
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = CyanNeon,
                                    contentColor = Color(0xFF00363D)
                                ) {
                                    Text(connectedDevicesCount.toString())
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Devices,
                                contentDescription = "已连设备"
                            )
                        }
                    }

                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新遥测"
                        )
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "设置"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.testTag("dashboard_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Mode Banner (when in simulated mode or disconnected)
            item {
                when (connectionState) {
                    is RouterConnectionState.Connected -> {
                        if (!connectionState.isRealDevice) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF132238),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyanNeon.copy(0.4f))),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenSettings() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = CyanNeon,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "当前处于「演示模拟模式」",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = CyanNeon
                                        )
                                        Text(
                                            text = "展示F50动态温度、5G波形与基站参数。若已连入F50 Wi-Fi，点此切换为真实设备。",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is RouterConnectionState.Disconnected -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ThermalDanger.copy(alpha = 0.12f),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ThermalDanger.copy(0.5f))),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenSettings() }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = ThermalDanger,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "未能连接到中兴 F50 网关",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ThermalDanger
                                    )
                                    Text(
                                        text = connectionState.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }

            // 1. Real-time Thermal Monitor Card
            item {
                ThermalGaugeCard(
                    temperature = status.temperature,
                    thermalLevel = status.thermalLevel,
                    highTempThreshold = settings.highTempThreshold,
                    tempHistory = traffic.tempHistory
                )
            }

            // 2. Real-time Speed & Traffic Waveform Card
            item {
                SpeedWaveformCard(
                    traffic = traffic,
                    uptimeFormatted = status.formattedUptime
                )
            }

            // 3. Cellular 5G/4G Radio Card
            item {
                SignalQualityCard(
                    metrics = metrics,
                    onViewDetailClick = onNavigateToCellular
                )
            }

            // 4. Quick Actions Hub
            item {
                Text(
                    text = "快捷工具与管理",
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
                    // Connected Devices button
                    QuickActionButton(
                        title = "在线设备",
                        subtitle = "$connectedDevicesCount 台在线",
                        icon = Icons.Outlined.Devices,
                        tint = CyanNeon,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToDevices
                    )

                    // Cellular metrics button
                    QuickActionButton(
                        title = "基站射频",
                        subtitle = "${metrics.band} / ${metrics.pci}",
                        icon = Icons.Outlined.CellTower,
                        tint = BlueAccent,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCellular
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Restart network button
                    QuickActionButton(
                        title = "重启蜂窝网",
                        subtitle = "快速刷频重连",
                        icon = Icons.Outlined.RestartAlt,
                        tint = ThermalWarning,
                        modifier = Modifier.weight(1f),
                        onClick = onRestartNetwork
                    )

                    // Reboot F50 button
                    QuickActionButton(
                        title = "重启设备",
                        subtitle = "一键软重启F50",
                        icon = Icons.Outlined.PowerSettingsNew,
                        tint = ThermalDanger,
                        modifier = Modifier.weight(1f),
                        onClick = onReboot
                    )
                }
            }

            // 5. Hardware Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "设备硬件与供电状态",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Divider(color = Color(0x1AFFFFFF))
                        DetailInfoRow("设备型号", status.model)
                        DetailInfoRow("固件版本", status.firmwareVersion)
                        DetailInfoRow("CPU 负荷", "${status.cpuUsage}%")
                        DetailInfoRow("供电方式", "Type-C 外部供电 (无内置电池)")
                        DetailInfoRow("连续运行", status.formattedUptime)
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusBadge(connectionState: RouterConnectionState) {
    val (label, color) = when (connectionState) {
        is RouterConnectionState.Connected -> {
            if (connectionState.isRealDevice) "实时连线" to SignalExcellent
            else "模拟演示" to CyanNeon
        }
        is RouterConnectionState.Connecting -> "检测中..." to ThermalWarning
        is RouterConnectionState.Disconnected -> "未连接" to ThermalDanger
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
