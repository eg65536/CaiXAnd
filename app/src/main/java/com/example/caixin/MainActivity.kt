package com.example.caixin

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    companion object {
        private const val CAIXIN_URL = "https://m.caixin.com/"
        private const val CAIXIN_HOST = "caixin.com"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupWebView()
        setupSwipeRefresh()

        // 加载财新网
        webView.loadUrl(CAIXIN_URL)
    }

    private fun initViews() {
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            // 启用 JavaScript
            javaScriptEnabled = true

            // 启用 DOM 存储
            domStorageEnabled = true

            // 启用数据库
            databaseEnabled = true

            // 设置缓存模式
            cacheMode = WebSettings.LOAD_DEFAULT

            // 支持缩放
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false

            // 自适应屏幕
            useWideViewPort = true
            loadWithOverviewMode = true

            // 允许混合内容 (HTTPS 页面加载 HTTP 资源)
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

            // 设置 User Agent，模拟移动端浏览器
            userAgentString = userAgentString + " CaiXinApp/1.0"

            // 允许文件访问
            allowFileAccess = true
            allowContentAccess = true

            // 启用地理定位
            setGeolocationEnabled(true)

            // 媒体播放不需要用户手势
            mediaPlaybackRequiresUserGesture = false
        }

        // 启用 Cookie
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        // 设置 WebViewClient
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false

                // 如果是财新网域名，在 WebView 内打开
                if (url.contains(CAIXIN_HOST)) {
                    return false
                }

                // 其他链接使用外部浏览器打开
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
            }
        }

        // 设置 WebChromeClient 用于处理进度和其他 Chrome 功能
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

        // 设置刷新指示器颜色
        swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // 处理返回键，如果 WebView 可以返回，则返回上一页
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onDestroy() {
        // 清理 WebView
        webView.apply {
            loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
            clearHistory()
            removeAllViews()
            destroy()
        }
        super.onDestroy()
    }
}
