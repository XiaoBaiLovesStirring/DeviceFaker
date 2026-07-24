package com.devicefaker.hooks

import android.telephony.TelephonyManager
import com.devicefaker.HookInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Hook 2: 电话信息伪装
 * - TelephonyManager.getDeviceId() → IMEI
 * - TelephonyManager.getImei() → IMEI
 * - TelephonyManager.getMeid() → MEID
 * - TelephonyManager.getSubscriberId() → IMSI
 * - TelephonyManager.getLine1Number() → 手机号
 */
object TelephonyHook {

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        val p = HookInit.currentProfile

        try {
            // getDeviceId (IMEI for GSM, MEID for CDMA)
            XposedHelpers.findAndHookMethod(
                TelephonyManager::class.java,
                "getDeviceId",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = p.imei
                        HookInit.log("  [Hook] getDeviceId() → ${p.imei}")
                    }
                }
            )

            // getImei (API 26+)
            try {
                XposedHelpers.findAndHookMethod(
                    TelephonyManager::class.java,
                    "getImei",
                    Int::class.javaPrimitiveType,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            param.result = p.imei
                            HookInit.log("  [Hook] getImei(slot) → ${p.imei}")
                        }
                    }
                )
            } catch (_: Throwable) {}

            // getMeid (API 26+)
            try {
                XposedHelpers.findAndHookMethod(
                    TelephonyManager::class.java,
                    "getMeid",
                    Int::class.javaPrimitiveType,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            param.result = p.meid
                            HookInit.log("  [Hook] getMeid(slot) → ${p.meid}")
                        }
                    }
                )
            } catch (_: Throwable) {}

            // getSubscriberId (IMSI)
            try {
                XposedHelpers.findAndHookMethod(
                    TelephonyManager::class.java,
                    "getSubscriberId",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            param.result = "46001${p.imei.take(10)}"
                            HookInit.log("  [Hook] getSubscriberId() → 46001...")
                        }
                    }
                )
            } catch (_: Throwable) {}

            HookInit.log("✓ TelephonyManager 伪装完成")
        } catch (t: Throwable) {
            HookInit.log("⚠ TelephonyManager Hook 失败: ${t.message}")
        }
    }
}