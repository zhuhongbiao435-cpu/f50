package com.example.data

import com.example.model.*
import com.example.network.ZteF50Client
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class ZteF50Repository(
    private val client: ZteF50Client = ZteF50Client()
) {
    private val _deviceStatus = MutableStateFlow(DeviceStatus())
    val deviceStatus: StateFlow<DeviceStatus> = _deviceStatus.asStateFlow()

    private val _cellularMetrics = MutableStateFlow(CellularMetrics())
    val cellularMetrics: StateFlow<CellularMetrics> = _cellularMetrics.asStateFlow()

    private val _trafficStats = MutableStateFlow(TrafficStats())
    val trafficStats: StateFlow<TrafficStats> = _trafficStats.asStateFlow()

    private val _connectedDevices = MutableStateFlow<List<ConnectedDevice>>(emptyList())
    val connectedDevices: StateFlow<List<ConnectedDevice>> = _connectedDevices.asStateFlow()

    private val _connectionState = MutableStateFlow<RouterConnectionState>(RouterConnectionState.Connecting)
    val connectionState: StateFlow<RouterConnectionState> = _connectionState.asStateFlow()

    private val speedHistory = ArrayDeque<Float>(30)
    private val tempHistory = ArrayDeque<Float>(30)

    // Simulation state variables for realistic dynamics
    private var simTemp = 44.5f
    private var simDownSpeed = 195.0f // Mbps
    private var simUptime = 4280L
    private var simMonthlyBytes = 46_200_000_000L

    init {
        // Pre-fill history buffers with realistic curves
        repeat(20) {
            val s = 140f + Random.nextFloat() * 80f
            speedHistory.addLast(s)
            val t = 42f + (it * 0.2f) + Random.nextFloat() * 0.4f
            tempHistory.addLast(t)
        }
        _connectedDevices.value = defaultSimulatedDevices()
    }

    suspend fun pollTelemetry(settings: RouterSettings) {
        if (settings.isSimulatedMode) {
            simulateStep(settings.highTempThreshold)
            _connectionState.value = RouterConnectionState.Connected(isRealDevice = false)
            return
        }

        // Attempt live poll to ZTE F50
        val result = client.fetchStatus(settings.gatewayIp, settings.port)
        result.onSuccess { data ->
            parseLiveTelemetry(data, settings.highTempThreshold)
            _connectionState.value = RouterConnectionState.Connected(isRealDevice = true)
        }.onFailure { err ->
            _connectionState.value = RouterConnectionState.Disconnected(
                err.message ?: "无法连接到 ${settings.gatewayIp}:${settings.port}，请确认已连接中兴F50的Wi-Fi或USB网络。"
            )
        }
    }

    private fun parseLiveTelemetry(data: Map<String, String>, tempThreshold: Float) {
        // Parse Temperature
        val rawTempStr = data["temperature"] ?: "43"
        val rawTemp = rawTempStr.toFloatOrNull() ?: 43.0f
        val actualTemp = if (rawTemp > 150f) rawTemp / 10f else rawTemp

        val level = when {
            actualTemp >= 55f -> ThermalLevel.CRITICAL
            actualTemp >= tempThreshold -> ThermalLevel.HOT
            actualTemp >= 46f -> ThermalLevel.WARM
            else -> ThermalLevel.NORMAL
        }

        val rawUptime = data["realtime_time"]?.toLongOrNull() ?: _deviceStatus.value.uptimeSeconds + 2

        _deviceStatus.value = _deviceStatus.value.copy(
            model = "ZTE F50 5G",
            temperature = actualTemp,
            thermalLevel = level,
            uptimeSeconds = rawUptime,
            firmwareVersion = data["cr_version"] ?: data["wa_inner_version"] ?: "BD_CNF50V1.0.0B08",
            simStatus = if (data["sim_card_status"] == "1" || data["sim_card_status"]?.contains("ok", ignoreCase = true) == true) "正常" else "SIM就绪",
            cpuUsage = (20 + (actualTemp - 38f).coerceAtLeast(0f) * 2.5f).toInt().coerceIn(15, 95)
        )

        // Parse Cellular metrics
        val pciVal = data["lte_pci"] ?: "328"
        val rsrpVal = data["Z5g_rsrp"]?.toIntOrNull() ?: data["lte_rsrp"]?.toIntOrNull() ?: -85
        val sinrVal = data["Z5g_sinr"]?.toFloatOrNull() ?: data["lte_sinr"]?.toFloatOrNull() ?: 18.0f
        val bandVal = data["wan_active_band"] ?: "n78"
        val signalBarVal = data["signalbar"]?.toIntOrNull() ?: 4
        val providerVal = when (data["network_provider"]?.uppercase()) {
            "CHINA MOBILE", "CMCC" -> "中国移动"
            "CHINA TELECOM", "CT" -> "中国电信"
            "CHINA UNICOM", "CU" -> "中国联通"
            "BROADNET", "CBN" -> "中国广电"
            else -> data["network_provider"] ?: "中国移动"
        }

        _cellularMetrics.value = CellularMetrics(
            networkType = if (data["network_type"]?.contains("5G", ignoreCase = true) == true) "5G SA" else (data["network_type"] ?: "5G SA"),
            provider = providerVal,
            signalBars = signalBarVal.coerceIn(0, 5),
            rsrp = rsrpVal,
            sinr = sinrVal,
            rsrq = data["lte_rsrq"]?.toIntOrNull() ?: -10,
            rssi = data["lte_rssi"]?.toIntOrNull() ?: -62,
            band = bandVal,
            frequency = getBandFrequency(bandVal),
            pci = pciVal,
            cellId = data["Z5g_CellID"] ?: "8421092",
            caActive = true
        )

        // Parse Speeds (bytes/sec)
        val rxBytesPerSec = data["realtime_rx_thr"]?.toLongOrNull() ?: 0L
        val txBytesPerSec = data["realtime_tx_thr"]?.toLongOrNull() ?: 0L
        val monthlyRx = data["monthly_rx_bytes"]?.toLongOrNull() ?: _trafficStats.value.monthlyUsedBytes
        val monthlyTx = data["monthly_tx_bytes"]?.toLongOrNull() ?: 0L

        val downMbps = (rxBytesPerSec * 8f) / 1_000_000f
        pushHistory(speedHistory, downMbps)
        pushHistory(tempHistory, actualTemp)

        _trafficStats.value = _trafficStats.value.copy(
            downSpeedBytesPerSec = rxBytesPerSec,
            upSpeedBytesPerSec = txBytesPerSec,
            monthlyUsedBytes = monthlyRx + monthlyTx,
            downSpeedHistory = speedHistory.toList(),
            tempHistory = tempHistory.toList()
        )
    }

    private fun simulateStep(tempThreshold: Float) {
        // Realistic thermal fluctuation
        val tempDelta = (Random.nextFloat() - 0.48f) * 0.4f
        simTemp = (simTemp + tempDelta).coerceIn(41.0f, 57.5f)

        val level = when {
            simTemp >= 55f -> ThermalLevel.CRITICAL
            simTemp >= tempThreshold -> ThermalLevel.HOT
            simTemp >= 46f -> ThermalLevel.WARM
            else -> ThermalLevel.NORMAL
        }

        simUptime += 2
        val simCpu = (22 + (simTemp - 40f) * 3f + Random.nextInt(8)).toInt().coerceIn(18, 92)

        _deviceStatus.value = DeviceStatus(
            model = "中兴 ZTE F50 5G",
            temperature = Math.round(simTemp * 10f) / 10f,
            thermalLevel = level,
            uptimeSeconds = simUptime,
            firmwareVersion = "BD_CNF50V1.0.0B08",
            imei = "867823057819234",
            simStatus = "正常已激活",
            cpuUsage = simCpu,
            memoryUsage = 42,
            isUsbPowered = true
        )

        // Cellular simulation
        val rsrpNoise = Random.nextInt(-2, 3)
        val sinrNoise = (Random.nextFloat() - 0.5f) * 2f
        _cellularMetrics.value = CellularMetrics(
            networkType = "5G SA (Sub-6G)",
            provider = "中国移动 5G",
            signalBars = 4,
            rsrp = -78 + rsrpNoise,
            sinr = Math.round((22.4f + sinrNoise) * 10f) / 10f,
            rsrq = -11,
            rssi = -64,
            band = "n78",
            frequency = "3500 MHz (100MHz带宽)",
            pci = "342",
            cellId = "8540912",
            caActive = true
        )

        // Speed simulation (140 - 290 Mbps)
        val speedVariation = (Random.nextFloat() - 0.47f) * 25f
        simDownSpeed = (simDownSpeed + speedVariation).coerceIn(120f, 320f)
        val simUpSpeed = (simDownSpeed * 0.18f).coerceIn(15f, 60f)

        val downBytes = (simDownSpeed * 1_000_000f / 8f).toLong()
        val upBytes = (simUpSpeed * 1_000_000f / 8f).toLong()
        simMonthlyBytes += downBytes * 2

        pushHistory(speedHistory, simDownSpeed)
        pushHistory(tempHistory, simTemp)

        _trafficStats.value = TrafficStats(
            downSpeedBytesPerSec = downBytes,
            upSpeedBytesPerSec = upBytes,
            monthlyUsedBytes = simMonthlyBytes,
            monthlyLimitBytes = 100_000_000_000L,
            downSpeedHistory = speedHistory.toList(),
            tempHistory = tempHistory.toList()
        )
    }

    private fun pushHistory(deque: ArrayDeque<Float>, value: Float) {
        if (deque.size >= 30) {
            deque.removeFirst()
        }
        deque.addLast(value)
    }

    private fun getBandFrequency(band: String): String = when (band.lowercase()) {
        "n78" -> "3500 MHz (Sub-6G)"
        "n41" -> "2500 MHz (Sub-6G)"
        "n28", "n28a" -> "700 MHz (广电/移动黄金频段)"
        "n1" -> "2100 MHz"
        "b1" -> "2100 MHz (LTE)"
        "b3" -> "1800 MHz (LTE)"
        "b39" -> "1900 MHz (LTE)"
        "b40" -> "2300 MHz (LTE)"
        "b41" -> "2500 MHz (LTE)"
        else -> "5G NR"
    }

    private fun defaultSimulatedDevices(): List<ConnectedDevice> = listOf(
        ConnectedDevice(
            ip = "192.168.0.102",
            mac = "8C:85:90:5A:23:E1",
            hostname = "当前安卓设备 (本设备)",
            connectionType = "Wi-Fi 5GHz (1201 Mbps)",
            connectedDuration = "01:42:15",
            isCurrentDevice = true
        ),
        ConnectedDevice(
            ip = "192.168.0.105",
            mac = "F4:D4:88:9C:12:33",
            hostname = "MacBook Pro M2",
            connectionType = "Wi-Fi 5GHz (866 Mbps)",
            connectedDuration = "03:15:00",
            isCurrentDevice = false
        ),
        ConnectedDevice(
            ip = "192.168.0.110",
            mac = "38:F9:D3:41:88:02",
            hostname = "iPad Pro 11",
            connectionType = "Wi-Fi 2.4GHz",
            connectedDuration = "00:25:40",
            isCurrentDevice = false
        )
    )

    suspend fun rebootRouter(settings: RouterSettings): Result<String> {
        if (settings.isSimulatedMode) {
            simUptime = 0
            return Result.success("OK (已触发重启)")
        }
        return client.rebootRouter(settings.gatewayIp, settings.port)
    }

    suspend fun restartNetwork(settings: RouterSettings): Result<String> {
        if (settings.isSimulatedMode) {
            return Result.success("OK (网络已重连)")
        }
        return client.restartNetwork(settings.gatewayIp, settings.port)
    }

    suspend fun setNetworkMode(settings: RouterSettings, mode: String): Result<String> {
        if (settings.isSimulatedMode) {
            _cellularMetrics.value = _cellularMetrics.value.copy(
                networkType = if (mode == "5G_ONLY") "5G SA Only" else if (mode == "4G_ONLY") "LTE Only" else "5G/4G Auto"
            )
            return Result.success("OK (模式已切换)")
        }
        return client.setNetworkMode(settings.gatewayIp, settings.port, mode)
    }
}
