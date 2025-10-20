package org.example.project.data

import kotlinx.serialization.Serializable

@Serializable
data class PerformanceStats(
    val avgFps: Double,
    val minFps: Double,
    val maxFps: Double,
    val percentile1: Double,
    val percentile5: Double,
    val percentile95: Double,
    val percentile99: Double,
    val frameTimeAvg: Double,
    val frameTimeMin: Double,
    val frameTimeMax: Double,
    val totalFrames: Int,
    val duration: Double
) {
    companion object {
        fun fromCaptureData(data: CaptureData): PerformanceStats {
            val frameTimes = data.MsBetweenPresents
            val fps = frameTimes.map { if (it > 0) 1000.0 / it else 0.0 }
            
            val sortedFps = fps.sorted()
            val sortedFrameTimes = frameTimes.sorted()
            
            return PerformanceStats(
                avgFps = fps.average(),
                minFps = sortedFps.firstOrNull() ?: 0.0,
                maxFps = sortedFps.lastOrNull() ?: 0.0,
                percentile1 = percentile(sortedFps, 1.0),
                percentile5 = percentile(sortedFps, 5.0),
                percentile95 = percentile(sortedFps, 95.0),
                percentile99 = percentile(sortedFps, 99.0),
                frameTimeAvg = frameTimes.average(),
                frameTimeMin = sortedFrameTimes.firstOrNull() ?: 0.0,
                frameTimeMax = sortedFrameTimes.lastOrNull() ?: 0.0,
                totalFrames = frameTimes.size,
                duration = data.TimeInSeconds.lastOrNull() ?: 0.0
            )
        }
        
        private fun percentile(sortedList: List<Double>, percentile: Double): Double {
            if (sortedList.isEmpty()) return 0.0
            val index = ((percentile / 100.0) * (sortedList.size - 1)).toInt()
            return sortedList.getOrNull(index) ?: 0.0
        }
    }
}
