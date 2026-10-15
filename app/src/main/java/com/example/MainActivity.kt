package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.RouterSettings
import com.example.ui.screens.CellularDetailScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DevicesScreen
import com.example.ui.screens.SettingsSheet
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ZteF50ViewModel

enum class Screen {
    DASHBOARD,
    CELLULAR_DETAIL,
    DEVICES
}

class MainActivity : ComponentActivity() {

    private val viewModel: ZteF50ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                ZteF50App(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ZteF50App(viewModel: ZteF50ViewModel) {
    val status by viewModel.deviceStatus.collectAsStateWithLifecycle()
    val metrics by viewModel.cellularMetrics.collectAsStateWithLifecycle()
    val traffic by viewModel.trafficStats.collectAsStateWithLifecycle()
    val connectedDevices by viewModel.connectedDevices.collectAsStateWithLifecycle()
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // User message feedback
    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // Handle back button on sub-screens
    if (currentScreen != Screen.DASHBOARD) {
        BackHandler {
            currentScreen = Screen.DASHBOARD
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.DASHBOARD -> {
                    DashboardScreen(
                        status = status,
                        metrics = metrics,
                        traffic = traffic,
                        connectedDevicesCount = connectedDevices.size,
                        connectionState = connectionState,
                        settings = settings,
                        onNavigateToCellular = { currentScreen = Screen.CELLULAR_DETAIL },
                        onNavigateToDevices = { currentScreen = Screen.DEVICES },
                        onOpenSettings = { showSettingsSheet = true },
                        onRefresh = { viewModel.refreshNow() },
                        onReboot = {
                            viewModel.rebootRouter { _, _ -> }
                        },
                        onRestartNetwork = {
                            viewModel.restartNetwork { _, _ -> }
                        }
                    )
                }

                Screen.CELLULAR_DETAIL -> {
                    CellularDetailScreen(
                        metrics = metrics,
                        status = status,
                        onBackClick = { currentScreen = Screen.DASHBOARD }
                    )
                }

                Screen.DEVICES -> {
                    DevicesScreen(
                        devices = connectedDevices,
                        onBackClick = { currentScreen = Screen.DASHBOARD }
                    )
                }
            }

            // Settings & Quick Action Bottom Sheet
            if (showSettingsSheet) {
                SettingsSheet(
                    settings = settings,
                    onSaveSettings = { newSettings: RouterSettings ->
                        viewModel.updateSettings(newSettings)
                    },
                    onRebootClick = {
                        viewModel.rebootRouter { _, _ -> }
                    },
                    onRestartNetworkClick = {
                        viewModel.restartNetwork { _, _ -> }
                    },
                    onSetBandLock = { mode ->
                        viewModel.setBandLockMode(mode) { _, _ -> }
                    },
                    onDismiss = { showSettingsSheet = false }
                )
            }
        }
    }
}
