package org.example.project.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CapFrameXData(
    @SerialName("Hash") val hash: String,
    @SerialName("Info") val info: SystemInfo,
    @SerialName("Runs") val runs: List<CaptureRun>
)

@Serializable
data class SystemInfo(
    @SerialName("AppVersion") val appVersion: String,
    @SerialName("Id") val id: String,
    @SerialName("Processor") val processor: String,
    @SerialName("GameName") val gameName: String,
    @SerialName("ProcessName") val processName: String,
    @SerialName("CreationDate") val creationDate: String,
    @SerialName("Motherboard") val motherboard: String,
    @SerialName("OS") val os: String,
    @SerialName("SystemRam") val systemRam: String,
    @SerialName("BaseDriverVersion") val baseDriverVersion: String? = null,
    @SerialName("GPUDriverVersion") val gpuDriverVersion: String,
    @SerialName("DriverPackage") val driverPackage: String? = null,
    @SerialName("GPU") val gpu: String,
    @SerialName("GPUCount") val gpuCount: String? = null,
    @SerialName("GpuCoreClock") val gpuCoreClock: String? = null,
    @SerialName("GpuMemoryClock") val gpuMemoryClock: String? = null,
    @SerialName("Comment") val comment: String? = null,
    @SerialName("ApiInfo") val apiInfo: String,
    @SerialName("ResizableBar") val resizableBar: String,
    @SerialName("WinGameMode") val winGameMode: String,
    @SerialName("HAGS") val hags: String,
    @SerialName("PresentationMode") val presentationMode: String,
    @SerialName("ResolutionInfo") val resolutionInfo: String? = null
)

@Serializable
data class CaptureRun(
    @SerialName("Hash") val hash: String,
    @SerialName("PresentMonRuntime") val presentMonRuntime: String,
    @SerialName("CaptureData") val captureData: CaptureData
)

@Serializable
data class CaptureData(
    @SerialName("TimeInSeconds") val timeInSeconds: List<Double>,
    @SerialName("MsBetweenPresents") val msBetweenPresents: List<Double>,
    @SerialName("MsInPresentAPI") val msInPresentAPI: List<Double>,
    @SerialName("MsUntilRenderComplete") val msUntilRenderComplete: List<Double>,
    @SerialName("MsUntilDisplayed") val msUntilDisplayed: List<Double>,
    @SerialName("MsBetweenDisplayChange") val msBetweenDisplayChange: List<Double>,
    @SerialName("AllowsTearing") val allowsTearing: List<Int>,
    @SerialName("PresentMode") val presentMode: List<Int>,
    @SerialName("SyncInterval") val syncInterval: List<Int>,
    @SerialName("Dropped") val dropped: List<Boolean>,
    @SerialName("QPCTime") val qpcTime: List<Double>,
    @SerialName("GpuActive") val gpuActive: List<Double>? = null,
    @SerialName("PcLatency") val pcLatency: List<Double>? = null
)
