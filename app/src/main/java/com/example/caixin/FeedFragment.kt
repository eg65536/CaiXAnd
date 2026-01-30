package com.example.caixin

/**
 * 资讯信息流 Fragment
 * 加载财新网首页
 */
class FeedFragment : BaseWebViewFragment() {

    companion object {
        // 使用桌面版首页
        private const val FEED_URL = "https://www.caixin.com/"

        fun newInstance(): FeedFragment {
            return FeedFragment()
        }
    }

    override fun getUrl(): String = FEED_URL
}
