package com.example.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ZteF50Client {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(2500, TimeUnit.MILLISECONDS)
        .readTimeout(2500, TimeUnit.MILLISECONDS)
        .writeTimeout(2500, TimeUnit.MILLISECONDS)
        .build()

    private val multiDataCmds = listOf(
        "isTest",
        "wa_inner_version",
        "cr_version",
        "network_provider",
        "network_type",
        "signalbar",
        "wan_active_band",
        "wan_active_channel",
        "lte_pci",
        "lte_rsrp",
        "lte_sinr",
        "lte_rsrq",
        "lte_rssi",
        "Z5g_rsrp",
        "Z5g_sinr",
        "Z5g_CellID",
        "temperature",
        "battery_value",
        "flux_state",
        "realtime_tx_thr",
        "realtime_rx_thr",
        "realtime_time",
        "monthly_tx_bytes",
        "monthly_rx_bytes",
        "station_num",
        "station_list",
        "modem_main_state",
        "sim_card_status"
    ).joinToString(separator = "%2C")

    suspend fun fetchStatus(gatewayIp: String, port: Int): Result<Map<String, String>> =
        withContext(Dispatchers.IO) {
            val url = "http://$gatewayIp:$port/goform/goform_get_cmd_process?isTest=false&cmd=$multiDataCmds&multi_data=1"
            val request = Request.Builder()
                .url(url)
                .header("Referer", "http://$gatewayIp/index.html")
                .header("Accept", "application/json, text/javascript, */*; q=0.01")
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile) ZTEF50Monitor/1.0")
                .get()
                .build()

            try {
                val response = okHttpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
                }
                val bodyString = response.body?.string() ?: ""
                val json = JSONObject(bodyString)
                val map = mutableMapOf<String, String>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    map[key] = json.optString(key)
                }
                Result.success(map)
            } catch (e: Exception) {
                Log.w("ZteF50Client", "Error fetching from $url: ${e.message}")
                Result.failure(e)
            }
        }

    suspend fun executeCommand(
        gatewayIp: String,
        port: Int,
        formParams: Map<String, String>
    ): Result<String> = withContext(Dispatchers.IO) {
        val url = "http://$gatewayIp:$port/goform/goform_set_cmd_process"
        val formBuilder = FormBody.Builder()
        formParams.forEach { (k, v) ->
            formBuilder.add(k, v)
        }

        val request = Request.Builder()
            .url(url)
            .header("Referer", "http://$gatewayIp/index.html")
            .header("X-Requested-With", "XMLHttpRequest")
            .post(formBuilder.build())
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""
            Result.success(body)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rebootRouter(gatewayIp: String, port: Int): Result<String> {
        return executeCommand(gatewayIp, port, mapOf("goformId" to "REBOOT_DEVICE", "isTest" to "false"))
    }

    suspend fun restartNetwork(gatewayIp: String, port: Int): Result<String> {
        executeCommand(gatewayIp, port, mapOf("goformId" to "DISCONNECT_NETWORK"))
        kotlinx.coroutines.delay(1000)
        return executeCommand(gatewayIp, port, mapOf("goformId" to "CONNECT_NETWORK"))
    }

    suspend fun setNetworkMode(gatewayIp: String, port: Int, mode: String): Result<String> {
        val pref = when (mode) {
            "5G_ONLY" -> "Only_5G"
            "4G_ONLY" -> "Only_LTE"
            else -> "NETWORK_auto"
        }
        return executeCommand(gatewayIp, port, mapOf("goformId" to "SET_BEARER_PREFERENCE", "BearerPreference" to pref))
    }
}
