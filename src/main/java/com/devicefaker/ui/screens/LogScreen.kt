package com.devicefaker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.devicefaker.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LogScreen() {
    val logs = remember { derivedStateOf { HookInit.logLines.toList() } }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var autoScroll by remember { mutableStateOf(true) }

    LaunchedEffect(logs.value.size) {
        if (autoScroll && logs.value.isNotEmpty()) {
            listState.animateScrollToItem(logs.value.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header card
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = NeonGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Filled.Terminal,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.padding(10.dp).size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Hook 日志",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${logs.value.size} 条记录",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { autoScroll = !autoScroll },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                if (autoScroll) Icons.Filled.VerticalAlignBottom else Icons.Filled.PauseCircle,
                                contentDescription = "自动滚动",
                                tint = if (autoScroll) NeonGreen else TextSecondary
                            )
                        }
                        IconButton(
                            onClick = {
                                synchronized(HookInit.logLines) {
                                    HookInit.logLines.clear()
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Filled.DeleteSweep,
                                contentDescription = "清空",
                                tint = ErrorRed
                            )
                        }
                    }
                }
            }
        }

        // Log list
        if (logs.value.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Inbox,
                        contentDescription = null,
                        tint = TextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "暂无日志",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                    Text(
                        "启动目标应用后自动记录",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(logs.value) { log ->
                    val isHookLine = log.contains("[Hook]")
                    val isError = log.contains("✗") || log.contains("⚠")
                    val isSuccess = log.contains("✓")
                    val isSeparator = log.contains("====")

                    val lineColor = when {
                        isSeparator -> NeonGreen
                        isSuccess -> NeonGreen
                        isError -> ErrorRed
                        isHookLine -> WarningAmber
                        else -> TextSecondary
                    }

                    Text(
                        text = log,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = lineColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSeparator) NeonGreen.copy(alpha = 0.05f)
                                else if (isHookLine) WarningAmber.copy(alpha = 0.03f)
                                else androidx.compose.ui.graphics.Color.Transparent,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}