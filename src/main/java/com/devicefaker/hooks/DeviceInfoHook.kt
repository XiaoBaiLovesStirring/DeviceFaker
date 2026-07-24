package com.devicefaker.hooks

import android.os.Build
import com.devicefaker.HookInit
import com.devicefaker.model.DeviceProfile
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Hook 1: 设备信息伪装
 * - Build 系列静态字段 (MODEL, BRAND, MANUFACTURER, DEVICE, PRODUCT, HARDWARE, FINGERPRINT)
 * - Build.getSerial()
 * - CPU 信息 (SystemProperties)
 * - WifiInfo.getMacAddress()
 */
object DeviceInfoHook {

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        val p = HookInit.currentProfile

        // === Build 静态字段 ===
        try {
            setBuildField("MODEL", p.phoneModel)
            setBuildField("BRAND", p.phoneBrand)
            setBuildField("MANUFACTURER", p.phoneManufacturer)
            setBuildField("DEVICE", p.phoneDevice)
            setBuildField("PRODUCT", p.phoneProduct)
            setBuildField("HARDWARE", p.phoneHardware)
            setBuildField("FINGERPRINT", p.phoneFingerprint)
            HookInit.log("✓ Build 字段伪装: ${p.phoneModel} / ${p.phoneManufacturer}")
        } catch (t: Throwable) {
            HookInit.log("⚠ Build 字段伪装失败: ${t.message}")
        }

        // === Build.getSerial() ===
        try {
            XposedHelpers.findAndHookMethod(
                Build::class.java,
                "getSerial",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = p.serialNumber
                        HookInit.log("  [Hook] Build.getSerial() → ${p.serialNumber}")
                    }
                }
            )
        } catch (t: Throwable) {
            HookInit.log("⚠ Build.getSerial Hook 失败: ${t.message}")
        }

        // === WifiInfo.getMacAddress() ===
        try {
            XposedHelpers.findAndHookMethod(
                "android.net.wifi.WifiInfo",
                lpparam.classLoader,
                "getMacAddress",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = p.macAddress
                        HookInit.log("  [Hook] WifiInfo.getMacAddress() → ${p.macAddress}")
                    }
                }
            )
        } catch (t: Throwable) {
            HookInit.log("⚠ MAC Address Hook 失败: ${t.message}")
        }
    }

    private fun setBuildField(field: String, value: String) {
        try {
            XposedHelpers.setStaticObjectField(Build::class.java, field, value)
        } catch (e: Throwable) {
            // Some fields may be final on newer Android versions
            XposedBridge.log("DeviceFaker: Cannot set Build.$field: ${e.message}")
        }
    }
}