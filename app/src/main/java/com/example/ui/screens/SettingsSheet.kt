package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Tune
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
import com.example.model.RouterSettings
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.ThermalDanger
import com.example.ui.theme.ThermalWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    settings: RouterSettings,
    onSaveSettings: (RouterSettings) -> Unit,
    onRebootClick: () -> Unit,
    onRestartNetworkClick: () -> Unit,
    onSetBandLock: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var gatewayIp by remember { mutableStateOf(settings.gatewayIp) }
    var portStr by remember { mutableStateOf(settings.port.toString()) }
    var password by remember { mutableStateOf(settings.password) }
    var pollInterval by remember { mutableStateOf(settings.pollIntervalSeconds.toFloat()) }
    var tempThreshold by remember { mutableStateOf(settings.highTempThreshold) }
    var isSimulated by remember { mutableStateOf(settings.isSimulatedMode) }

    var showRebootConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.testTag("settings_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = "设置",
                        tint = CyanNeon,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "F50 连接设置与快捷操作",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "关闭")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Simulation / Demo Mode Switch
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "演示与测试模式 (Simulated)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "未连接真实F50 Wi-Fi时可开启，模拟实时温度波动、5G速率与基站数据",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isSimulated,
                        onCheckedChange = {
                            isSimulated = it
                            onSaveSettings(
                                settings.copy(
                                    gatewayIp = gatewayIp,
                                    port = portStr.toIntOrNull() ?: 80,
                                    password = password,
                                    pollIntervalSeconds = pollInterval.toInt(),
                                    highTempThreshold = tempThreshold,
                                    isSimulatedMode = it
                                )
                            )
                        },
                        modifier = Modifier.testTag("simulation_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gateway Configuration Section
            Text(
                text = "网络连接配置",
                style = MaterialTheme.typography.labelLarge,
                color = CyanNeon,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = gatewayIp,
                onValueChange = { gatewayIp = it },
                label = { Text("F50 网关 IP 地址 (默认 192.168.0.1)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gateway_ip_input"),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Router, contentDescription = "网关IP")
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = portStr,
                    onValueChange = { portStr = it },
                    label = { Text("端口 (默认 80)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("管理密码 (可选)") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Lock, contentDescription = "密码")
                    },
                    modifier = Modifier.weight(1.5f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Telemetry Refresh & Threshold Sliders
            Text(
                text = "遥测与报警参数",
                style = MaterialTheme.typography.labelLarge,
                color = CyanNeon,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Poll Interval Slider
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "状态刷新频率",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${pollInterval.toInt()} 秒/次",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon
                        )
                    }
                    Slider(
                        value = pollInterval,
                        onValueChange = { pollInterval = it },
                        valueRange = 1f..5f,
                        steps = 3,
                        modifier = Modifier.testTag("poll_interval_slider")
                    )

                    Divider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(vertical = 8.dp))

                    // High Temp Alert Threshold Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "高温报警阈值",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${tempThreshold.toInt()} °C",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (tempThreshold > 50f) ThermalDanger else ThermalWarning
                        )
                    }
                    Slider(
                        value = tempThreshold,
                        onValueChange = { tempThreshold = it },
                        valueRange = 45f..60f,
                        steps = 14,
                        modifier = Modifier.testTag("temp_threshold_slider")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Operations Section
            Text(
                text = "快捷运维操作",
                style = MaterialTheme.typography.labelLarge,
                color = CyanNeon,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Band Lock buttons
            Text(
                text = "网络制式锁定:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onSetBandLock("5G_ONLY") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("锁5G SA", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { onSetBandLock("4G_ONLY") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("锁4G LTE", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { onSetBandLock("AUTO") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("自动制式", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Restart Network & Reboot
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledTonalButton(
                    onClick = onRestartNetworkClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("重连蜂窝网")
                }

                Button(
                    onClick = { showRebootConfirm = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ThermalDanger)
                ) {
                    Icon(imageVector = Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("重启 F50")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save Button
            Button(
                onClick = {
                    onSaveSettings(
                        settings.copy(
                            gatewayIp = gatewayIp.trim(),
                            port = portStr.toIntOrNull() ?: 80,
                            password = password,
                            pollIntervalSeconds = pollInterval.toInt(),
                            highTempThreshold = tempThreshold,
                            isSimulatedMode = isSimulated
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_settings_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Color(0xFF00363D))
            ) {
                Text("保存并应用设置", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }

    // Confirm Reboot Dialog
    if (showRebootConfirm) {
        AlertDialog(
            onDismissRequest = { showRebootConfirm = false },
            icon = { Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = ThermalDanger) },
            title = { Text("确认重启中兴 F50 吗？") },
            text = { Text("重启过程大约需要 30~50 秒，期间 Wi-Fi 和网络连接将短暂中断。") },
            confirmButton = {
                Button(
                    onClick = {
                        showRebootConfirm = false
                        onRebootClick()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ThermalDanger)
                ) {
                    Text("确认重启")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRebootConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}
