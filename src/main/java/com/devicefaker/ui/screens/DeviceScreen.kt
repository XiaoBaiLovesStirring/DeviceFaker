package com.devicefaker.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.devicefaker.HookInit
import com.devicefaker.model.DeviceProfile
import com.devicefaker.ui.theme.*
import com.devicefaker.utils.RandomGenerator

data class DeviceField(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val key: String,
    val getValue: () -> String,
    val setValue: (String) -> Unit
)

@Composable
fun DeviceScreen() {
    val profile = remember { mutableStateOf(HookInit.currentProfile) }
    var showSnackbar by remember { mutableStateOf(false) }

    val fields = remember {
        listOf(
            DeviceField("序列号 (SN)", Icons.Filled.Tag, "serial",
                { profile.value.serialNumber },
                { profile.value = profile.value.copy(serialNumber = it) }
            ),
            DeviceField("MAC 地址", Icons.Filled.Wifi, "mac",
                { profile.value.macAddress },
                { profile.value = profile.value.copy(macAddress = it) }
            ),
            DeviceField("Android ID", Icons.Filled.Android, "android_id",
                { profile.value.androidId },
                { profile.value = profile.value.copy(androidId = it) }
            ),
            DeviceField("IMEI", Icons.Filled.PhoneAndroid, "imei",
                { profile.value.imei },
                { profile.value = profile.value.copy(imei = it) }
            ),
            DeviceField("MEID", Icons.Filled.SimCard, "meid",
                { profile.value.meid },
                { profile.value = profile.value.copy(meid = it) }
            ),
            DeviceField("OAID / AAID", Icons.Filled.Fingerprint, "oaid",
                { profile.value.oaid },
                { profile.value = profile.value.copy(oaid = it) }
            ),
            DeviceField("手机型号", Icons.Filled.PhoneAndroid, "model",
                { profile.value.phoneModel },
                { profile.value = profile.value.copy(phoneModel = it) }
            ),
            DeviceField("CPU 型号", Icons.Filled.Memory, "cpu",
                { profile.value.cpuModel },
                { profile.value = profile.value.copy(cpuModel = it) }
            )
        )
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
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = NeonGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.Shield,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.padding(12.dp).size(28.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "设备伪装配置",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "所有值将在目标应用启动时生效",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Randomize all button
        item {
            Button(
                onClick = {
                    profile.value = DeviceProfile(
                        serialNumber = RandomGenerator.generateSerialNumber(),
                        macAddress = RandomGenerator.generateMacAddress(),
                        androidId = RandomGenerator.generateAndroidId(),
                        imei = RandomGenerator.generateImei(),
                        meid = RandomGenerator.generateMeid(),
                        oaid = RandomGenerator.generateOaid(),
                        phoneModel = RandomGenerator.generatePhoneModel(),
                        phoneBrand = "samsung",
                        phoneManufacturer = "samsung",
                        phoneDevice = "SM-S9280",
                        phoneProduct = "e3qxxx",
                        phoneHardware = "qcom",
                        phoneFingerprint = "samsung/e3qxxx/e3q:14/UP1A.231005.007/S9280ZCU1AXK5:user/release-keys",
                        cpuModel = RandomGenerator.generateCpuModel(),
                        cpuCores = 8,
                        cpuArch = "arm64-v8a"
                    )
                    HookInit.currentProfile = profile.value
                    showSnackbar = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen.copy(alpha = 0.15f),
                    contentColor = NeonGreen
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("随机生成全部", fontWeight = FontWeight.SemiBold)
            }
        }

        // Device fields
        items(fields) { field ->
            DeviceFieldCard(
                field = field,
                onValueChange = { newValue ->
                    field.setValue(newValue)
                    HookInit.currentProfile = profile.value
                }
            )
        }

        // Apply button
        item {
            Button(
                onClick = {
                    HookInit.currentProfile = profile.value
                    showSnackbar = true
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    contentColor = DeepBackground
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("应用配置", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }

    if (showSnackbar) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            showSnackbar = false
        }
    }
}

@Composable
fun DeviceFieldCard(
    field: DeviceField,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var editingValue by remember { mutableStateOf(field.getValue()) }

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
                    Icon(
                        field.icon,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            field.label,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            field.getValue(),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Filled.ExpandLess else Icons.Filled.Edit,
                        contentDescription = "编辑",
                        tint = NeonGreen
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editingValue,
                        onValueChange = {
                            editingValue = it
                            onValueChange(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("自定义值") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            focusedLabelColor = NeonGreen,
                            cursorColor = NeonGreen,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val newValue = when (field.key) {
                                    "serial" -> RandomGenerator.generateSerialNumber()
                                    "mac" -> RandomGenerator.generateMacAddress()
                                    "android_id" -> RandomGenerator.generateAndroidId()
                                    "imei" -> RandomGenerator.generateImei()
                                    "meid" -> RandomGenerator.generateMeid()
                                    "oaid" -> RandomGenerator.generateOaid()
                                    "model" -> RandomGenerator.generatePhoneModel()
                                    "cpu" -> RandomGenerator.generateCpuModel()
                                    else -> ""
                                }
                                editingValue = newValue
                                onValueChange(newValue)
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen)
                        ) {
                            Icon(Icons.Filled.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("随机")
                        }
                        OutlinedButton(
                            onClick = { expanded = false },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                        ) {
                            Text("完成")
                        }
                    }
                }
            }
        }
    }
}