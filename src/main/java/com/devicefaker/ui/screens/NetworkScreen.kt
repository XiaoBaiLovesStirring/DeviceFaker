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
import com.devicefaker.model.NetworkRule
import com.devicefaker.ui.theme.*

@Composable
fun NetworkScreen() {
    var rules by remember { mutableStateOf(listOf<NetworkRule>()) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Default rules
    LaunchedEffect(Unit) {
        if (rules.isEmpty()) {
            rules = listOf(
                NetworkRule(
                    name = "API 重定向",
                    originalUrl = "api.example.com",
                    redirectUrl = "192.168.1.100:8080",
                    enabled = true
                ),
                NetworkRule(
                    name = "响应篡改示例",
                    originalUrl = "config.example.com/status",
                    modifyResponse = true,
                    newResponseBody = "{\"status\":\"ok\",\"verified\":true}",
                    newStatusCode = 200,
                    enabled = false
                )
            )
        }
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
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = NeonGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.CloudOff,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.padding(12.dp).size(28.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "服务器拦截规则",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "拦截并篡改目标应用的网络请求",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Global toggle
        item {
            var enabled by remember { mutableStateOf(true) }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (enabled) NeonGreen.copy(alpha = 0.1f) else SurfaceVariantDark
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (enabled) Icons.Filled.Shield else Icons.Filled.Shield,
                            contentDescription = null,
                            tint = if (enabled) NeonGreen else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                if (enabled) "拦截已启用" else "拦截已禁用",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "OkHttp · HttpURLConnection · WebView",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = { enabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DeepBackground,
                            checkedTrackColor = NeonGreen
                        )
                    )
                }
            }
        }

        // Add rule button
        item {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen.copy(alpha = 0.15f),
                    contentColor = NeonGreen
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("添加规则", fontWeight = FontWeight.SemiBold)
            }
        }

        // Rules list
        items(rules.filter { it.enabled }) { rule ->
            NetworkRuleCard(rule)
        }

        // Disabled rules
        if (rules.any { !it.enabled }) {
            item {
                Text(
                    "已禁用",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(rules.filter { !it.enabled }) { rule ->
                NetworkRuleCard(rule)
            }
        }
    }

    if (showAddDialog) {
        AddRuleDialog(
            onDismiss = { showAddDialog = false },
            onSave = { rule ->
                rules = rules + rule
                showAddDialog = false
            }
        )
    }
}

@Composable
fun NetworkRuleCard(rule: NetworkRule) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (rule.enabled) SurfaceVariantDark else SurfaceVariantDark.copy(alpha = 0.5f)
        ),
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
                        if (rule.modifyResponse) Icons.Filled.EditNote else Icons.Filled.AltRoute,
                        contentDescription = null,
                        tint = if (rule.enabled) NeonGreen else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        rule.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    color = if (rule.enabled) NeonGreen.copy(alpha = 0.15f) else TextSecondary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        if (rule.modifyResponse) "篡改响应" else "重定向",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (rule.enabled) NeonGreen else TextSecondary
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            if (rule.originalUrl.isNotEmpty()) {
                Row {
                    Text(
                        "目标: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        rule.originalUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = WarningAmber,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            if (rule.redirectUrl.isNotEmpty()) {
                Row {
                    Text(
                        "重定向: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        rule.redirectUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonGreen,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun AddRuleDialog(
    onDismiss: () -> Unit,
    onSave: (NetworkRule) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var originalUrl by remember { mutableStateOf("") }
    var redirectUrl by remember { mutableStateOf("") }
    var modifyResponse by remember { mutableStateOf(false) }
    var newResponseBody by remember { mutableStateOf("") }
    var newStatusCode by remember { mutableStateOf("200") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        iconContentColor = NeonGreen,
        title = {
            Text("添加拦截规则", fontWeight = FontWeight.Bold)
        },
        icon = {
            Icon(Icons.Filled.AddCircle, contentDescription = null)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("规则名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedFieldColors(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = originalUrl,
                    onValueChange = { originalUrl = it },
                    label = { Text("原始 URL / 关键词") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedFieldColors(),
                    shape = RoundedCornerShape(8.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("修改响应", color = TextPrimary)
                    Switch(
                        checked = modifyResponse,
                        onCheckedChange = { modifyResponse = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DeepBackground,
                            checkedTrackColor = NeonGreen
                        )
                    )
                }
                if (modifyResponse) {
                    OutlinedTextField(
                        value = newStatusCode,
                        onValueChange = { newStatusCode = it },
                        label = { Text("状态码") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = newResponseBody,
                        onValueChange = { newResponseBody = it },
                        label = { Text("新响应体") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    OutlinedTextField(
                        value = redirectUrl,
                        onValueChange = { redirectUrl = it },
                        label = { Text("重定向到 URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        NetworkRule(
                            name = name.ifEmpty { "未命名规则" },
                            originalUrl = originalUrl,
                            redirectUrl = redirectUrl,
                            modifyResponse = modifyResponse,
                            newResponseBody = newResponseBody,
                            newStatusCode = newStatusCode.toIntOrNull() ?: 200
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = DeepBackground)
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonGreen,
    focusedLabelColor = NeonGreen,
    cursorColor = NeonGreen,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    unfocusedBorderColor = BorderDark
)