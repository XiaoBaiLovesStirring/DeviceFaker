package com.devicefaker.model

data class DeviceProfile(
    val serialNumber: String = "",
    val macAddress: String = "",
    val androidId: String = "",
    val imei: String = "",
    val meid: String = "",
    val oaid: String = "",
    val phoneModel: String = "",
    val phoneBrand: String = "",
    val phoneManufacturer: String = "",
    val phoneDevice: String = "",
    val phoneProduct: String = "",
    val phoneHardware: String = "",
    val phoneFingerprint: String = "",
    val cpuModel: String = "",
    val cpuCores: Int = 8,
    val cpuArch: String = "arm64-v8a"
)

data class SpoofConfig(
    val targetPackage: String = "com.immomo.miraimind",
    val spoofSerial: Boolean = true,
    val spoofMac: Boolean = true,
    val spoofAndroidId: Boolean = true,
    val spoofImei: Boolean = true,
    val spoofMeid: Boolean = true,
    val spoofOaid: Boolean = true,
    val spoofPhoneModel: Boolean = true,
    val spoofCpuModel: Boolean = true,
    val networkIntercept: Boolean = true,
    val randomizeOnBoot: Boolean = true
)

data class NetworkRule(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val originalUrl: String = "",
    val redirectUrl: String = "",
    val modifyResponse: Boolean = false,
    val newResponseBody: String = "",
    val newStatusCode: Int = 200,
    val enabled: Boolean = true
)