package com.example

import com.example.model.DeviceStatus
import com.example.model.ThermalLevel
import com.example.model.TrafficStats
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testThermalLevelEvaluation() {
        val normalStatus = DeviceStatus(temperature = 42.0f, thermalLevel = ThermalLevel.NORMAL)
        assertEquals(ThermalLevel.NORMAL, normalStatus.thermalLevel)

        val hotStatus = DeviceStatus(temperature = 53.0f, thermalLevel = ThermalLevel.HOT)
        assertEquals(ThermalLevel.HOT, hotStatus.thermalLevel)

        val criticalStatus = DeviceStatus(temperature = 56.5f, thermalLevel = ThermalLevel.CRITICAL)
        assertEquals(ThermalLevel.CRITICAL, criticalStatus.thermalLevel)
    }

    @Test
    fun testUptimeFormatting() {
        val status = DeviceStatus(uptimeSeconds = 3665L)
        assertEquals("01:01:05", status.formattedUptime)
    }

    @Test
    fun testTrafficStatsMbpsConversion() {
        val traffic = TrafficStats(
            downSpeedBytesPerSec = 12_500_000L, // 12.5 MB/s = 100 Mbps
            upSpeedBytesPerSec = 2_500_000L     // 2.5 MB/s = 20 Mbps
        )
        assertEquals(100f, traffic.downSpeedMbps, 0.1f)
        assertEquals(20f, traffic.upSpeedMbps, 0.1f)
    }
}
