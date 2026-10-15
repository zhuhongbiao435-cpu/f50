package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ZteF50Repository
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ZteF50ViewModel(
    private val repository: ZteF50Repository = ZteF50Repository()
) : ViewModel() {

    private val _settings = MutableStateFlow(RouterSettings())
    val settings: StateFlow<RouterSettings> = _settings.asStateFlow()

    val deviceStatus: StateFlow<DeviceStatus> = repository.deviceStatus
    val cellularMetrics: StateFlow<CellularMetrics> = repository.cellularMetrics
    val trafficStats: StateFlow<TrafficStats> = repository.trafficStats
    val connectedDevices: StateFlow<List<ConnectedDevice>> = repository.connectedDevices
    val connectionState: StateFlow<RouterConnectionState> = repository.connectionState

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private var pollingJob: Job? = null

    init {
        startPolling()
    }

    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                repository.pollTelemetry(_settings.value)
                val intervalMs = (_settings.value.pollIntervalSeconds.coerceAtLeast(1) * 1000L)
                delay(intervalMs)
            }
        }
    }

    fun refreshNow() {
        viewModelScope.launch {
            repository.pollTelemetry(_settings.value)
        }
    }

    fun updateSettings(newSettings: RouterSettings) {
        val intervalChanged = newSettings.pollIntervalSeconds != _settings.value.pollIntervalSeconds
        val modeChanged = newSettings.isSimulatedMode != _settings.value.isSimulatedMode
        _settings.value = newSettings

        if (intervalChanged || modeChanged) {
            startPolling()
        }
    }

    fun toggleSimulatedMode(enabled: Boolean) {
        updateSettings(_settings.value.copy(isSimulatedMode = enabled))
        _userMessage.value = if (enabled) "已开启演示模拟模式" else "已切换至真实中兴F50连接"
    }

    fun rebootRouter(onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.rebootRouter(_settings.value)
            result.onSuccess {
                _userMessage.value = "重启指令已发送，F50正在重启..."
                onComplete(true, "重启指令已发送成功")
            }.onFailure {
                _userMessage.value = "重启失败: ${it.message}"
                onComplete(false, it.message ?: "重启失败")
            }
        }
    }

    fun restartNetwork(onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.restartNetwork(_settings.value)
            result.onSuccess {
                _userMessage.value = "移动网络已重连"
                onComplete(true, "移动网络重连成功")
            }.onFailure {
                _userMessage.value = "重连失败: ${it.message}"
                onComplete(false, it.message ?: "重连失败")
            }
        }
    }

    fun setBandLockMode(mode: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.setNetworkMode(_settings.value, mode)
            result.onSuccess {
                _userMessage.value = "网络模式已切换至: $mode"
                onComplete(true, "模式切换成功")
            }.onFailure {
                _userMessage.value = "模式设置失败: ${it.message}"
                onComplete(false, it.message ?: "设置失败")
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
