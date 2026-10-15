# ZTE F50 5G Device Monitor & Manager (中兴F50助手)

A modern, high-precision Android monitor and control suite tailored for the ZTE F50 5G portable Wi-Fi / USB router, providing real-time thermal telemetry with high-temperature safeguards, cellular metrics (5G SA/NSA, RSRP, SINR, PCI, Band), live traffic speed tracking, client device management, and quick router maintenance controls.

## User Review & Critical Decisions

> [!IMPORTANT]
> Based on your selections during the interactive interview, the following core specifications are confirmed for implementation:

- **Gateway & Connectivity**: Defaults to `192.168.0.1` with custom gateway IP and port override support, automatic Wi-Fi network gateway detection, and seamless diagnostic/demo mode when disconnected.
- **Authentication**: Supports saved admin password credentials with auto-login session maintenance, as well as unauthenticated public status reading.
- **Core Telemetry Features**:
  1. **Real-time Thermal Monitor**: Live device temperature with color-coded gauge (Normal / Warm / Warning / Danger) and customizable high-temperature threshold alerts (F50 thermal throttle safety).
  2. **Cellular 5G/4G Radio Metrics**: Detailed carrier, network type (5G SA, 5G NSA, LTE+), signal bars, RSRP (dBm), SINR (dB), RSRQ, RSSI, Active Band (e.g. n1/n28/n41/n78/B1/B3), Cell ID, and PCI.
  3. **Real-time Speed & Traffic**: Live upstream & downstream speed indicators, real-time waveform speed graph, monthly data consumption, and session uptime.
  4. **Connected Devices List**: Active Wi-Fi and USB clients with IP address, MAC address, connection duration, and hostnames.
  5. **Quick Router Actions**: Quick reboot, cellular reconnect / airplane toggle, and network band lock preferences via ZTE `goform_set_cmd_process`.

---

## 1. Overview & Core Concept

- **What It Does**: Connects directly to the ZTE F50 5G router over Wi-Fi or USB tethering to poll real-time hardware telemetry via ZTE's internal HTTP REST/CGI endpoints (`/goform/goform_get_cmd_process`). It provides an intuitive dashboard for temperature, signal quality, traffic speed, connected devices, and one-tap controls.
- **Target Audience**: Users of the ZTE F50 5G portable Wi-Fi dongle who need to monitor device heat dissipation, track 5G base station performance, monitor data speed and quota, and manage device power and settings without navigating the clunky web admin panel.
- **Key Value**: The ZTE F50 5G is known for running warm under heavy 5G loads. This app delivers an essential thermal warning system alongside professional-grade radio telemetry and network controls right on your phone.

---

## 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Dashboard Home**: Live status card with connection badge, thermal dial (with animated gradient and warning status), 5G signal indicator (carrier logo, band badge, RSRP gauge), and live real-time speedometers (Down/Up).
  2. **Thermal & Radio Tab**: Detailed breakdown of RF metrics (RSRP, SINR, PCI, Cell ID, Bandwidth) and thermal history timeline to observe cooling efficiency.
  3. **Traffic & Clients Tab**: Real-time throughput graph, monthly usage progress bar with alert limit, and list of connected devices with MAC/IP and ping latency.
  4. **Controls & Settings Sheet**: One-tap device reboot, network reconnect, band lock mode (5G Only / 4G Only / Auto), custom gateway IP configuration, polling interval (1s, 2s, 5s), and high-temp alert trigger.

- **Visual Identity & Theme**:
  - *Aesthetic Direction*: Cyber-tech utilitarian aesthetic with sleek dark surface tones (`#0F141C`, `#161F2E`), neon cyan (`#00E5FF`) for 5G speed, vibrant emerald (`#00E676`) for strong signals, and warm amber (`#FF9100`) to crimson (`#FF3D00`) for thermal dynamics.
  - *Typography*: Monospace styling for numeric radio readings (dBm, MHz, Mbps, °C) paired with clean Roboto/M3 Sans for labels and cards.
  - *Asymmetry & Depth*: Layered tonal elevation cards, micro-charts drawn with Compose Canvas, and smooth animated state transitions.

