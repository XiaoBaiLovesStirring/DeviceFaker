package com.devicefaker

import com.devicefaker.hooks.NetworkHook
import com.devicefaker.hooks.TelephonyHook
import com.devicefaker.model.DeviceProfile
import com.devicefaker.utils.RandomGenerator
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import android.os.Build

class HookInit : IXposedHookLoadPackage {

    companion object {
        const val TAG = "DeviceFaker"
        private var profileInitialized = false

        // 预生成伪造配置，确保所有 Hook 使用同一份数据
        @Volatile var fakeProfile = DeviceProfile()
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // 只 Hook 主进程
        if (lpparam.processName != lpparam.packageName) return

        // 只在第一次加载时生成配置
        synchronized(this) {
            if (!profileInitialized) {
                fakeProfile = RandomGenerator.generateFullProfile()
                profileInitialized = true
            }
        }

        val p = fakeProfile

        XposedBridge.log("=".repeat(48))
        XposedBridge.log("DeviceFaker Pro v2.2 (API 102)")
        XposedBridge.log("目标进程: ${lpparam.packageName}")
        XposedBridge.log("-".repeat(48))
        XposedBridge.log("SN:    ${p.serialNumber}")
        XposedBridge.log("MODEL: ${p.phoneModel}")
        XposedBridge.log("BRAND: ${p.phoneBrand}")
        XposedBridge.log("MFR:   ${p.phoneManufacturer}")
        XposedBridge.log("IMEI:  ${p.imei}")
        XposedBridge.log("MAC:   ${p.macAddress}")
        XposedBridge.log("AID:   ${p.androidId}")
        XposedBridge.log("CPU:   ${p.cpuModel}")
        XposedBridge.log("=".repeat(48))

        // ====== 先修改 Build 静态字段（最优先） ======
        applyBuildFieldSpoofing(p)

        // ====== 再安装方法级 Hook ======
        try {
            hookSystemProperties(lpparam, p)
            XposedBridge.log("[OK] SystemProperties Hook 已安装")
        } catch (t: Throwable) {
            XposedBridge.log("[FAIL] SystemProperties: ${t.message}")
            XposedBridge.log(t)
        }

        try {
            hookDeviceIdentifiers(lpparam, p)
            XposedBridge.log("[OK] DeviceIdentifier Hook 已安装")
        } catch (t: Throwable) {
            XposedBridge.log("[FAIL] DeviceIdentifier: ${t.message}")
            XposedBridge.log(t)
        }

        try {
            TelephonyHook.hook(lpparam, p)
            XposedBridge.log("[OK] Telephony Hook 已安装")
        } catch (t: Throwable) {
            XposedBridge.log("[FAIL] Telephony: ${t.message}")
        }

        try {
            NetworkHook.hook(lpparam)
            XposedBridge.log("[OK] Network Hook 已安装")
        } catch (t: Throwable) {
            XposedBridge.log("[FAIL] Network: ${t.message}")
        }

        // 验证 Build 字段是否成功修改
        XposedBridge.log("-".repeat(48))
        XposedBridge.log("验证: MODEL=${Build.MODEL}, BRAND=${Build.BRAND}, MANUFACTURER=${Build.MANUFACTURER}")
        XposedBridge.log("验证: SERIAL=${Build.getSerial()}")
        XposedBridge.log("=".repeat(48))
    }

    // ====== 直接修改 Build 静态字段 ======
    private fun applyBuildFieldSpoofing(p: DeviceProfile) {
        val fields = mapOf(
            "MODEL" to p.phoneModel,
            "BRAND" to p.phoneBrand,
            "MANUFACTURER" to p.phoneManufacturer,
            "DEVICE" to p.phoneDevice,
            "PRODUCT" to p.phoneProduct,
            "HARDWARE" to p.phoneHardware,
            "FINGERPRINT" to p.phoneFingerprint,
            "BOARD" to p.phoneDevice,
            "DISPLAY" to "${p.phoneDevice}-user 14 UP1A.231005.007 release-keys",
            "ID" to "UP1A.231005.007",
            "SUPPORTED_ABIS" to arrayOf(p.cpuArch, "armeabi-v7a", "armeabi"),
            "SUPPORTED_32_BIT_ABIS" to arrayOf("armeabi-v7a", "armeabi"),
            "SUPPORTED_64_BIT_ABIS" to arrayOf(p.cpuArch)
        )

        for ((name, value) in fields) {
            setBuildField(Build::class.java, name, value)
        }
    }

