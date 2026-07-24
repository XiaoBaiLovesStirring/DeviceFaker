package com.devicefaker.utils

import kotlin.random.Random

object RandomGenerator {

    private val hex = "0123456789abcdef"
    private val alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

    fun generateSerialNumber(): String = buildString {
        repeat(10) { append(alpha[Random.nextInt(alpha.length)]) }
    }

    fun generateMacAddress(): String = buildString {
        for (i in 0..5) {
            if (i > 0) append(":")
            append(hex[Random.nextInt(16)])
            append(hex[Random.nextInt(16)])
        }
    }

    fun generateAndroidId(): String = buildString {
        repeat(16) { append(hex[Random.nextInt(16)]) }
    }

    fun generateImei(): String {
        // 生成合法Luhn校验的IMEI
        val sb = StringBuilder()
        repeat(14) { sb.append(Random.nextInt(10)) }
        // Luhn checksum
        var sum = 0
        for (i in 0..13) {
            var digit = sb[i].digitToInt()
            if (i % 2 == 0) {
                digit *= 2
                if (digit > 9) digit -= 9
            }
            sum += digit
        }
        val checkDigit = (10 - (sum % 10)) % 10
        sb.append(checkDigit)
        return sb.toString()
    }

    fun generateMeid(): String = buildString {
        repeat(14) { append(hex[Random.nextInt(16)]) }
    }

    fun generateImsi(): String = buildString {
        append("460") // MCC-MNC: China
        repeat(12) { append(Random.nextInt(10)) }
    }

    fun generateOaid(): String = buildString {
        repeat(8) { append(hex[Random.nextInt(16)]) }; append("-")
        repeat(4) { append(hex[Random.nextInt(16)]) }; append("-")
        repeat(4) { append(hex[Random.nextInt(16)]) }; append("-")
        repeat(4) { append(hex[Random.nextInt(16)]) }; append("-")
        repeat(12) { append(hex[Random.nextInt(16)]) }
    }

    private val phoneModels = listOf(
        "SM-S9280", "SM-S9180", "SM-S9080", "SM-G9980",
        "SM-S9260", "SM-S9160", "SM-S9060", "SM-G9960",
        "SM-F9460", "SM-F9360", "SM-F9260", "SM-N9860",
        "Pixel 8 Pro", "Pixel 7 Pro", "Pixel 6 Pro",
        "Xiaomi 14 Ultra", "Xiaomi 13 Pro", "Xiaomi 12S Ultra",
        "OPPO Find X7 Ultra", "vivo X100 Pro", "OnePlus 12"
    )

    fun generatePhoneModel(): String = phoneModels[Random.nextInt(phoneModels.size)]

    fun generatePhoneBrand(model: String): String = when {
        model.startsWith("SM-") -> "samsung"
        model.startsWith("Pixel") -> "google"
        model.startsWith("Xiaomi") -> "Xiaomi"
        model.startsWith("OPPO") -> "OPPO"
        model.startsWith("vivo") -> "vivo"
        model.startsWith("OnePlus") -> "OnePlus"
        else -> "samsung"
    }

    fun generateManufacturer(brand: String): String = when (brand) {
        "samsung" -> "samsung"
        "google" -> "Google"
        "Xiaomi" -> "Xiaomi"
        "OPPO" -> "OPPO"
        "vivo" -> "vivo"
        "OnePlus" -> "OnePlus"
        else -> "samsung"
    }

    fun generateDevice(model: String): String = model.lowercase()

    fun generateProduct(model: String): String = model.lowercase().replace(" ", "_") + "xxx"

    fun generateFingerprint(brand: String, model: String): String {
        val device = model.lowercase().replace(" ", "_")
        return "$brand/$device/$device:14/UP1A.231005.007/${model.substring(0..4).uppercase()}CU1AXK5:user/release-keys"
    }

    private val cpuModels = listOf(
        "Qualcomm Snapdragon 8 Gen 3",
        "Qualcomm Snapdragon 8 Gen 2",
        "Qualcomm Snapdragon 8+ Gen 1",
        "MediaTek Dimensity 9300",
        "MediaTek Dimensity 9200+",
        "Samsung Exynos 2400",
        "Google Tensor G3",
        "Google Tensor G2"
    )

    fun generateCpuModel(): String = cpuModels[Random.nextInt(cpuModels.size)]

    fun generateCpuArch(): String = "arm64-v8a"

    fun generateCpuAbiList(): String = "arm64-v8a,armeabi-v7a,armeabi"

    /**
     * 生成完整的随机 DeviceProfile
     */
    fun generateFullProfile(): com.devicefaker.model.DeviceProfile {
        val model = generatePhoneModel()
        val brand = generatePhoneBrand(model)
        return com.devicefaker.model.DeviceProfile(
            serialNumber = generateSerialNumber(),
            macAddress = generateMacAddress(),
            bluetoothMac = generateMacAddress(),
            androidId = generateAndroidId(),
            imei = generateImei(),
            imei2 = generateImei(),
            meid = generateMeid(),
            imsi = generateImsi(),
            oaid = generateOaid(),
            phoneModel = model,
            phoneBrand = brand,
            phoneManufacturer = generateManufacturer(brand),
            phoneDevice = generateDevice(model),
            phoneProduct = generateProduct(model),
            phoneHardware = "qcom",
            phoneFingerprint = generateFingerprint(brand, model),
            cpuModel = generateCpuModel(),
            cpuCores = 8,
            cpuArch = generateCpuArch(),
            cpuAbiList = generateCpuAbiList()
        )
    }
}