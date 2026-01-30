package com.example.caixin

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.google.android.material.progressindicator.LinearProgressIndicator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

abstract class BaseWebViewFragment : Fragment() {

    protected lateinit var webView: WebView
    protected lateinit var progressBar: LinearProgressIndicator
    protected lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var isWebViewInitialized = false
    private var pendingUrl: String? = null

    companion object {
        private const val CAIXIN_HOST = "caixin.com"

        // 桌面版 Chrome User Agent
        private const val DESKTOP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

        // 全面优化页面的 JavaScript
        private const val OPTIMIZE_PAGE_JS = """
            (function() {
                'use strict';

                // ========== 1. 隐藏所有 App 下载提示 ==========
                var appSelectors = [
                    // 通用 App 下载提示
                    '.app-download', '.download-app', '.app-banner', '.app-tip',
                    '.open-app', '.openApp', '.app-guide', '.smart-banner',
                    '.app-smart-banner', '.bottom-app-download', '.fixed-download',
                    '[class*="app-download"]', '[class*="download-app"]',
                    '[class*="open-app"]', '[class*="openApp"]',
                    '[id*="app-download"]', '[id*="download-app"]',
                    '[id*="open-app"]', '[id*="openApp"]',
                    // 财新特定
                    '.app-download-bar', '.app-download-tip', '.download-banner',
                    '.appTip', '.appBanner', '.app_download', '.download_app',
                    '.guide-app', '.guideApp', '.app-float', '.float-app',
                    '.app-popup', '.popup-app', '.app-modal', '.modal-app',
                    // 浮动层
                    '[class*="float"]', '[class*="popup"]', '[class*="modal"]',
                    // 底部固定栏
                    '.fixed-bottom', '.bottom-fixed', '.footer-fixed',
                    '[class*="fixed-bottom"]', '[class*="bottom-bar"]'
                ];

                function hideAppElements() {
                    appSelectors.forEach(function(selector) {
                        try {
                            document.querySelectorAll(selector).forEach(function(el) {
                                if (el && (
                                    el.textContent.indexOf('下载') !== -1 ||
                                    el.textContent.indexOf('打开') !== -1 ||
                                    el.textContent.indexOf('App') !== -1 ||
                                    el.textContent.indexOf('APP') !== -1 ||
                                    el.textContent.indexOf('客户端') !== -1 ||
                                    el.className.indexOf('app') !== -1 ||
                                    el.className.indexOf('download') !== -1 ||
                                    el.id.indexOf('app') !== -1 ||
                                    el.id.indexOf('download') !== -1
                                )) {
                                    el.style.cssText = 'display: none !important; visibility: hidden !important; height: 0 !important; opacity: 0 !important;';
                                    el.remove();
                                }
                            });
                        } catch(e) {}
                    });
                }

                // ========== 2. 优化页面样式 ==========
                function optimizePageStyle() {
                    var style = document.createElement('style');
                    style.textContent = `
                        /* 隐藏 App 相关元素 */
                        [class*="app-download"], [class*="download-app"],
                        [class*="open-app"], [class*="openApp"],
                        [id*="app-download"], [id*="download-app"],
                        .app-banner, .app-tip, .app-guide, .smart-banner {
                            display: none !important;
                            visibility: hidden !important;
                            height: 0 !important;
                            max-height: 0 !important;
                            overflow: hidden !important;
                            opacity: 0 !important;
                            pointer-events: none !important;
                        }

                        /* 优化阅读体验 */
                        body {
                            -webkit-text-size-adjust: 100%;
                            text-size-adjust: 100%;
                        }

                        /* 优化图片 */
                        img {
                            max-width: 100%;
                            height: auto;
                        }

                        /* 隐藏可能的广告 */
                        [class*="ad-"], [class*="-ad"], [id*="ad-"], [id*="-ad"],
                        [class*="advertisement"], [class*="广告"] {
                            display: none !important;
                        }

                        /* 优化滚动 */
                        html, body {
                            scroll-behavior: smooth;
                            -webkit-overflow-scrolling: touch;
                        }

                        /* 移除可能的底部占位 */
                        body::after {
                            display: none !important;
                        }
                    `;
                    document.head.appendChild(style);
                }

                // ========== 3. 拦截 App 跳转 ==========
                function interceptAppLinks() {
                    // 拦截打开 App 的链接
                    document.addEventListener('click', function(e) {
                        var target = e.target;
                        while (target && target !== document) {
                            var href = target.getAttribute && target.getAttribute('href');
                            if (href && (
                                href.indexOf('itunes.apple.com') !== -1 ||
                                href.indexOf('play.google.com') !== -1 ||
                                href.indexOf('app.caixin.com') !== -1 ||
                                href.indexOf('://caixin') !== -1 ||
                                href.indexOf('market://') !== -1 ||
                                href.indexOf('intent://') !== -1
                            )) {
                                e.preventDefault();
                                e.stopPropagation();
                                return false;
                            }
                            target = target.parentNode;
                        }
                    }, true);

                    // 阻止 App 唤起
                    var originalOpen = window.open;
                    window.open = function(url) {
                        if (url && (
                            url.indexOf('itunes') !== -1 ||
                            url.indexOf('play.google') !== -1 ||
                            url.indexOf('://caixin') !== -1
                        )) {
                            return null;
                        }
                        return originalOpen.apply(this, arguments);
                    };
                }

                // ========== 4. 移除 WebView 检测 ==========
                function removeWebViewDetection() {
                    // 删除 WebView 标识
                    Object.defineProperty(navigator, 'userAgent', {
                        get: function() {
                            return navigator.userAgent.replace(/; wv\)/, ')').replace(/Version\/[\d.]+/, '');
                        }
                    });

                    // 模拟真实浏览器环境
                    if (!window.chrome) {
                        window.chrome = { runtime: {} };
                    }
                }

                // ========== 5. 执行优化 ==========
                function init() {
                    hideAppElements();
                    optimizePageStyle();
                    interceptAppLinks();
                    removeWebViewDetection();
                }

                // 立即执行
                init();

                // DOM 加载完成后执行
                if (document.readyState === 'loading') {
                    document.addEventListener('DOMContentLoaded', init);
                }

                // 页面完全加载后执行
                window.addEventListener('load', function() {
                    init();
                    // 延迟再次执行，处理动态加载的内容
                    setTimeout(init, 500);
                    setTimeout(init, 1500);
                    setTimeout(init, 3000);
                });

                // 监听 DOM 变化
                var observer = new MutationObserver(function(mutations) {
                    hideAppElements();
                });

                observer.observe(document.documentElement, {
                    childList: true,
                    subtree: true
                });
            })();
        """
    }

