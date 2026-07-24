package com.devicefaker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devicefaker.HookInit
import com.devicefaker.ui.theme.*

@Composable
fun SettingsScreen() {
    var targetPackage by remember { mutableStateOf(HookInit.currentConfig.targetPackage) }
    var spoofSerial by remember { mutableStateOf(HookInit.currentConfig.spoofSerial) }
    var spoofMac by remember { mutableStateOf(HookInit.currentConfig.spoofMac) }
    var spoofAndroidId by remember { mutableStateOf(HookInit.currentConfig.spoofAndroidId) }
    var spoofImei by remember { mutableStateOf(HookInit.currentConfig.spoofImei) }
    var spoofMeid by remember { mutableStateOf(HookInit.currentConfig.spoofMeid) }
    var spoofOaid by remember { mutableStateOf(HookInit.currentConfig.spoofOaid) }
    var spoofPhoneModel by remember { mutableStateOf(HookInit.currentConfig.spoofPhoneModel) }
    var spoofCpuModel by remember { mutableStateOf(HookInit.currentConfig.spoofCpuModel) }
    var networkIntercept by remember { mutableStateOf(HookInit.currentConfig.networkIntercept) }
    var randomizeOnBoot by remember { mutableStateOf(HookInit.currentConfig.randomizeOnBoot) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // About card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = NeonGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Filled.Shield,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.padding(16.dp).size(40.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "DeviceFaker Pro",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "v2.0.0 · API 86+",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = NeonGreen.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "免 Root · LSPatch 兼容",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonGreen
                    )
                }
            }
        }

        // Target package
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "目标应用包名",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetPackage,
                    onValueChange = {
                        targetPackage = it
                        HookInit.currentConfig = HookInit.currentConfig.copy(targetPackage = it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        focusedLabelColor = NeonGreen,
                        cursorColor = NeonGreen,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        unfocusedBorderColor = BorderDark
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        // Spoof toggles
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "伪装开关",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))

                SettingsToggle("序列号 (SN)", spoofSerial) {
                    spoofSerial = it
                    HookInit.currentConfig = HookInit.currentConfig.copy(spoofSerial = it)
                }
                SettingsToggle("MAC 地址", spoofMac) {
                    spoofMac = it
                    HookInit.currentConfig = HookInit.currentConfig.copy(spoofMac = it)
                }
                SettingsToggle("Android ID", spoofAndroidId) {
                    spoofAndroidId = it
                    HookInit.currentConfig = HookInit.currentConfig.copy(spoofAndroidId = it)
                }
                SettingsToggle("IMEI", spoofImei) {
                    spoofImei = it
                    HookInit.currentConfig = HookInit.currentConfig.copy(spoofImei = it)
                }
                SettingsToggle("MEID", spoofMeid) {
                    spoofMeid = it
                    HookInit.currentConfig = HookInit.currentConfig.copy(spoofMeid = it)
                }
                SettingsToggle("OAID / AAID", spoofOaid) {
                    spoofOaid = it
                    HookInit.currentConfig = HookInit.currentConfig.copy(spoofOaid = it)
                }
                SettingsToggle("手机型号", spoofPhoneModel) {
                    spoofPhoneModel = it
                    HookInit.currentConfig = HookInit.currentConfig.copy(spoofPhoneModel = it)
                }
                SettingsToggle("CPU 型号", spoofCpuModel) {
                    spoofCpuModel = it
                    HookInit.currentConfig = HookInit.currentConfig.copy(spoofCpuModel = it)
                }
                SettingsToggle("网络拦截", networkIntercept) {
                    networkIntercept = it
                    HookInit.currentConfig = HookInit.currentConfig.copy(networkIntercept = it)
                }
                SettingsToggle("启动时随机化", randomizeOnBoot) {
                    randomizeOnBoot = it
                    HookInit.currentConfig = HookInit.currentConfig.copy(randomizeOnBoot = it)
                }
            }
        }

        // LSPatch guide
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "LSPatch 使用指南",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                GuideStep("1", "安装 LSPatch 应用")
                GuideStep("2", "在 LSPatch 中选择目标 APK 进行修补")
                GuideStep("3", "在模块管理中勾选 DeviceFaker")
                GuideStep("4", "安装修补后的 APK，无需 Root")
                GuideStep("5", "启动目标应用，伪装自动生效")
            }
        }
    }
}

@Composable
fun SettingsToggle(
    label: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary
        )
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DeepBackground,
                checkedTrackColor = NeonGreen,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = BorderDark
            )
        )
    }
}

@Composable
fun GuideStep(number: String, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = NeonGreen.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    number,
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}