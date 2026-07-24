package com.devicefaker.hooks

import com.devicefaker.HookInit
import com.devicefaker.model.NetworkRuleEngine
import com.devicefaker.model.SpoofConfig
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Hook 4: 网络拦截 — 真正的篡改引擎
 * 支持: OkHttp 请求重定向/响应篡改, HttpURLConnection 响应篡改, WebView 重定向
 */
object NetworkHook {

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam, cfg: SpoofConfig) {
        if (!cfg.networkIntercept) {
            HookInit.log("○ 网络拦截已禁用")
            return
        }

        hookOkHttpExecute(lpparam)
        hookOkHttpEnqueue(lpparam)
        hookHttpURLConnection(lpparam)
        hookWebView(lpparam)

        HookInit.log("✓ 网络拦截已激活 (${NetworkRuleEngine.getRules().size} 条规则)")
    }

    // ===== OkHttp RealCall.execute() — 同步请求 =====
    private fun hookOkHttpExecute(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val realCallClass = XposedHelpers.findClass("okhttp3.RealCall", lpparam.classLoader)

            XposedHelpers.findAndHookMethod(realCallClass, "execute",
                object : XC_MethodHook() {

                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!NetworkRuleEngine.isEnabled()) return
                        try {
                            val originalRequest = XposedHelpers.callMethod(param.thisObject, "request")
                            val url = XposedHelpers.callMethod(originalRequest, "url").toString()
                            val method = XposedHelpers.callMethod(originalRequest, "method").toString()

                            // 检查是否需要阻断
                            if (NetworkRuleEngine.shouldBlock(url)) {
                                HookInit.log("  [BLOCK] $method $url")
                                // 抛异常阻断请求
                                throw java.io.IOException("Request blocked by DeviceFaker")
                            }

                            // 检查是否需要重定向
                            val redirectUrl = NetworkRuleEngine.applyRedirect(url)
                            if (redirectUrl != null) {
                                HookInit.log("  [REDIRECT] $url → $redirectUrl")
                                // 构建新 URL 的 Request
                                val okHttpUrlClass = XposedHelpers.findClass("okhttp3.HttpUrl", lpparam.classLoader)
                                val newHttpUrl = XposedHelpers.callStaticMethod(okHttpUrlClass, "parse", redirectUrl)
                                val newRequest = XposedHelpers.callMethod(originalRequest, "newBuilder")
                                XposedHelpers.callMethod(newRequest, "url", newHttpUrl)
                                val builtRequest = XposedHelpers.callMethod(newRequest, "build")
                                // 替换 RealCall 中的 originalRequest
                                XposedHelpers.setObjectField(param.thisObject, "originalRequest", builtRequest)
                            }
                        } catch (e: java.io.IOException) {
                            param.throwable = e
                            param.result = null
                        } catch (_: Throwable) {}
                    }

                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (!NetworkRuleEngine.isEnabled()) return
                        val response = param.result ?: return
                        try {
                            val request = XposedHelpers.callMethod(response, "request")
                            val url = XposedHelpers.callMethod(request, "url").toString()

                            val tampered = NetworkRuleEngine.applyResponseTampering(url)
                            if (tampered != null) {
                                HookInit.log("  [TAMPER] $url → ${tampered.statusCode} (${tampered.body.length} bytes)")

                                val mediaTypeClass = XposedHelpers.findClass("okhttp3.MediaType", lpparam.classLoader)
                                val mediaType = XposedHelpers.callStaticMethod(mediaTypeClass, "parse", tampered.contentType)
                                val responseBodyClass = XposedHelpers.findClass("okhttp3.ResponseBody", lpparam.classLoader)
                                val newBody = XposedHelpers.callStaticMethod(responseBodyClass, "create", mediaType, tampered.body)

                                val builder = XposedHelpers.callMethod(response, "newBuilder")
                                XposedHelpers.callMethod(builder, "body", newBody)
                                XposedHelpers.callMethod(builder, "code", tampered.statusCode)
                                // 移除 Content-Encoding 避免解压错误
                                XposedHelpers.callMethod(builder, "removeHeader", "Content-Encoding")
                                param.result = XposedHelpers.callMethod(builder, "build")
                            }
                        } catch (_: Throwable) {}
                    }
                }
            )
            HookInit.log("  ✓ OkHttp execute() 已拦截")
        } catch (t: Throwable) {
            HookInit.log("  ⚠ OkHttp execute: ${t.message}")
        }
    }

    // ===== OkHttp RealCall.enqueue() — 异步请求 =====
    private fun hookOkHttpEnqueue(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val realCallClass = XposedHelpers.findClass("okhttp3.RealCall", lpparam.classLoader)
            val callbackClass = XposedHelpers.findClass("okhttp3.Callback", lpparam.classLoader)

            XposedHelpers.findAndHookMethod(realCallClass, "enqueue", callbackClass,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!NetworkRuleEngine.isEnabled()) return
                        try {
                            val originalRequest = XposedHelpers.callMethod(param.thisObject, "request")
                            val url = XposedHelpers.callMethod(originalRequest, "url").toString()

                            // 检查重定向
                            val redirectUrl = NetworkRuleEngine.applyRedirect(url)
                            if (redirectUrl != null) {
                                HookInit.log("  [ASYNC REDIRECT] $url → $redirectUrl")
                                val okHttpUrlClass = XposedHelpers.findClass("okhttp3.HttpUrl", lpparam.classLoader)
                                val newHttpUrl = XposedHelpers.callStaticMethod(okHttpUrlClass, "parse", redirectUrl)
                                val newRequest = XposedHelpers.callMethod(originalRequest, "newBuilder")
                                XposedHelpers.callMethod(newRequest, "url", newHttpUrl)
                                XposedHelpers.setObjectField(param.thisObject, "originalRequest",
                                    XposedHelpers.callMethod(newRequest, "build"))
                            }
                        } catch (_: Throwable) {}
                    }
                }
            )
            HookInit.log("  ✓ OkHttp enqueue() 已拦截")
        } catch (t: Throwable) {
            HookInit.log("  ⚠ OkHttp enqueue: ${t.message}")
        }
    }

    // ===== HttpURLConnection 拦截 =====
    private fun hookHttpURLConnection(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Hook getInputStream() — 篡改响应流
        try {
            XposedHelpers.findAndHookMethod(
                HttpURLConnection::class.java, "getInputStream",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (!NetworkRuleEngine.isEnabled()) return
                        try {
                            val conn = param.thisObject as HttpURLConnection
                            val url = conn.url.toString()

                            val tampered = NetworkRuleEngine.applyResponseTampering(url)
                            if (tampered != null) {
                                HookInit.log("  [HttpURL TAMPER] $url → ${tampered.statusCode}")
                                param.result = ByteArrayInputStream(tampered.body.toByteArray())
                            }
                        } catch (_: Throwable) {}
                    }
                }
            )
        } catch (t: Throwable) {
            HookInit.log("  ⚠ HttpURL getInputStream: ${t.message}")
        }

        // Hook getResponseCode() — 篡改状态码
        try {
            XposedHelpers.findAndHookMethod(
                HttpURLConnection::class.java, "getResponseCode",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (!NetworkRuleEngine.isEnabled()) return
                        try {
                            val conn = param.thisObject as HttpURLConnection
                            val url = conn.url.toString()
                            val tampered = NetworkRuleEngine.applyResponseTampering(url)
                            if (tampered != null) {
                                param.result = tampered.statusCode
                            }
                        } catch (_: Throwable) {}
                    }
                }
            )
        } catch (t: Throwable) {
            HookInit.log("  ⚠ HttpURL getResponseCode: ${t.message}")
        }

        HookInit.log("  ✓ HttpURLConnection 已拦截")
    }

    // ===== WebView 拦截 =====
    private fun hookWebView(lpparam: XC_LoadPackage.LoadPackageParam) {
        // shouldInterceptRequest — 拦截并篡改
        try {
            XposedHelpers.findAndHookMethod(
                "android.webkit.WebViewClient", lpparam.classLoader,
                "shouldInterceptRequest",
                Class.forName("android.webkit.WebView"),
                Class.forName("android.webkit.WebResourceRequest"),
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!NetworkRuleEngine.isEnabled()) return
                        try {
                            val request = param.args[1]
                            val url = XposedHelpers.callMethod(request, "getUrl").toString()
                            val tampered = NetworkRuleEngine.applyResponseTampering(url)
                            if (tampered != null) {
                                HookInit.log("  [WebView TAMPER] $url")
                                val mimeType = when {
                                    url.contains(".json") -> "application/json"
                                    url.contains(".html") -> "text/html"
                                    else -> "text/plain"
                                }
                                val encoding = "UTF-8"
                                val webResourceResponseClass = XposedHelpers.findClass(
                                    "android.webkit.WebResourceResponse",
                                    lpparam.classLoader
                                )
                                param.result = XposedHelpers.newInstance(
                                    webResourceResponseClass,
                                    mimeType, encoding,
                                    ByteArrayInputStream(tampered.body.toByteArray())
                                )
                            }
                        } catch (_: Throwable) {}
                    }
                }
            )
        } catch (_: Throwable) {}

        // loadUrl() — 重定向
        try {
            XposedHelpers.findAndHookMethod(
                "android.webkit.WebView", lpparam.classLoader,
                "loadUrl", String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!NetworkRuleEngine.isEnabled()) return
                        val url = param.args[0] as String
                        val redirectUrl = NetworkRuleEngine.applyRedirect(url)
                        if (redirectUrl != null) {
                            HookInit.log("  [WebView REDIRECT] $url → $redirectUrl")
                            param.args[0] = redirectUrl
                        }
                    }
                }
            )
        } catch (_: Throwable) {}

        HookInit.log("  ✓ WebView 已拦截")
    }
}