package com.devicefaker.utils

import kotlin.random.Random

object RandomGenerator {

    private val hexChars = "0123456789abcdef"
    private val alphanumeric = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

    fun generateSerialNumber(): String = buildString {
        repeat(10) { append(alphanumeric[Random.nextInt(alphanumeric.length)]) }
    }

    fun generateMacAddress(): String = buildString {
        for (i in 0..5) {
            if (i > 0) append(":")
            append(hexChars[Random.nextInt(16)])
            append(hexChars[Random.nextInt(16)])
        }
    }

    fun generateAndroidId(): String = buildString {
        repeat(16) { append(hexChars[Random.nextInt(16)]) }
    }

    fun generateImei(): String = buildString {
        repeat(15) { append(Random.nextInt(10)) }
    }

    fun generateMeid(): String = buildString {
        repeat(14) { append(hexChars[Random.nextInt(16)]) }
    }

    fun generateOaid(): String = buildString {
        // OAID format: UUID-like
        append(hexCharsChunk(8)); append("-")
        append(hexCharsChunk(4)); append("-")
        append(hexCharsChunk(4)); append("-")
        append(hexCharsChunk(4)); append("-")
        append(hexCharsChunk(12))
    }

    private fun hexCharsChunk(length: Int): String = buildString {
        repeat(length) { append(hexChars[Random.nextInt(16)]) }
    }

    fun generatePhoneModel(): String {
        val models = listOf(
            "SM-S9280", "SM-S9180", "SM-S9080", "SM-G9980",
            "SM-S9260", "SM-S9160", "SM-S9060", "SM-G9960",
            "SM-F9460", "SM-F9360", "SM-F9260", "SM-N9860"
        )
        return models[Random.nextInt(models.size)]
    }

    fun generateCpuModel(): String {
        val cpuModels = listOf(
            "Qualcomm Snapdragon 8 Gen 3",
            "Qualcomm Snapdragon 8 Gen 2",
            "MediaTek Dimensity 9300",
            "MediaTek Dimensity 9200+",
            "Samsung Exynos 2400",
            "Google Tensor G3"
        )
        return cpuModels[Random.nextInt(cpuModels.size)]
    }
}