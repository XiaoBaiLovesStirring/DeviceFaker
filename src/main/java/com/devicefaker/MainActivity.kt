package com.devicefaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devicefaker.ui.screens.*
import com.devicefaker.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeviceFakerTheme {
                DeviceFakerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceFakerApp() {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        Triple("设备伪装", Icons.Filled.DevicesOther, Icons.Outlined.DevicesOther),
        Triple("网络拦截", Icons.Filled.CloudOff, Icons.Outlined.CloudOff),
        Triple("日志", Icons.Filled.Terminal, Icons.Outlined.Terminal),
        Triple("设置", Icons.Filled.Settings, Icons.Outlined.Settings)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Shield,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "DeviceFaker",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = NeonGreen.copy(alpha = 0.15f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                "PRO",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = NeonGreen,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                contentColor = TextPrimary
            ) {
                tabs.forEachIndexed { index, (label, selectedIcon, unselectedIcon) ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (selectedTab == index) selectedIcon else unselectedIcon,
                                contentDescription = label
                            )
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonGreen,
                            selectedTextColor = NeonGreen,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = NeonGreen.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() + slideInHorizontally { it / 4 } togetherWith
                        fadeOut() + slideOutHorizontally { -it / 4 }
                }
            ) { tab ->
                when (tab) {
                    0 -> DeviceScreen()
                    1 -> NetworkScreen()
                    2 -> LogScreen()
                    3 -> SettingsScreen()
                }
            }
        }
    }
}