package com.devicefaker.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.devicefaker.model.DeviceProfile
import com.devicefaker.model.NetworkRule
import com.devicefaker.model.SpoofConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "devicefaker_config")

object DataStoreManager {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // ===== DeviceProfile =====

    suspend fun saveProfile(profile: DeviceProfile) {
        appContext.dataStore.edit { prefs ->
            prefs[stringPreferencesKey("serial")] = profile.serialNumber
            prefs[stringPreferencesKey("mac")] = profile.macAddress
            prefs[stringPreferencesKey("bt_mac")] = profile.bluetoothMac
            prefs[stringPreferencesKey("android_id")] = profile.androidId
            prefs[stringPreferencesKey("imei")] = profile.imei
            prefs[stringPreferencesKey("imei2")] = profile.imei2
            prefs[stringPreferencesKey("meid")] = profile.meid
            prefs[stringPreferencesKey("imsi")] = profile.imsi
            prefs[stringPreferencesKey("oaid")] = profile.oaid
            prefs[stringPreferencesKey("model")] = profile.phoneModel
            prefs[stringPreferencesKey("brand")] = profile.phoneBrand
            prefs[stringPreferencesKey("manufacturer")] = profile.phoneManufacturer
            prefs[stringPreferencesKey("device")] = profile.phoneDevice
            prefs[stringPreferencesKey("product")] = profile.phoneProduct
            prefs[stringPreferencesKey("hardware")] = profile.phoneHardware
            prefs[stringPreferencesKey("fingerprint")] = profile.phoneFingerprint
            prefs[stringPreferencesKey("cpu_model")] = profile.cpuModel
            prefs[intPreferencesKey("cpu_cores")] = profile.cpuCores
            prefs[stringPreferencesKey("cpu_arch")] = profile.cpuArch
            prefs[stringPreferencesKey("cpu_abilist")] = profile.cpuAbiList
        }
    }

    suspend fun loadProfile(): DeviceProfile {
        val prefs = appContext.dataStore.data.first()
        return DeviceProfile(
            serialNumber = prefs[stringPreferencesKey("serial")] ?: "",
            macAddress = prefs[stringPreferencesKey("mac")] ?: "",
            bluetoothMac = prefs[stringPreferencesKey("bt_mac")] ?: "",
            androidId = prefs[stringPreferencesKey("android_id")] ?: "",
            imei = prefs[stringPreferencesKey("imei")] ?: "",
            imei2 = prefs[stringPreferencesKey("imei2")] ?: "",
            meid = prefs[stringPreferencesKey("meid")] ?: "",
            imsi = prefs[stringPreferencesKey("imsi")] ?: "",
            oaid = prefs[stringPreferencesKey("oaid")] ?: "",
            phoneModel = prefs[stringPreferencesKey("model")] ?: "",
            phoneBrand = prefs[stringPreferencesKey("brand")] ?: "",
            phoneManufacturer = prefs[stringPreferencesKey("manufacturer")] ?: "",
            phoneDevice = prefs[stringPreferencesKey("device")] ?: "",
            phoneProduct = prefs[stringPreferencesKey("product")] ?: "",
            phoneHardware = prefs[stringPreferencesKey("hardware")] ?: "",
            phoneFingerprint = prefs[stringPreferencesKey("fingerprint")] ?: "",
            cpuModel = prefs[stringPreferencesKey("cpu_model")] ?: "",
            cpuCores = prefs[intPreferencesKey("cpu_cores")] ?: 8,
            cpuArch = prefs[stringPreferencesKey("cpu_arch")] ?: "arm64-v8a",
            cpuAbiList = prefs[stringPreferencesKey("cpu_abilist")] ?: "arm64-v8a,armeabi-v7a,armeabi"
        )
    }

    // ===== SpoofConfig =====

    suspend fun saveConfig(config: SpoofConfig) {
        appContext.dataStore.edit { prefs ->
            prefs[stringPreferencesKey("target_pkg")] = config.targetPackage
            prefs[booleanPreferencesKey("spoof_serial")] = config.spoofSerial
            prefs[booleanPreferencesKey("spoof_mac")] = config.spoofMac
            prefs[booleanPreferencesKey("spoof_bt_mac")] = config.spoofBluetoothMac
            prefs[booleanPreferencesKey("spoof_android_id")] = config.spoofAndroidId
            prefs[booleanPreferencesKey("spoof_imei")] = config.spoofImei
            prefs[booleanPreferencesKey("spoof_meid")] = config.spoofMeid
            prefs[booleanPreferencesKey("spoof_imsi")] = config.spoofImsi
            prefs[booleanPreferencesKey("spoof_oaid")] = config.spoofOaid
            prefs[booleanPreferencesKey("spoof_model")] = config.spoofPhoneModel
            prefs[booleanPreferencesKey("spoof_cpu")] = config.spoofCpuModel
            prefs[booleanPreferencesKey("net_intercept")] = config.networkIntercept
            prefs[booleanPreferencesKey("randomize_boot")] = config.randomizeOnBoot
        }
    }

    suspend fun loadConfig(): SpoofConfig {
        val prefs = appContext.dataStore.data.first()
        return SpoofConfig(
            targetPackage = prefs[stringPreferencesKey("target_pkg")] ?: "com.immomo.miraimind",
            spoofSerial = prefs[booleanPreferencesKey("spoof_serial")] ?: true,
            spoofMac = prefs[booleanPreferencesKey("spoof_mac")] ?: true,
            spoofBluetoothMac = prefs[booleanPreferencesKey("spoof_bt_mac")] ?: true,
            spoofAndroidId = prefs[booleanPreferencesKey("spoof_android_id")] ?: true,
            spoofImei = prefs[booleanPreferencesKey("spoof_imei")] ?: true,
            spoofMeid = prefs[booleanPreferencesKey("spoof_meid")] ?: true,
            spoofImsi = prefs[booleanPreferencesKey("spoof_imsi")] ?: true,
            spoofOaid = prefs[booleanPreferencesKey("spoof_oaid")] ?: true,
            spoofPhoneModel = prefs[booleanPreferencesKey("spoof_model")] ?: true,
            spoofCpuModel = prefs[booleanPreferencesKey("spoof_cpu")] ?: true,
            networkIntercept = prefs[booleanPreferencesKey("net_intercept")] ?: true,
            randomizeOnBoot = prefs[booleanPreferencesKey("randomize_boot")] ?: true
        )
    }
}