package com.devicefaker

import com.devicefaker.model.DeviceProfile
import com.devicefaker.model.SpoofConfig
import java.text.SimpleDateFormat
import java.util.*

/**
 * 全局状态持有者 — 不依赖 Xposed API，UI 和 Hook 层共享
 * 从 HookInit 剥离出来，避免普通环境下触发 Xposed 类加载导致崩溃
 */
object DeviceState {

    @Volatile var currentProfile = DeviceProfile()
    @Volatile var currentConfig = SpoofConfig()

    private val logLines = mutableListOf<String>()
    private val logLock = Any()

    fun log(msg: String) {
        val ts = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val line = "[$ts] $msg"
        synchronized(logLock) {
            logLines.add(line)
            if (logLines.size > 500) logLines.removeAt(0)
        }
    }

    fun getLogs(): List<String> = synchronized(logLock) { logLines.toList() }

    fun clearLogs() = synchronized(logLock) { logLines.clear() }
}