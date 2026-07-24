package com.devicefaker.model

import java.util.UUID

/**
 * 设备伪装配置
 */
data class DeviceProfile(
    val serialNumber: String = "",
    val macAddress: String = "",
    val bluetoothMac: String = "",
    val androidId: String = "",
    val imei: String = "",
    val imei2: String = "",
    val meid: String = "",
    val imsi: String = "",
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
    val cpuArch: String = "arm64-v8a",
    val cpuAbiList: String = "arm64-v8a,armeabi-v7a,armeabi",
    val cpuAbiList32: String = "armeabi-v7a,armeabi",
    val cpuAbiList64: String = "arm64-v8a"
)

/**
 * 伪装开关总控
 */
data class SpoofConfig(
    val targetPackage: String = "com.immomo.miraimind",
    val spoofSerial: Boolean = true,
    val spoofMac: Boolean = true,
    val spoofBluetoothMac: Boolean = true,
    val spoofAndroidId: Boolean = true,
    val spoofImei: Boolean = true,
    val spoofMeid: Boolean = true,
    val spoofImsi: Boolean = true,
    val spoofOaid: Boolean = true,
    val spoofPhoneModel: Boolean = true,
    val spoofCpuModel: Boolean = true,
    val networkIntercept: Boolean = true,
    val randomizeOnBoot: Boolean = true
)

/**
 * 网络拦截规则
 */
data class NetworkRule(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val urlPattern: String = "",
    val action: RuleAction = RuleAction.REDIRECT,
    val redirectUrl: String = "",
    val modifyResponse: Boolean = false,
    val newResponseBody: String = "",
    val newStatusCode: Int = 200,
    val enabled: Boolean = true
)

enum class RuleAction {
    REDIRECT,       // 重定向到新URL
    MODIFY_RESPONSE, // 篡改响应体
    BLOCK            // 阻断请求
}

/**
 * 篡改后的响应
 */
data class TamperedResponse(
    val statusCode: Int,
    val body: String,
    val contentType: String = "application/json"
)