package com.example.frienddb.util

import java.util.Random

object ColorUtils {
    private val PALETTE = intArrayOf(
        0xFFE91E63.toInt(),
        0xFF9C27B0.toInt(),
        0xFF3F51B5.toInt(),
        0xFF2196F3.toInt(),
        0xFF009688.toInt(),
        0xFF4CAF50.toInt(),
        0xFFCDDC39.toInt(),
        0xFFFF9800.toInt(),
        0xFFF44336.toInt(),
        0xFF795548.toInt(),
        0xFF607D8B.toInt(),
        0xFFFF5722.toInt(),
    )
    private val rng = Random()
    fun randomArgb(): Int = PALETTE[rng.nextInt(PALETTE.size)]
}