    private fun setBuildField(clazz: Class<*>, fieldName: String, value: Any) {
        // 策略1: XposedHelpers.setStaticObjectField (Unsafe)
        try {
            XposedHelpers.setStaticObjectField(clazz, fieldName, value)
            return
        } catch (e1: Throwable) {
            XposedBridge.log("  [WARN] 策略1失败: $fieldName → ${e1.message}")
        }

        // 策略2: 反射 + 去 final
        try {
            val field = clazz.getDeclaredField(fieldName)
            field.isAccessible = true

            val modifiersField = java.lang.reflect.Field::class.java.getDeclaredField("modifiers")
            modifiersField.isAccessible = true
            modifiersField.setInt(field, field.modifiers and java.lang.reflect.Modifier.FINAL.inv())

            field.set(null, value)
            return
        } catch (e2: Throwable) {
            XposedBridge.log("  [WARN] 策略2失败: $fieldName → ${e2.message}")
        }

        // 策略3: 通过 accessFlags (Android 10+)
        try {
            val field = clazz.getDeclaredField(fieldName)
            field.isAccessible = true

            val accessFlagsField = java.lang.reflect.Field::class.java.getDeclaredField("accessFlags")
            accessFlagsField.isAccessible = true
            accessFlagsField.setInt(field, field.modifiers and java.lang.reflect.Modifier.FINAL.inv())

            field.set(null, value)
        } catch (e3: Throwable) {
            XposedBridge.log("  [ERR] 所有策略都失败: $fieldName → ${e3.message}")
        }
    }

    // ====== SystemProperties 底层拦截 ======
    private fun hookSystemProperties(lpparam: XC_LoadPackage.LoadPackageParam, p: DeviceProfile) {
        val spoofMap = buildMap {
            put("ro.serialno", p.serialNumber)
            put("ro.boot.serialno", p.serialNumber)
            put("ro.product.model", p.phoneModel)
            put("ro.product.brand", p.phoneBrand)
            put("ro.product.manufacturer", p.phoneManufacturer)
            put("ro.product.device", p.phoneDevice)
            put("ro.product.name", p.phoneProduct)
            put("ro.product.board", p.phoneDevice)
            put("ro.hardware", p.phoneHardware)
            put("ro.build.fingerprint", p.phoneFingerprint)
            put("ro.build.description", "${p.phoneDevice}-user 14 UP1A.231005.007 release-keys")
            put("ro.board.platform", "lahaina")
            put("ro.chipname", p.cpuModel)
            put("ro.product.cpu.abi", p.cpuArch)
            put("ro.product.cpu.abilist", p.cpuAbiList)
            put("ro.product.cpu.abilist32", p.cpuAbiList32)
            put("ro.product.cpu.abilist64", p.cpuArch)
        }

        try {
            // 使用 BootClassLoader (null) 查找 SystemProperties
            val spClazz = XposedHelpers.findClass("android.os.SystemProperties", null)

            // SystemProperties.get(key)
            XposedHelpers.findAndHookMethod(
                spClazz, "get", String::class.java,
                object : de.robv.android.xposed.XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val key = param.args[0] as? String ?: return
                        spoofMap[key]?.let { param.result = it }
                    }
                }
            )