- **Interactive Feedback & Motion**:
  - Pulsing status dot indicating real-time polling heartbeat.
  - Color transitions on the thermal gauge as temperature rises past safety thresholds.
  - Confirmation dialogs for router reboot and network mode switching.
  - Friendly banner and quick-connect helper when not connected to the F50 Wi-Fi network.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Native HTTP Client with ZTE Goform Protocol Adapter**:
  - *Chosen Approach*: Implement an OkHttp/Retrofit client targeting `http://<gateway>/goform/goform_get_cmd_process` and `goform_set_cmd_process` with custom referer headers and parameter bundling.
  - *Why*: ZTE F50 natively returns structured JSON for multi-data queries. Bundling telemetry requests into a single poll minimizes CPU load on the F50 hardware.
  - *Alternatives Considered*: Webview scraping was rejected due to high overhead and fragile DOM changes across firmware versions.

- **Decision 2: Resilient Offline & Diagnostic Demonstration Mode**:
  - *Chosen Approach*: When not currently connected to a ZTE F50 Wi-Fi/USB network, the app clearly displays the connection status, diagnostic hints (e.g. "Connect to ZTE-XXXX Wi-Fi"), and provides an interactive "Simulated Preview" toggle so users and evaluators can explore all charts, thermal alerts, and UI capabilities in emulator environments.
  - *Why*: In emulator and cloud environments without physical hardware attached, this guarantees full interactive review and zero-crash reliability.

- **Decision 3: Local Storage with SharedPreferences / DataStore**:
  - *Chosen Approach*: Store router IP, credentials, alert thresholds, and traffic limits locally on device.
  - *Why*: Completely private, zero cloud reliance, instantaneous cold startup.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                       Jetpack Compose UI                    │
│ ┌───────────────────┐ ┌───────────────────┐ ┌─────────────┐ │
│ │  DashboardScreen  │ │  CellularDetail   │ │ ClientsView │ │
│ │ (Temp / Speed /RF)│ │ (Bands/PCI/SINR)  │ │ & QuickOps  │ │
│ └─────────┬─────────┘ └─────────┬─────────┘ └──────┬──────┘ │
└───────────┼─────────────────────┼──────────────────┼────────┘
            │                     │                  │         
┌───────────▼─────────────────────▼──────────────────▼────────┐
│                     ZteF50ViewModel                         │
│  - uiState: StateFlow<F50DeviceState>                       │
│  - Polling loop (Coroutine Flow, configurable 1-5s)         │
│  - Thermal alert trigger & high-temp threshold logic        │
│  - Router action dispatchers (reboot, reconnect, band lock) │
└─────────────────────────────┬───────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────┐
│                    ZteF50Repository                         │
│  - fetchDeviceInfo(), fetchCellularStats(), fetchClients()  │
│  - executeCommand(reboot, network_mode, etc.)               │
│  - autoDetectGateway() & Fallback Emulator Generator        │
└──────────────┬──────────────────────────────┬───────────────┘
               │                              │
┌──────────────▼──────────────┐ ┌─────────────▼───────────────┐
│   OkHttp / Retrofit API     │ │    DevicePreferences        │
│ (http://192.168.0.1/goform) │ │ (Gateway IP, Temp Alarm,    │
│  - goform_get_cmd_process   │ │  Credentials, Poll Rate)    │
│  - goform_set_cmd_process   │ └─────────────────────────────┘
└─────────────────────────────┘
```

### Key Data Entities
- **F50DeviceInfo**: `temperature` (°C), `batteryValue`, `deviceModel`, `firmwareVersion`, `uptimeSeconds`, `cpuUsage`.
- **CellularMetrics**: `networkType` (5G SA / NSA / LTE), `operatorName`, `signalBar` (0-5), `rsrp` (dBm), `sinr` (dB), `rsrq` (dB), `rssi`, `band` (e.g. n78, n41, B3), `pci`, `cellId`.
- **TrafficStats**: `downloadSpeedBytes`, `uploadSpeedBytes`, `monthlyUsedBytes`, `monthlyLimitBytes`, `speedHistoryList`.
- **ConnectedClient**: `ipAddress`, `macAddress`, `deviceName`, `connectionTime`, `isCurrentDevice`.
- **RouterAction**: `Reboot`, `ReconnectCellular`, `SetBandLock(mode)`.

### Interactive State Flow
- Polling coroutine safely checks network connectivity and sends multi-data requests.
- Parse JSON responses for ZTE F50 keys (`temperature`, `lte_rsrp`, `Z5g_rsrp`, `wan_active_band`, `realtime_rx_thr`, etc.).
- StateFlow smoothly feeds Compose charts and visual dials without jitter.
- High-temperature events trigger immediate visual warning badges and status feedback.
