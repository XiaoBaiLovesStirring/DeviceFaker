package com.devicefaker.hooks

import android.telephony.TelephonyManager
import com.devicefaker.model.DeviceProfile
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * 电话信息伪装
 * 覆盖: IMEI, IMEI2, MEID, IMSI, Line1Number, SIM Serial, NetworkType
 */
object TelephonyHook {

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam, p: DeviceProfile) {
        try {
            // === getDeviceId() → IMEI (GSM) / MEID (CDMA) ===
            XposedHelpers.findAndHookMethod(
                TelephonyManager::class.java,
                "getDeviceId",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = p.imei
                    }
                }
            )

            // getDeviceId(slotIndex)
            try {
                XposedHelpers.findAndHookMethod(
                    TelephonyManager::class.java,
                    "getDeviceId",
                    Int::class.javaPrimitiveType,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            val slot = param.args[0] as Int
                            param.result = if (slot == 0) p.imei else p.imei2
                        }
                    }
                )
            } catch (_: Throwable) {}

            // === getImei(slot) ===
            try {
                XposedHelpers.findAndHookMethod(
                    TelephonyManager::class.java,
                    "getImei", Int::class.javaPrimitiveType,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            val slot = param.args[0] as Int
                            param.result = if (slot == 0) p.imei else p.imei2
                        }
                    }
                )
            } catch (_: Throwable) {}

            // getImei() no-arg
            try {
                XposedHelpers.findAndHookMethod(
                    TelephonyManager::class.java,
                    "getImei",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            param.result = p.imei
                        }
                    }
                )
            } catch (_: Throwable) {}

            // === getMeid(slot) ===
            try {
                XposedHelpers.findAndHookMethod(
                    TelephonyManager::class.java,
                    "getMeid", Int::class.javaPrimitiveType,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            param.result = p.meid
                        }
                    }
                )
            } catch (_: Throwable) {}

            try {
                XposedHelpers.findAndHookMethod(
                    TelephonyManager::class.java,
                    "getMeid",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            param.result = p.meid
                        }
                    }
                )
            } catch (_: Throwable) {}

            // === getSubscriberId() → IMSI ===
            try {
                XposedHelpers.findAndHookMethod(
                    TelephonyManager::class.java,
                    "getSubscriberId",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            param.result = p.imsi
                        }
                    }
                )
            } catch (_: Throwable) {}

            try {
                XposedHelpers.findAndHookMethod(
                    TelephonyManager::class.java,
                    "getSubscriberId", Int::class.javaPrimitiveType,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            param.result = p.imsi
                        }
                    }
                )
            } catch (_: Throwable) {}

            // === getLine1Number() ===
            try {
                XposedHelpers.findAndHookMethod(
                    TelephonyManager::class.java,
                    "getLine1Number",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            param.result = ""
                        }
                    }
                )
            } catch (_: Throwable) {}

            // === getSimSerialNumber() ===
            try {
                XposedHelpers.findAndHookMethod(
                    TelephonyManager::class.java,
                    "getSimSerialNumber",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            param.result = "89860${p.imei.take(13)}"
                        }
                    }
                )
            } catch (_: Throwable) {}

            // === getNetworkOperatorName() ===
            try {
                XposedHelpers.findAndHookMethod(
                    TelephonyManager::class.java,
                    "getNetworkOperatorName",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            param.result = "中国移动"
                        }
                    }
                )
            } catch (_: Throwable) {}

            // === getSimOperatorName() ===
            try {
                XposedHelpers.findAndHookMethod(
                    TelephonyManager::class.java,
                    "getSimOperatorName",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            param.result = "中国移动"
                        }
                    }
                )
            } catch (_: Throwable) {}

            XposedBridge.log("  TelephonyManager 已拦截")
        } catch (t: Throwable) {
            XposedBridge.log("  TelephonyManager 拦截失败: ${t.message}")
        }
    }
}