    abstract fun getUrl(): String

    // 是否使用桌面版，默认使用桌面版
    open fun useDesktopMode(): Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_webview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.webView)
        progressBar = view.findViewById(R.id.progressBar)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        setupWebView()
        setupSwipeRefresh()

        if (!isWebViewInitialized) {
            val url = pendingUrl ?: getUrl()
            pendingUrl = null
            webView.loadUrl(url)
            isWebViewInitialized = true
        }
    }

    fun loadUrl(url: String) {
        if (::webView.isInitialized) {
            webView.loadUrl(url)
        } else {
            pendingUrl = url
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT

            // 禁用缩放，更像原生 App
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false

            // 适配屏幕
            useWideViewPort = true
            loadWithOverviewMode = true

            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

            // 使用桌面版 User Agent 或移除 WebView 标识
            userAgentString = if (useDesktopMode()) {
                DESKTOP_USER_AGENT
            } else {
                userAgentString
                    .replace("; wv)", ")")
                    .replace("Version/4.0 ", "")
            }

            allowFileAccess = true
            allowContentAccess = true
            setGeolocationEnabled(true)
            mediaPlaybackRequiresUserGesture = false

            // 开启硬件加速
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }

        // 启用 Cookie
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false

                // 拦截 App Store / Play Store 链接
                if (url.contains("itunes.apple.com") ||
                    url.contains("play.google.com") ||
                    url.contains("market://") ||
                    url.startsWith("intent://") ||
                    url.startsWith("caixin://")) {
                    return true
                }

                // 财新相关域名在 WebView 内打开
                if (url.contains(CAIXIN_HOST)) {
                    return false
                }

                // 其他链接使用外部浏览器
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
                // 尽早注入优化脚本
                view?.evaluateJavascript(OPTIMIZE_PAGE_JS, null)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false
                // 页面加载完成后再次注入
                view?.evaluateJavascript(OPTIMIZE_PAGE_JS, null)
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (::progressBar.isInitialized) {
                    progressBar.progress = newProgress
                    if (newProgress == 100) {
                        progressBar.visibility = View.GONE
                    }
                }
            }
        }

        // 设置滚动条样式
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            webView.reload()
        }

        swipeRefreshLayout.setColorSchemeResources(
            R.color.caixin_primary
        )

        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.surface)
    }

    fun canGoBack(): Boolean = if (::webView.isInitialized) webView.canGoBack() else false

    fun goBack() {
        if (::webView.isInitialized) {
            webView.goBack()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::webView.isInitialized) {
            webView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::webView.isInitialized) {
            webView.onPause()
        }
    }

    override fun onDestroyView() {
        if (::webView.isInitialized) {
            webView.apply {
                stopLoading()
                loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
                clearHistory()
                (parent as? ViewGroup)?.removeView(this)
                destroy()
            }
        }
        super.onDestroyView()
    }
}