            // SystemProperties.get(key, def)
            XposedHelpers.findAndHookMethod(
                spClazz, "get", String::class.java, String::class.java,
                object : de.robv.android.xposed.XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val key = param.args[0] as? String ?: return
                        spoofMap[key]?.let { param.result = it }
                    }
                }
            )

            // SystemProperties.getInt(key, def)
            try {
                XposedHelpers.findAndHookMethod(
                    spClazz, "getInt", String::class.java, Int::class.javaPrimitiveType,
                    object : de.robv.android.xposed.XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {}
                    }
                )
            } catch (_: Throwable) {}

            // SystemProperties.getLong(key, def)
            try {
                XposedHelpers.findAndHookMethod(
                    spClazz, "getLong", String::class.java, Long::class.javaPrimitiveType,
                    object : de.robv.android.xposed.XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {}
                    }
                )
            } catch (_: Throwable) {}

            // SystemProperties.getBoolean(key, def)
            try {
                XposedHelpers.findAndHookMethod(
                    spClazz, "getBoolean", String::class.java, Boolean::class.javaPrimitiveType,
                    object : de.robv.android.xposed.XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {}
                    }
                )
            } catch (_: Throwable) {}

            XposedBridge.log("  SystemProperties 已拦截 (${spoofMap.size} 个属性)")
        } catch (t: Throwable) {
            XposedBridge.log("  SystemProperties 拦截失败: ${t.message}")
            XposedBridge.log(t)
        }
    }

    // ====== 设备标识符 Hook ======
    private fun hookDeviceIdentifiers(lpparam: XC_LoadPackage.LoadPackageParam, p: DeviceProfile) {
        // === Build.getSerial() ===
        try {
            XposedHelpers.findAndHookMethod(
                Build::class.java, "getSerial",
                object : de.robv.android.xposed.XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = p.serialNumber
                    }
                }
            )
        } catch (t: Throwable) {
            XposedBridge.log("  Build.getSerial 失败: ${t.message}")
        }

        // === Settings.Secure.getString() → Android ID ===
        try {
            XposedHelpers.findAndHookMethod(
                "android.provider.Settings.Secure", lpparam.classLoader,
                "getString",
                android.content.ContentResolver::class.java,
                String::class.java,
                object : de.robv.android.xposed.XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (param.args[1] == "android_id") {
                            param.result = p.androidId
                        }
                    }
                }
            )
            XposedBridge.log("  Android ID 已拦截 → ${p.androidId}")
        } catch (t: Throwable) {
            XposedBridge.log("  Android ID 失败: ${t.message}")
        }

        // === WifiInfo.getMacAddress() ===
        try {
            XposedHelpers.findAndHookMethod(
                "android.net.wifi.WifiInfo", lpparam.classLoader,
                "getMacAddress",
                object : de.robv.android.xposed.XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = p.macAddress
                    }
                }
            )
            XposedBridge.log("  WiFi MAC 已拦截 → ${p.macAddress}")
        } catch (t: Throwable) {
            XposedBridge.log("  WiFi MAC 失败: ${t.message}")
        }

        // === BluetoothAdapter.getAddress() ===
        try {
            XposedHelpers.findAndHookMethod(
                "android.bluetooth.BluetoothAdapter", lpparam.classLoader,
                "getAddress",
                object : de.robv.android.xposed.XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = p.bluetoothMac
                    }
                }
            )
            XposedBridge.log("  BT MAC 已拦截 → ${p.bluetoothMac}")
        } catch (t: Throwable) {
            XposedBridge.log("  BT MAC 失败: ${t.message}")
        }

        // === NetworkInterface.getHardwareAddress() ===
        try {
            XposedHelpers.findAndHookMethod(
                java.net.NetworkInterface::class.java,
                "getHardwareAddress",
                object : de.robv.android.xposed.XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val ni = param.thisObject as java.net.NetworkInterface
                            val name = ni.name ?: ""
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
            XposedBridge.log("  NetworkInterface MAC 已拦截")
        } catch (t: Throwable) {
            XposedBridge.log("  NetworkInterface MAC 失败: ${t.message}")
        }

        // === OAID/AAID 各厂商 ===
        hookOaidProviders(lpparam, p)
    }

    private fun hookOaidProviders(lpparam: XC_LoadPackage.LoadPackageParam, p: DeviceProfile) {
        // 华为 OAID
        try {
            XposedHelpers.findAndHookMethod(
                "com.huawei.hms.ads.identifier.AdvertisingIdClient",
                lpparam.classLoader,
                "getAdvertisingIdInfo",
                android.content.Context::class.java,
                object : de.robv.android.xposed.XC_MethodHook() {
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

        // Google AAID
        try {
            XposedHelpers.findAndHookMethod(
                "com.google.android.gms.ads.identifier.AdvertisingIdClient",
                lpparam.classLoader,
                "getAdvertisingIdInfo",
                android.content.Context::class.java,
                object : de.robv.android.xposed.XC_MethodHook() {
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

        // 小米 OAID
        try {
            XposedHelpers.findAndHookMethod(
                "com.android.id.impl.IdProviderImpl", lpparam.classLoader,
                "getOAID", android.content.Context::class.java,
                object : de.robv.android.xposed.XC_MethodHook() {
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
                object : de.robv.android.xposed.XC_MethodHook() {
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
                object : de.robv.android.xposed.XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = p.oaid
                    }
                }
            )
        } catch (_: Throwable) {}
    }
}