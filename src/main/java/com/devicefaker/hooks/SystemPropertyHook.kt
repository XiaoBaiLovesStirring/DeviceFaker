package com.devicefaker.hooks

import com.devicefaker.HookInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Hook 3: 系统属性伪装
 * - SystemProperties.get() → 序列号/CPU 型号
 * - Settings.Secure.getString() → Android ID
 * - OAID/AAID 相关
 */
object SystemPropertyHook {

    private val spoofedProps = mapOf(
        "ro.serialno" to { HookInit.currentProfile.serialNumber },
        "ro.boot.serialno" to { HookInit.currentProfile.serialNumber },
        "ro.product.model" to { HookInit.currentProfile.phoneModel },
        "ro.product.brand" to { HookInit.currentProfile.phoneBrand },
        "ro.product.manufacturer" to { HookInit.currentProfile.phoneManufacturer },
        "ro.product.device" to { HookInit.currentProfile.phoneDevice },
        "ro.product.name" to { HookInit.currentProfile.phoneProduct },
        "ro.hardware" to { HookInit.currentProfile.phoneHardware },
        "ro.board.platform" to { "lahaina" },
        "ro.chipname" to { HookInit.currentProfile.cpuModel },
        "ro.product.cpu.abi" to { HookInit.currentProfile.cpuArch },
        "ro.product.cpu.abilist" to { HookInit.currentProfile.cpuArch },
        "ro.product.cpu.abilist64" to { HookInit.currentProfile.cpuArch }
    )

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        val p = HookInit.currentProfile

        // === SystemProperties.get() ===
        try {
            XposedHelpers.findAndHookMethod(
                "android.os.SystemProperties",
                lpparam.classLoader,
                "get",
                String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val key = param.args[0] as? String ?: return
                        spoofedProps[key]?.let { generator ->
                            val newValue = generator()
                            param.result = newValue
                            HookInit.log("  [Hook] SystemProperties.get($key) → $newValue")
                        }
                    }
                }
            )

            // SystemProperties.get(key, default)
            XposedHelpers.findAndHookMethod(
                "android.os.SystemProperties",
                lpparam.classLoader,
                "get",
                String::class.java,
                String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val key = param.args[0] as? String ?: return
                        spoofedProps[key]?.let { generator ->
                            param.result = generator()
                        }
                    }
                }
            )
        } catch (t: Throwable) {
            HookInit.log("⚠ SystemProperties Hook 失败: ${t.message}")
        }

        // === Settings.Secure.getString → Android ID ===
        try {
            XposedHelpers.findAndHookMethod(
                "android.provider.Settings.Secure",
                lpparam.classLoader,
                "getString",
                android.content.ContentResolver::class.java,
                String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val name = param.args[1] as? String ?: return
                        if (name == "android_id") {
                            param.result = p.androidId
                            HookInit.log("  [Hook] Settings.Secure.getString(android_id) → ${p.androidId}")
                        }
                    }
                }
            )
        } catch (t: Throwable) {
            HookInit.log("⚠ Android ID Hook 失败: ${t.message}")
        }

        // === OAID/AAID 拦截 ===
        hookOaid(lpparam, p)

        HookInit.log("✓ SystemProperties & Android ID 伪装完成")
    }

    private fun hookOaid(lpparam: XC_LoadPackage.LoadPackageParam, p: com.devicefaker.model.DeviceProfile) {
        // 华为 OAID
        try {
            XposedHelpers.findAndHookMethod(
                "com.huawei.hms.ads.identifier.AdvertisingIdClient",
                lpparam.classLoader,
                "getAdvertisingIdInfo",
                android.content.Context::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val info = param.result
                        if (info != null) {
                            try {
                                XposedHelpers.callMethod(info, "getId")
                                // 构造新的 OAID 返回
                                val fakeInfo = XposedHelpers.newInstance(
                                    XposedHelpers.findClass(
                                        "com.huawei.hms.ads.identifier.AdvertisingIdClient\$Info",
                                        lpparam.classLoader
                                    ),
                                    p.oaid,
                                    java.lang.Boolean.FALSE
                                )
                                param.result = fakeInfo
                                HookInit.log("  [Hook] 华为 OAID → ${p.oaid}")
                            } catch (_: Throwable) {}
                        }
                    }
                }
            )
        } catch (_: Throwable) {}

        // 小米 OAID
        try {
            XposedHelpers.findAndHookMethod(
                "com.android.id.impl.IdProviderImpl",
                lpparam.classLoader,
                "getOAID",
                android.content.Context::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = p.oaid
                        HookInit.log("  [Hook] 小米 OAID → ${p.oaid}")
                    }
                }
            )
        } catch (_: Throwable) {}

        // 通用 OAID (MSA SDK)
        try {
            XposedHelpers.findAndHookMethod(
                "com.bun.miitmdid.core.MdidSdkHelper",
                lpparam.classLoader,
                "InitSdk",
                android.content.Context::class.java,
                java.lang.Boolean::class.java,
                Any::class.java,  // IIdentifierListener
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        HookInit.log("  [Hook] MSA OAID SDK 初始化已拦截")
                    }
                }
            )
        } catch (_: Throwable) {}

        // OPPO/VIVO OAID
        try {
            XposedHelpers.findAndHookMethod(
                "com.heytap.openid.sdk.IdentifyService",
                lpparam.classLoader,
                "getOuid",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = p.oaid
                        HookInit.log("  [Hook] OPPO OAID → ${p.oaid}")
                    }
                }
            )
        } catch (_: Throwable) {}
    }
}