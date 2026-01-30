package com.example.caixin

import android.os.Bundle
import android.view.View

/**
 * 财新周刊 Fragment
 * 加载财新周刊页面，并优化阅读体验
 */
class WeeklyFragment : BaseWebViewFragment() {

    companion object {
        // 财新周刊 - 使用杂志频道页面，有更好的期刊列表
        private const val WEEKLY_URL = "https://weekly.caixin.com/"

        // 周刊页面专用的优化脚本
        private const val WEEKLY_OPTIMIZE_JS = """
            (function() {
                'use strict';

                function optimizeWeeklyPage() {
                    // 添加样式优化阅读体验
                    var style = document.createElement('style');
                    style.textContent = `
                        /* 优化文章内容显示 */
                        .article-content, .content, .text, .main-content,
                        [class*="article"], [class*="content"] {
                            font-size: 18px !important;
                            line-height: 1.8 !important;
                            color: #333 !important;
                        }

                        /* 优化标题 */
                        h1, h2, h3, .title, [class*="title"] {
                            font-size: 22px !important;
                            font-weight: bold !important;
                            line-height: 1.4 !important;
                            margin-bottom: 16px !important;
                        }

                        /* 优化图片 */
                        img {
                            max-width: 100% !important;
                            height: auto !important;
                            display: block;
                            margin: 16px auto;
                        }

                        /* 隐藏不需要的元素 */
                        .sidebar, .ad, .advertisement, .recommend,
                        [class*="sidebar"], [class*="ad-"], [class*="recommend"],
                        .share-bar, .comment-section, footer,
                        [class*="share"], [class*="comment"] {
                            display: none !important;
                        }

                        /* 优化段落间距 */
                        p {
                            margin-bottom: 16px !important;
                            text-align: justify;
                        }

                        /* 优化列表样式 */
                        .issue-list, .magazine-list, [class*="list"] {
                            padding: 0 !important;
                        }

                        .issue-list li, .magazine-list li {
                            padding: 12px 0 !important;
                            border-bottom: 1px solid #eee;
                        }
                    `;
                    document.head.appendChild(style);

                    // 移除付费墙遮罩（如果有）
                    var paywalls = document.querySelectorAll('[class*="paywall"], [class*="subscribe"], [class*="login-prompt"]');
                    paywalls.forEach(function(el) {
                        el.style.display = 'none';
                    });

                    // 展开被折叠的内容
                    var collapsedContent = document.querySelectorAll('[class*="collapsed"], [class*="truncated"], [class*="fold"]');
                    collapsedContent.forEach(function(el) {
                        el.style.maxHeight = 'none';
                        el.style.overflow = 'visible';
                    });
                }

                // 立即执行
                optimizeWeeklyPage();

                // 页面加载完成后执行
                if (document.readyState === 'loading') {
                    document.addEventListener('DOMContentLoaded', optimizeWeeklyPage);
                }
                window.addEventListener('load', function() {
                    optimizeWeeklyPage();
                    setTimeout(optimizeWeeklyPage, 1000);
                });

                // 监听 DOM 变化
                var observer = new MutationObserver(optimizeWeeklyPage);
                observer.observe(document.documentElement, { childList: true, subtree: true });
            })();
        """

        fun newInstance(): WeeklyFragment {
            return WeeklyFragment()
        }
    }

    override fun getUrl(): String = WEEKLY_URL

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 页面加载完成后注入周刊专用优化脚本
        webView.webViewClient = object : android.webkit.WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: android.webkit.WebView?,
                request: android.webkit.WebResourceRequest?
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
                val caixinHosts = listOf("caixin.com", "caixinglobal.com", "caixin.global")
                if (caixinHosts.any { url.contains(it) }) {
                    return false
                }

                // 其他链接使用外部浏览器
                try {
                    startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return true
            }

            override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false

                // 注入通用优化脚本和周刊专用脚本
                view?.evaluateJavascript(WEEKLY_OPTIMIZE_JS, null)
            }
        }
    }
}
