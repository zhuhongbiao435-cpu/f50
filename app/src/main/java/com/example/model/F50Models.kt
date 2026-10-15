package com.example.model

enum class ThermalLevel {
    NORMAL,   // < 45°C
    WARM,     // 45°C - 50°C
    HOT,      // 50°C - 55°C (warning)
    CRITICAL  // >= 55°C (danger of thermal throttling / damage)
}

data class DeviceStatus(
    val model: String = "ZTE F50 5G",
    val temperature: Float = 42.0f,
    val thermalLevel: ThermalLevel = ThermalLevel.NORMAL,
    val uptimeSeconds: Long = 3600L,
    val firmwareVersion: String = "BD_CNF50V1.0.0B08",
    val imei: String = "86782305******9",
    val simStatus: String = "正常已激活",
    val cpuUsage: Int = 28,
    val memoryUsage: Int = 45,
    val isUsbPowered: Boolean = true
) {
    val formattedUptime: String
        get() {
            val hours = uptimeSeconds / 3600
            val minutes = (uptimeSeconds % 3600) / 60
            val secs = uptimeSeconds % 60
            return String.format("%02d:%02d:%02d", hours, minutes, secs)
        }
}

data class CellularMetrics(
    val networkType: String = "5G SA",
    val provider: String = "中国移动",
    val signalBars: Int = 4, // 0 - 5
    val rsrp: Int = -82,     // dBm (-140 to -44)
    val sinr: Float = 21.5f,  // dB (-10 to 40)
    val rsrq: Int = -11,     // dB (-20 to -3)
    val rssi: Int = -65,     // dBm
    val band: String = "n78",
    val frequency: String = "3500 MHz",
    val pci: String = "328",
    val cellId: String = "8421092",
    val caActive: Boolean = true,
    val nrRsrp: Int? = -80,
    val nrSinr: Float? = 23.0f
) {
    val signalQualityDescription: String
        get() = when {
            rsrp >= -80 -> "极佳 (优异)"
            rsrp >= -95 -> "良好 (顺畅)"
            rsrp >= -105 -> "一般 (可用)"
            else -> "较弱 (需调整摆放)"
        }
}

data class TrafficStats(
    val downSpeedBytesPerSec: Long = 18_450_000L, // ~18.4 MB/s (147 Mbps)
    val upSpeedBytesPerSec: Long = 3_820_000L,   // ~3.8 MB/s (30 Mbps)
    val monthlyUsedBytes: Long = 52_400_000_000L, // 52.4 GB
    val monthlyLimitBytes: Long = 100_000_000_000L, // 100 GB
    val downSpeedHistory: List<Float> = emptyList(), // in Mbps
    val tempHistory: List<Float> = emptyList() // in °C
) {
    val downSpeedMbps: Float
        get() = (downSpeedBytesPerSec * 8f) / 1_000_000f

    val upSpeedMbps: Float
        get() = (upSpeedBytesPerSec * 8f) / 1_000_000f

    val monthlyUsedGb: Float
        get() = monthlyUsedBytes / 1_000_000_000f

    val monthlyLimitGb: Float
        get() = monthlyLimitBytes / 1_000_000_000f

    val monthlyUsagePercent: Float
        get() = if (monthlyLimitBytes > 0) (monthlyUsedBytes.toFloat() / monthlyLimitBytes).coerceIn(0f, 1f) else 0f
}

data class ConnectedDevice(
    val ip: String,
    val mac: String,
    val hostname: String,
    val connectionType: String = "Wi-Fi 5GHz",
    val connectedDuration: String = "01:24:32",
    val isCurrentDevice: Boolean = false
)

data class RouterSettings(
    val gatewayIp: String = "192.168.0.1",
    val port: Int = 80,
    val password: String = "",
    val pollIntervalSeconds: Int = 2,
    val highTempThreshold: Float = 52.0f,
    val isSimulatedMode: Boolean = true // Starts in simulated mode until real F50 connection verified
)

sealed interface RouterConnectionState {
    data object Connecting : RouterConnectionState
    data class Connected(val isRealDevice: Boolean) : RouterConnectionState
    data class Disconnected(val message: String) : RouterConnectionState
}
