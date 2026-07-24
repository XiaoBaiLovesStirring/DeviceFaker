package com.devicefaker.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 网络规则引擎 — UI 和 Hook 之间的共享桥梁
 * 使用单例 + StateFlow 保证 UI 和 Hook 层的数据一致性
 */
object NetworkRuleEngine {

    private val _rules = MutableStateFlow<List<NetworkRule>>(emptyList())
    val rules: StateFlow<List<NetworkRule>> get() = _rules

    private val _enabled = MutableStateFlow(true)
    val enabled: StateFlow<Boolean> get() = _enabled

    fun setEnabled(e: Boolean) { _enabled.value = e }
    fun isEnabled(): Boolean = _enabled.value

    fun setRules(newRules: List<NetworkRule>) { _rules.value = newRules }
    fun getRules(): List<NetworkRule> = _rules.value

    /**
     * 根据 URL 查找匹配的规则
     * urlPattern 支持子串匹配和通配符 *
     */
    fun findMatchingRule(url: String): NetworkRule? {
        if (!_enabled.value) return null
        return _rules.value.firstOrNull { rule ->
            rule.enabled && matchPattern(url, rule.urlPattern)
        }
    }

    private fun matchPattern(url: String, pattern: String): Boolean {
        if (pattern.isEmpty()) return false
        // 支持通配符 *
        if (pattern.contains("*")) {
            val regex = pattern
                .replace(".", "\\.")
                .replace("*", ".*")
            return Regex(regex, RegexOption.IGNORE_CASE).containsMatchIn(url)
        }
        // 子串匹配
        return url.contains(pattern, ignoreCase = true)
    }

    /**
     * 应用重定向：返回新的 URL，如果没有匹配规则则返回 null
     */
    fun applyRedirect(originalUrl: String): String? {
        val rule = findMatchingRule(originalUrl) ?: return null
        if (rule.action == RuleAction.REDIRECT && rule.redirectUrl.isNotEmpty()) {
            return rule.redirectUrl
        }
        return null
    }

    /**
     * 应用响应篡改：返回篡改后的响应，如果没有匹配规则则返回 null
     */
    fun applyResponseTampering(url: String): TamperedResponse? {
        val rule = findMatchingRule(url) ?: return null
        if (rule.action == RuleAction.MODIFY_RESPONSE) {
            return TamperedResponse(
                statusCode = rule.newStatusCode,
                body = rule.newResponseBody
            )
        }
        return null
    }

    /**
     * 判断是否应该阻断此 URL
     */
    fun shouldBlock(url: String): Boolean {
        val rule = findMatchingRule(url) ?: return false
        return rule.action == RuleAction.BLOCK
    }
}