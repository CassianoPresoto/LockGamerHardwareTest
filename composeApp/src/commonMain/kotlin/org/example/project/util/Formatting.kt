package org.example.project.util

import kotlin.math.pow
import kotlin.math.round

fun formatNumber(value: Double, decimals: Int = 2): String {
    if (value.isNaN() || value.isInfinite()) return "0".let { if (decimals > 0) "$it.${"0".repeat(decimals)}" else it }
    val factor = 10.0.pow(decimals)
    val rounded = round(value * factor) / factor
    val asString = rounded.toString()
    val parts = asString.split(".")
    val intPart = parts[0]
    if (decimals <= 0) return intPart
    val fracPart = if (parts.size > 1) parts[1] else ""
    val fixed = (fracPart + "0".repeat(decimals)).take(decimals)
    return "$intPart.$fixed"
}
