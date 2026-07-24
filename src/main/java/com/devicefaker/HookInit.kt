package com.devicefaker

import com.devicefaker.hooks.*
import com.devicefaker.utils.RandomGenerator
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HookInit : IXposedHookLoadPackage {

    companion object {
        const val TAG = "DeviceFaker"
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val cfg = DeviceState.currentConfig
        if (lpparam.packageName != cfg.targetPackage) return
        if (lpparam.processName != lpparam.packageName) return

        if (cfg.randomizeOnBoot) {
            DeviceState.currentProfile = RandomGenerator.generateFullProfile()
        }

        val p = DeviceState.currentProfile

        DeviceState.log("========================================")
        DeviceState.log("DeviceFaker Pro v2.0 已激活")
        DeviceState.log("目标: ${lpparam.packageName}")
        DeviceState.log("----------------------------------------")
        if (p.serialNumber.isNotEmpty()) {
            DeviceState.log("SN: ${p.serialNumber}")
            DeviceState.log("IMEI: ${p.imei}")
            DeviceState.log("型号: ${p.phoneModel}")
            DeviceState.log("CPU: ${p.cpuModel}")
        }
        DeviceState.log("========================================")

        try {
            SystemPropertyHook.hook(lpparam, cfg)
            DeviceInfoHook.hook(lpparam, cfg)
            TelephonyHook.hook(lpparam, cfg)
            NetworkHook.hook(lpparam, cfg)
            DeviceState.log("✓ 所有 Hook 注入完成")
        } catch (t: Throwable) {
            DeviceState.log("✗ Hook 注入失败: ${t.message}")
            XposedBridge.log(t)
        }
    }
}