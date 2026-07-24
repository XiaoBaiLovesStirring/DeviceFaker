package com.devicefaker.ui.screens

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
import com.devicefaker.model.NetworkRuleEngine
import com.devicefaker.model.RuleAction
import com.devicefaker.ui.theme.*

@Composable
fun NetworkScreen() {
    val rules by NetworkRuleEngine.rules.collectAsState()
    val enabled by NetworkRuleEngine.enabled.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

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
                        Icon(Icons.Filled.CloudOff, null, tint = NeonGreen, modifier = Modifier.padding(12.dp).size(28.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("服务器拦截规则", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text("拦截并篡改目标应用的网络请求", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }

        // Global toggle
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (enabled) NeonGreen.copy(alpha = 0.1f) else SurfaceVariantDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (enabled) Icons.Filled.Shield else Icons.Filled.Shield, null,
                            tint = if (enabled) NeonGreen else TextSecondary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(if (enabled) "拦截已启用" else "拦截已禁用",
                                style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("OkHttp / HttpURLConnection / WebView",
                                style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                    Switch(checked = enabled, onCheckedChange = { NetworkRuleEngine.setEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = DeepBackground, checkedTrackColor = NeonGreen))
                }
            }
        }

        // Add button
        item {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen.copy(alpha = 0.15f), contentColor = NeonGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("添加规则", fontWeight = FontWeight.SemiBold)
            }
        }

        // Rules
        if (rules.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Rule, null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("暂无拦截规则", color = TextSecondary)
                        Text("点击上方按钮添加", style = MaterialTheme.typography.bodySmall, color = TextSecondary.copy(alpha = 0.7f))
                    }
                }
            }
        }

        items(rules, key = { it.id }) { rule ->
            RuleCard(rule,
                onToggle = {
                    NetworkRuleEngine.setRules(rules.map { r -> if (r.id == rule.id) r.copy(enabled = !r.enabled) else r })
                },
                onDelete = {
                    NetworkRuleEngine.setRules(rules.filter { r -> r.id != rule.id })
                }
            )
        }
    }

    if (showAddDialog) {
        AddRuleDialog(
            onDismiss = { showAddDialog = false },
            onSave = { rule ->
                NetworkRuleEngine.setRules(rules + rule)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun RuleCard(rule: NetworkRule, onToggle: () -> Unit, onDelete: () -> Unit) {
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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        when (rule.action) {
                            RuleAction.REDIRECT -> Icons.Filled.AltRoute
                            RuleAction.MODIFY_RESPONSE -> Icons.Filled.EditNote
                            RuleAction.BLOCK -> Icons.Filled.Block
                        }, null,
                        tint = if (rule.enabled) NeonGreen else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(rule.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        if (rule.urlPattern.isNotEmpty()) {
                            Text(rule.urlPattern, style = MaterialTheme.typography.bodySmall, color = WarningAmber, fontFamily = FontFamily.Monospace, maxLines = 1)
                        }
                    }
                }
                Row {
                    Switch(checked = rule.enabled, onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(checkedThumbColor = DeepBackground, checkedTrackColor = NeonGreen),
                        modifier = Modifier.size(width = 40.dp, height = 24.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Delete, "删除", tint = ErrorRed, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Surface(
                color = if (rule.enabled) NeonGreen.copy(alpha = 0.15f) else TextSecondary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    when (rule.action) {
                        RuleAction.REDIRECT -> "重定向 → ${rule.redirectUrl}"
                        RuleAction.MODIFY_RESPONSE -> "篡改响应 → ${rule.newStatusCode}"
                        RuleAction.BLOCK -> "阻断请求"
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (rule.enabled) NeonGreen else TextSecondary
                )
            }
        }
    }
}

@Composable
fun AddRuleDialog(onDismiss: () -> Unit, onSave: (NetworkRule) -> Unit) {
    var name by remember { mutableStateOf("") }
    var urlPattern by remember { mutableStateOf("") }
    var action by remember { mutableStateOf(RuleAction.REDIRECT) }
    var redirectUrl by remember { mutableStateOf("") }
    var newResponseBody by remember { mutableStateOf("") }
    var newStatusCode by remember { mutableStateOf("200") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        icon = { Icon(Icons.Filled.AddCircle, null, tint = NeonGreen) },
        title = { Text("添加拦截规则", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("规则名称") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), colors = fieldColors(), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = urlPattern, onValueChange = { urlPattern = it }, label = { Text("URL 匹配模式 (支持*)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors(), shape = RoundedCornerShape(8.dp))

                Text("动作类型", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = action == RuleAction.REDIRECT, onClick = { action = RuleAction.REDIRECT },
                        label = { Text("重定向") }, colors = chipColors())
                    FilterChip(selected = action == RuleAction.MODIFY_RESPONSE, onClick = { action = RuleAction.MODIFY_RESPONSE },
                        label = { Text("篡改响应") }, colors = chipColors())
                    FilterChip(selected = action == RuleAction.BLOCK, onClick = { action = RuleAction.BLOCK },
                        label = { Text("阻断") }, colors = chipColors())
                }

                when (action) {
                    RuleAction.REDIRECT -> OutlinedTextField(value = redirectUrl, onValueChange = { redirectUrl = it },
                        label = { Text("重定向到 URL") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors(), shape = RoundedCornerShape(8.dp))
                    RuleAction.MODIFY_RESPONSE -> {
                        OutlinedTextField(value = newStatusCode, onValueChange = { newStatusCode = it },
                            label = { Text("状态码") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                            colors = fieldColors(), shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(value = newResponseBody, onValueChange = { newResponseBody = it },
                            label = { Text("新响应体") }, minLines = 3, modifier = Modifier.fillMaxWidth(),
                            colors = fieldColors(), shape = RoundedCornerShape(8.dp))
                    }
                    RuleAction.BLOCK -> Text("此规则将直接阻断匹配的请求", color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(NetworkRule(name = name.ifEmpty { "未命名规则" }, urlPattern = urlPattern, action = action,
                    redirectUrl = redirectUrl, newResponseBody = newResponseBody,
                    newStatusCode = newStatusCode.toIntOrNull() ?: 200, modifyResponse = action == RuleAction.MODIFY_RESPONSE))
            }, colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = DeepBackground)) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消", color = TextSecondary) } }
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonGreen, focusedLabelColor = NeonGreen, cursorColor = NeonGreen,
    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, unfocusedBorderColor = BorderDark
)

@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = NeonGreen.copy(alpha = 0.15f), selectedLabelColor = NeonGreen,
    containerColor = SurfaceVariantDark, labelColor = TextSecondary
)