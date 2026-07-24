package com.devicefaker.hooks

import android.os.Build
import com.devicefaker.HookInit
import com.devicefaker.model.SpoofConfig
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.net.NetworkInterface

/**
 * Hook 1: 设备信息伪装
 * 覆盖: Build.* 字段, Serial, MAC, Bluetooth MAC, NetworkInterface MAC
 */
object DeviceInfoHook {

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam, cfg: SpoofConfig) {
        val p = HookInit.currentProfile

        // === Build 静态字段 (通过 SystemProperties 底层拦截来实现) ===
        // 说明: Build 字段在类加载时从 SystemProperties 读取，SystemPropertyHook 已拦截
        // 这里额外尝试直接设置，作为双保险
        if (cfg.spoofPhoneModel) {
            trySetBuildField("MODEL", p.phoneModel)
            trySetBuildField("BRAND", p.phoneBrand)
            trySetBuildField("MANUFACTURER", p.phoneManufacturer)
            trySetBuildField("DEVICE", p.phoneDevice)
            trySetBuildField("PRODUCT", p.phoneProduct)
            trySetBuildField("HARDWARE", p.phoneHardware)
            trySetBuildField("FINGERPRINT", p.phoneFingerprint)
            trySetBuildField("SUPPORTED_ABIS", arrayOf(p.cpuArch, "armeabi-v7a", "armeabi"))
            trySetBuildField("SUPPORTED_32_BIT_ABIS", arrayOf("armeabi-v7a", "armeabi"))
            trySetBuildField("SUPPORTED_64_BIT_ABIS", arrayOf(p.cpuArch))
            HookInit.log("✓ Build 字段: ${p.phoneModel} / ${p.phoneManufacturer}")
        }

        // === Build.getSerial() ===
        if (cfg.spoofSerial) {
            try {
                XposedHelpers.findAndHookMethod(
                    Build::class.java, "getSerial",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            param.result = p.serialNumber
                        }
                    }
                )
            } catch (t: Throwable) {
                HookInit.log("⚠ Build.getSerial: ${t.message}")
            }
        }

        // === WifiInfo.getMacAddress() ===
        if (cfg.spoofMac) {
            try {
                XposedHelpers.findAndHookMethod(
                    "android.net.wifi.WifiInfo", lpparam.classLoader,
                    "getMacAddress",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            param.result = p.macAddress
                        }
                    }
                )
            } catch (t: Throwable) {
                HookInit.log("⚠ Wifi MAC: ${t.message}")
            }
        }

        // === BluetoothAdapter.getAddress() ===
        if (cfg.spoofBluetoothMac) {
            try {
                XposedHelpers.findAndHookMethod(
                    "android.bluetooth.BluetoothAdapter", lpparam.classLoader,
                    "getAddress",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            param.result = p.bluetoothMac
                        }
                    }
                )
            } catch (t: Throwable) {
                HookInit.log("⚠ BT MAC: ${t.message}")
            }
        }

        // === NetworkInterface.getHardwareAddress() ===
        if (cfg.spoofMac) {
            try {
                XposedHelpers.findAndHookMethod(
                    NetworkInterface::class.java,
                    "getHardwareAddress",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            try {
                                val ni = param.thisObject as NetworkInterface
                                val name = ni.name ?: ""
                                // 只伪装 wlan 接口
                                if (name.startsWith("wlan")) {
                                    val macBytes = p.macAddress.split(":").map {
                                        it.toInt(16).toByte()
                                    }.toByteArray()
                                    param.result = macBytes
                                }
                            } catch (_: Throwable) {}
                        }
                    }
                )
            } catch (t: Throwable) {
                HookInit.log("⚠ NetworkInterface: ${t.message}")
            }
        }

        HookInit.log("✓ DeviceInfoHook 完成")
    }

    private fun trySetBuildField(field: String, value: Any) {
        try {
            XposedHelpers.setStaticObjectField(Build::class.java, field, value)
        } catch (_: Throwable) {}
    }
}