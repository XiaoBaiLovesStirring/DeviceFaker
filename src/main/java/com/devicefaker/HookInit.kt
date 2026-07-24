package com.devicefaker

import android.content.Context
import com.devicefaker.hooks.*
import com.devicefaker.model.DeviceProfile
import com.devicefaker.model.SpoofConfig
import com.devicefaker.utils.RandomGenerator
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HookInit : IXposedHookLoadPackage {

    companion object {
        const val TAG = "DeviceFaker"
        var currentProfile = DeviceProfile()
        var currentConfig = SpoofConfig()
        var logLines = mutableListOf<String>()
        private var isHooked = false

        fun log(msg: String) {
            val ts = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date())
            val line = "[$ts] $msg"
            synchronized(logLines) {
                logLines.add(line)
                if (logLines.size > 500) logLines.removeAt(0)
            }
            XposedBridge.log("$TAG: $msg")
        }
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != currentConfig.targetPackage) return
        if (isHooked) return

        // 生成随机设备配置
        currentProfile = DeviceProfile(
            serialNumber = RandomGenerator.generateSerialNumber(),
            macAddress = RandomGenerator.generateMacAddress(),
            androidId = RandomGenerator.generateAndroidId(),
            imei = RandomGenerator.generateImei(),
            meid = RandomGenerator.generateMeid(),
            oaid = RandomGenerator.generateOaid(),
            phoneModel = RandomGenerator.generatePhoneModel(),
            phoneBrand = "samsung",
            phoneManufacturer = "samsung",
            phoneDevice = "SM-S9280",
            phoneProduct = "e3qxxx",
            phoneHardware = "qcom",
            phoneFingerprint = "samsung/e3qxxx/e3q:14/UP1A.231005.007/S9280ZCU1AXK5:user/release-keys",
            cpuModel = RandomGenerator.generateCpuModel(),
            cpuCores = 8,
            cpuArch = "arm64-v8a"
        )

        isHooked = true
        log("========================================")
        log("DeviceFaker Pro v2.0 已激活")
        log("目标: ${lpparam.packageName} | 进程: ${lpparam.processName}")
        log("序列号: ${currentProfile.serialNumber}")
        log("IMEI: ${currentProfile.imei}")
        log("型号: ${currentProfile.phoneModel}")
        log("CPU: ${currentProfile.cpuModel}")

        // 注入所有 Hook
        try {
            SystemPropertyHook.hook(lpparam)
            TelephonyHook.hook(lpparam)
            DeviceInfoHook.hook(lpparam)
            NetworkHook.hook(lpparam)
            log("✓ 所有 Hook 注入完成 (7/7)")
        } catch (t: Throwable) {
            log("✗ Hook 注入失败: ${t.message}")
            XposedBridge.log(t)
        }
    }
}