package com.devicefaker

import com.devicefaker.hooks.*
import com.devicefaker.model.DeviceProfile
import com.devicefaker.model.SpoofConfig
import com.devicefaker.utils.RandomGenerator
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HookInit : IXposedHookLoadPackage {

    companion object {
        const val TAG = "DeviceFaker"

        @Volatile var currentProfile = DeviceProfile()
        @Volatile var currentConfig = SpoofConfig()

        val logLines = mutableListOf<String>()
        private val logLock = Any()

        fun log(msg: String) {
            val ts = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date())
            val line = "[$ts] $msg"
            synchronized(logLock) {
                logLines.add(line)
                if (logLines.size > 500) logLines.removeAt(0)
            }
            XposedBridge.log("$TAG: $msg")
        }

        fun getLogs(): List<String> = synchronized(logLock) { logLines.toList() }

        fun clearLogs() = synchronized(logLock) { logLines.clear() }
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val cfg = currentConfig
        if (lpparam.packageName != cfg.targetPackage) return
        if (lpparam.processName != lpparam.packageName) return

        // 如果配置了随机化，每次启动生成新配置
        if (cfg.randomizeOnBoot) {
            currentProfile = RandomGenerator.generateFullProfile()
        }

        val p = currentProfile
        val isActive = currentProfile.serialNumber.isNotEmpty()

        log("========================================")
        log("DeviceFaker Pro v2.0 已激活")
        log("目标: ${lpparam.packageName}")
        log("----------------------------------------")
        if (isActive) {
            log("SN: ${p.serialNumber}")
            log("IMEI: ${p.imei}")
            log("型号: ${p.phoneModel}")
            log("CPU: ${p.cpuModel}")
        }
        log("========================================")

        try {
            // 按开关注入 Hook
            SystemPropertyHook.hook(lpparam, cfg)
            DeviceInfoHook.hook(lpparam, cfg)
            TelephonyHook.hook(lpparam, cfg)
            NetworkHook.hook(lpparam, cfg)
            log("✓ 所有 Hook 注入完成")
        } catch (t: Throwable) {
            log("✗ Hook 注入失败: ${t.message}")
            XposedBridge.log(t)
        }
    }
}