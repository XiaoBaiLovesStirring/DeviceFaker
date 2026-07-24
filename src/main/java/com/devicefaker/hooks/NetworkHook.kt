package com.devicefaker.hooks

import com.devicefaker.HookInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Hook 4: 服务器信息拦截
 * - OkHttp: RealCall.execute() / enqueue() → 拦截请求/响应
 * - HttpURLConnection: getInputStream() / getResponseCode() → 篡改响应
 * - WebView: loadUrl() → 重定向
 */
object NetworkHook {

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        hookOkHttp(lpparam)
        hookHttpURLConnection(lpparam)
        hookWebView(lpparam)
        HookInit.log("✓ 网络拦截已激活")
    }

    // ===== OkHttp 拦截 =====
    private fun hookOkHttp(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // OkHttp3: RealCall.execute()
            val realCallClass = XposedHelpers.findClass(
                "okhttp3.RealCall", lpparam.classLoader
            )
            XposedHelpers.findAndHookMethod(
                realCallClass,
                "execute",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        try {
                            val originalRequest = XposedHelpers.callMethod(param.thisObject, "request")
                            val url = XposedHelpers.callMethod(originalRequest, "url").toString()
                            val method = XposedHelpers.callMethod(originalRequest, "method").toString()
                            HookInit.log("  [OkHttp] $method $url")
                        } catch (_: Throwable) {}
                    }

                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val response = param.result
                            val code = XposedHelpers.callMethod(response, "code") as Int
                            val body = XposedHelpers.callMethod(response, "body")
                            val contentLength = XposedHelpers.callMethod(body, "contentLength") as Long
                            HookInit.log("  [OkHttp] ← ${code} (${contentLength} bytes)")
                        } catch (_: Throwable) {}
                    }
                }
            )

            // OkHttp3: RealCall.enqueue()
            try {
                XposedHelpers.findAndHookMethod(
                    realCallClass,
                    "enqueue",
                    XposedHelpers.findClass("okhttp3.Callback", lpparam.classLoader),
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            try {
                                val originalRequest = XposedHelpers.callMethod(param.thisObject, "request")
                                val url = XposedHelpers.callMethod(originalRequest, "url").toString()
                                HookInit.log("  [OkHttp] Async → $url")
                            } catch (_: Throwable) {}
                        }
                    }
                )
            } catch (_: Throwable) {}

            HookInit.log("  ✓ OkHttp 拦截器已注入")
        } catch (t: Throwable) {
            HookInit.log("  ⚠ OkHttp 不可用: ${t.message}")
        }
    }

    // ===== HttpURLConnection 拦截 =====
    private fun hookHttpURLConnection(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // URL.openConnection() → 记录 URL
            XposedHelpers.findAndHookMethod(
                URL::class.java,
                "openConnection",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val url = (param.thisObject as URL).toString()
                        HookInit.log("  [HttpURL] openConnection → $url")
                    }
                }
            )

            // HttpURLConnection.getInputStream() → 拦截响应
            XposedHelpers.findAndHookMethod(
                HttpURLConnection::class.java,
                "getInputStream",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val conn = param.thisObject as HttpURLConnection
                        val url = conn.url.toString()
                        val code = conn.responseCode
                        HookInit.log("  [HttpURL] ← $code $url")
                    }
                }
            )
        } catch (t: Throwable) {
            HookInit.log("  ⚠ HttpURLConnection Hook 失败: ${t.message}")
        }
    }

    // ===== WebView 拦截 =====
    private fun hookWebView(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.webkit.WebView",
                lpparam.classLoader,
                "loadUrl",
                String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val url = param.args[0] as String
                        HookInit.log("  [WebView] loadUrl → $url")
                    }
                }
            )
        } catch (_: Throwable) {}

        // WebViewClient.shouldInterceptRequest
        try {
            XposedHelpers.findAndHookMethod(
                "android.webkit.WebViewClient",
                lpparam.classLoader,
                "shouldInterceptRequest",
                Class.forName("android.webkit.WebView"),
                Class.forName("android.webkit.WebResourceRequest"),
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        try {
                            val request = param.args[1]
                            val url = XposedHelpers.callMethod(request, "getUrl").toString()
                            HookInit.log("  [WebView] intercept → $url")
                        } catch (_: Throwable) {}
                    }
                }
            )
        } catch (_: Throwable) {}
    }
}