package com.devicefaker.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devicefaker.DeviceState
import com.devicefaker.model.DeviceProfile
import com.devicefaker.ui.theme.*
import com.devicefaker.utils.DataStoreManager
import com.devicefaker.utils.RandomGenerator
import kotlinx.coroutines.launch

@Composable
fun DeviceScreen() {
    val scope = rememberCoroutineScope()
    var profile by remember { mutableStateOf(DeviceState.currentProfile) }

    // 同步到 HookInit
    fun sync() {
        DeviceState.currentProfile = profile
        scope.launch { DataStoreManager.saveProfile(profile) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = NeonGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Filled.Shield, null, tint = NeonGreen, modifier = Modifier.padding(12.dp).size(28.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("设备伪装配置", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text("修改后将在目标应用下次启动时生效", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }

        // Randomize all
        item {
            Button(
                onClick = {
                    profile = RandomGenerator.generateFullProfile()
                    sync()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen.copy(alpha = 0.15f), contentColor = NeonGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Shuffle, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("随机生成全部", fontWeight = FontWeight.SemiBold)
            }
        }

        // Fields
        item { DeviceFieldCard("序列号 (SN)", Icons.Filled.Tag, profile.serialNumber) { profile = profile.copy(serialNumber = it); sync() } }
        item { DeviceFieldCard("MAC 地址", Icons.Filled.Wifi, profile.macAddress) { profile = profile.copy(macAddress = it); sync() } }
        item { DeviceFieldCard("蓝牙 MAC", Icons.Filled.Bluetooth, profile.bluetoothMac) { profile = profile.copy(bluetoothMac = it); sync() } }
        item { DeviceFieldCard("Android ID", Icons.Filled.Android, profile.androidId) { profile = profile.copy(androidId = it); sync() } }
        item { DeviceFieldCard("IMEI", Icons.Filled.PhoneAndroid, profile.imei) { profile = profile.copy(imei = it); sync() } }
        item { DeviceFieldCard("IMEI2 (SIM2)", Icons.Filled.SimCard, profile.imei2) { profile = profile.copy(imei2 = it); sync() } }
        item { DeviceFieldCard("MEID", Icons.Filled.SimCard, profile.meid) { profile = profile.copy(meid = it); sync() } }
        item { DeviceFieldCard("IMSI", Icons.Filled.CreditCard, profile.imsi) { profile = profile.copy(imsi = it); sync() } }
        item { DeviceFieldCard("OAID / AAID", Icons.Filled.Fingerprint, profile.oaid) { profile = profile.copy(oaid = it); sync() } }
        item { DeviceFieldCard("手机型号", Icons.Filled.Smartphone, profile.phoneModel) { profile = profile.copy(phoneModel = it); sync() } }
        item { DeviceFieldCard("品牌", Icons.Filled.Store, profile.phoneBrand) { profile = profile.copy(phoneBrand = it); sync() } }
        item { DeviceFieldCard("制造商", Icons.Filled.Factory, profile.phoneManufacturer) { profile = profile.copy(phoneManufacturer = it); sync() } }
        item { DeviceFieldCard("CPU 型号", Icons.Filled.Memory, profile.cpuModel) { profile = profile.copy(cpuModel = it); sync() } }
        item { DeviceFieldCard("CPU 架构", Icons.Filled.DeveloperBoard, profile.cpuArch) { profile = profile.copy(cpuArch = it); sync() } }
    }
}

@Composable
fun DeviceFieldCard(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var editing by remember(value) { mutableStateOf(value) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(label, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(value.ifEmpty { "未设置" }, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontFamily = FontFamily.Monospace)
                    }
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.Edit, "编辑", tint = NeonGreen)
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editing,
                        onValueChange = { editing = it; onChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("自定义值") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen, focusedLabelColor = NeonGreen,
                            cursorColor = NeonGreen, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val newVal = randomValueForField(label)
                                editing = newVal; onChange(newVal)
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen)
                        ) {
                            Icon(Icons.Filled.Shuffle, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("随机")
                        }
                        OutlinedButton(
                            onClick = { expanded = false },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                        ) { Text("完成") }
                    }
                }
            }
        }
    }
}

private fun randomValueForField(label: String): String = when {
    label.contains("序列号") -> RandomGenerator.generateSerialNumber()
    label.contains("MAC") -> RandomGenerator.generateMacAddress()
    label.contains("蓝牙") -> RandomGenerator.generateMacAddress()
    label.contains("Android ID") -> RandomGenerator.generateAndroidId()
    label.contains("IMEI2") -> RandomGenerator.generateImei()
    label.contains("IMEI") -> RandomGenerator.generateImei()
    label.contains("MEID") -> RandomGenerator.generateMeid()
    label.contains("IMSI") -> RandomGenerator.generateImsi()
    label.contains("OAID") -> RandomGenerator.generateOaid()
    label.contains("型号") -> RandomGenerator.generatePhoneModel()
    label.contains("品牌") -> "samsung"
    label.contains("制造商") -> "samsung"
    label.contains("CPU") && label.contains("架构") -> RandomGenerator.generateCpuArch()
    label.contains("CPU") -> RandomGenerator.generateCpuModel()
    else -> ""
}