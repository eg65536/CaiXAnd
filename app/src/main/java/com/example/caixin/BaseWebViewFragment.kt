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
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

abstract class BaseWebViewFragment : Fragment() {

    protected lateinit var webView: WebView
    protected lateinit var progressBar: ProgressBar
    protected lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var isWebViewInitialized = false

    companion object {
        private const val CAIXIN_HOST = "caixin.com"

        // 隐藏安装提示和广告的 JavaScript
        private const val HIDE_ELEMENTS_JS = """
            (function() {
                // 隐藏安装 App 提示
                var selectors = [
                    '.app-download',
                    '.download-app',
                    '.app-banner',
                    '.app-tip',
                    '.open-app',
                    '.openApp',
                    '[class*="app-download"]',
                    '[class*="download-app"]',
                    '[class*="open-app"]',
                    '[id*="app-download"]',
                    '[id*="download-app"]',
                    '.bottom-app-download',
                    '.fixed-download',
                    '.app-guide',
                    '.smart-banner',
                    '.app-smart-banner'
                ];

                selectors.forEach(function(selector) {
                    var elements = document.querySelectorAll(selector);
                    elements.forEach(function(el) {
                        el.style.display = 'none';
                        el.remove();
                    });
                });

                // 监听 DOM 变化，持续隐藏
                var observer = new MutationObserver(function(mutations) {
                    selectors.forEach(function(selector) {
                        var elements = document.querySelectorAll(selector);
                        elements.forEach(function(el) {
                            el.style.display = 'none';
                            el.remove();
                        });
                    });
                });

                observer.observe(document.body, {
                    childList: true,
                    subtree: true
                });
            })();
        """
    }

    abstract fun getUrl(): String

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
            webView.loadUrl(getUrl())
            isWebViewInitialized = true
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            userAgentString = userAgentString.replace("; wv", "")
            allowFileAccess = true
            allowContentAccess = true
            setGeolocationEnabled(true)
            mediaPlaybackRequiresUserGesture = false
        }

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

                if (url.contains(CAIXIN_HOST)) {
                    return false
                }

                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false

                // 注入 JavaScript 隐藏安装提示
                view?.evaluateJavascript(HIDE_ELEMENTS_JS, null)
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            webView.reload()
        }

        swipeRefreshLayout.setColorSchemeResources(
            R.color.caixin_primary,
            R.color.caixin_secondary
        )
    }

    fun canGoBack(): Boolean = webView.canGoBack()

    fun goBack() {
        webView.goBack()
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
