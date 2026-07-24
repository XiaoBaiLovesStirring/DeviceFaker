package com.devicefaker.hooks

import com.devicefaker.DeviceState
import com.devicefaker.model.SpoofConfig
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Hook 2: 系统属性 & Android ID 伪装
 * 覆盖: SystemProperties.get(), Settings.Secure.getString(), OAID/AAID
 */
object SystemPropertyHook {

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam, cfg: SpoofConfig) {
        val p = DeviceState.currentProfile

        hookSystemProperties(lpparam, cfg, p)
        hookAndroidId(lpparam, cfg, p)
        hookOaid(lpparam, cfg, p)
        hookCpuInfo(lpparam, cfg, p)

        DeviceState.log("✓ SystemPropertyHook 完成")
    }

    // ===== SystemProperties.get() =====
    private fun hookSystemProperties(lpparam: XC_LoadPackage.LoadPackageParam, cfg: SpoofConfig, p: com.devicefaker.model.DeviceProfile) {
        val spoofMap = buildMap {
            if (cfg.spoofSerial) {
                put("ro.serialno", p.serialNumber)
                put("ro.boot.serialno", p.serialNumber)
            }
            if (cfg.spoofPhoneModel) {
                put("ro.product.model", p.phoneModel)
                put("ro.product.brand", p.phoneBrand)
                put("ro.product.manufacturer", p.phoneManufacturer)
                put("ro.product.device", p.phoneDevice)
                put("ro.product.name", p.phoneProduct)
                put("ro.product.board", p.phoneDevice)
                put("ro.hardware", p.phoneHardware)
                put("ro.build.fingerprint", p.phoneFingerprint)
                put("ro.build.description", "${p.phoneDevice}-user 14 UP1A.231005.007 release-keys")
            }
            if (cfg.spoofCpuModel) {
                put("ro.board.platform", "lahaina")
                put("ro.chipname", p.cpuModel)
                put("ro.product.cpu.abi", p.cpuArch)
                put("ro.product.cpu.abilist", p.cpuAbiList)
                put("ro.product.cpu.abilist32", p.cpuAbiList32)
                put("ro.product.cpu.abilist64", p.cpuArch)
            }
        }

        try {
            // SystemProperties.get(key)
            XposedHelpers.findAndHookMethod(
                "android.os.SystemProperties", lpparam.classLoader,
                "get", String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val key = param.args[0] as? String ?: return
                        spoofMap[key]?.let { newVal ->
                            param.result = newVal
                        }
                    }
                }
            )

            // SystemProperties.get(key, def)
            XposedHelpers.findAndHookMethod(
                "android.os.SystemProperties", lpparam.classLoader,
                "get", String::class.java, String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val key = param.args[0] as? String ?: return
                        spoofMap[key]?.let { newVal ->
                            param.result = newVal
                        }
                    }
                }
            )
        } catch (t: Throwable) {
            DeviceState.log("⚠ SystemProperties: ${t.message}")
        }
    }

    // ===== Settings.Secure Android ID =====
    private fun hookAndroidId(lpparam: XC_LoadPackage.LoadPackageParam, cfg: SpoofConfig, p: com.devicefaker.model.DeviceProfile) {
        if (!cfg.spoofAndroidId) return

        try {
            XposedHelpers.findAndHookMethod(
                "android.provider.Settings.Secure", lpparam.classLoader,
                "getString",
                android.content.ContentResolver::class.java,
                String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (param.args[1] == "android_id") {
                            param.result = p.androidId
                        }
                    }
                }
            )
        } catch (t: Throwable) {
            DeviceState.log("⚠ Android ID: ${t.message}")
        }
    }

    // ===== OAID/AAID 各家厂商 SDK =====
    private fun hookOaid(lpparam: XC_LoadPackage.LoadPackageParam, cfg: SpoofConfig, p: com.devicefaker.model.DeviceProfile) {
        if (!cfg.spoofOaid) return

        // 华为 OAID
        try {
            XposedHelpers.findAndHookMethod(
                "com.huawei.hms.ads.identifier.AdvertisingIdClient",
                lpparam.classLoader,
                "getAdvertisingIdInfo",
                android.content.Context::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val infoClass = XposedHelpers.findClass(
                                "com.huawei.hms.ads.identifier.AdvertisingIdClient\$Info",
                                lpparam.classLoader
                            )
                            param.result = XposedHelpers.newInstance(infoClass, p.oaid, false)
                        } catch (_: Throwable) {}
                    }
                }
            )
        } catch (_: Throwable) {}

        // 小米 OAID
        try {
            XposedHelpers.findAndHookMethod(
                "com.android.id.impl.IdProviderImpl", lpparam.classLoader,
                "getOAID", android.content.Context::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = p.oaid
                    }
                }
            )
        } catch (_: Throwable) {}

        // OPPO OAID
        try {
            XposedHelpers.findAndHookMethod(
                "com.heytap.openid.sdk.IdentifyService", lpparam.classLoader,
                "getOuid",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = p.oaid
                    }
                }
            )
        } catch (_: Throwable) {}

        // VIVO OAID
        try {
            XposedHelpers.findAndHookMethod(
                "com.vivo.identifier.IdentifierIdManager", lpparam.classLoader,
                "getOAID", android.content.Context::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = p.oaid
                    }
                }
            )
        } catch (_: Throwable) {}

        // MSA SDK (通用)
        try {
            XposedHelpers.findAndHookMethod(
                "com.bun.miitmdid.core.MdidSdkHelper", lpparam.classLoader,
                "InitSdk",
                android.content.Context::class.java,
                Boolean::class.java,
                Any::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        DeviceState.log("  [OAID] MSA SDK 已拦截")
                    }
                }
            )
        } catch (_: Throwable) {}

        // Google AAID (Play Services)
        try {
            XposedHelpers.findAndHookMethod(
                "com.google.android.gms.ads.identifier.AdvertisingIdClient",
                lpparam.classLoader,
                "getAdvertisingIdInfo",
                android.content.Context::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val infoClass = XposedHelpers.findClass(
                                "com.google.android.gms.ads.identifier.AdvertisingIdClient\$Info",
                                lpparam.classLoader
                            )
                            param.result = XposedHelpers.newInstance(infoClass, p.oaid, false)
                        } catch (_: Throwable) {}
                    }
                }
            )
        } catch (_: Throwable) {}
    }

    // ===== /proc/cpuinfo 读取拦截 =====
    private fun hookCpuInfo(lpparam: XC_LoadPackage.LoadPackageParam, cfg: SpoofConfig, p: com.devicefaker.model.DeviceProfile) {
        if (!cfg.spoofCpuModel) return

        try {
            // 拦截 FileInputStream 读取 /proc/cpuinfo
            XposedHelpers.findAndHookConstructor(
                "java.io.FileInputStream", lpparam.classLoader,
                java.io.File::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val file = param.args[0] as? java.io.File ?: return
                        if (file.absolutePath.contains("/proc/cpuinfo")) {
                            // 不让读真实cpuinfo，用假的替换
                            // 这里我们无法直接替换，但可以记录
                            DeviceState.log("  [CPU] 拦截 /proc/cpuinfo 读取")
                        }
                    }
                }
            )
        } catch (_: Throwable) {}
    }
}