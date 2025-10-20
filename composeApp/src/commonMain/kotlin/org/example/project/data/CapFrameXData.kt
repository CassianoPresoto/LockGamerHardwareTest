package org.example.project.data

import kotlinx.serialization.Serializable

@Serializable
data class CapFrameXData(
    val Hash: String,
    val Info: SystemInfo,
    val Runs: List<CaptureRun>
)

@Serializable
data class SystemInfo(
    val AppVersion: String,
    val Id: String,
    val Processor: String,
    val GameName: String,
    val ProcessName: String,
    val CreationDate: String,
    val Motherboard: String,
    val OS: String,
    val SystemRam: String,
    val BaseDriverVersion: String? = null,
    val GPUDriverVersion: String,
    val DriverPackage: String? = null,
    val GPU: String,
    val GPUCount: String? = null,
    val GpuCoreClock: String? = null,
    val GpuMemoryClock: String? = null,
    val Comment: String? = null,
    val ApiInfo: String,
    val ResizableBar: String,
    val WinGameMode: String,
    val HAGS: String,
    val PresentationMode: String,
    val ResolutionInfo: String? = null
)

@Serializable
data class CaptureRun(
    val Hash: String,
    val PresentMonRuntime: String,
    val CaptureData: CaptureData
)

@Serializable
data class CaptureData(
    val TimeInSeconds: List<Double>,
    val MsBetweenPresents: List<Double>,
    val MsInPresentAPI: List<Double>,
    val MsUntilRenderComplete: List<Double>,
    val MsUntilDisplayed: List<Double>,
    val MsBetweenDisplayChange: List<Double>,
    val AllowsTearing: List<Int>,
    val PresentMode: List<Int>,
    val SyncInterval: List<Int>,
    val Dropped: List<Boolean>,
    val QPCTime: List<Double>,
    val GpuActive: List<Double>? = null,
    val PcLatency: List<Double>? = null
)
