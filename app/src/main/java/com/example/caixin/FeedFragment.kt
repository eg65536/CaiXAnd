package com.example.caixin

/**
 * 资讯信息流 Fragment
 * 加载财新网移动端首页
 */
class FeedFragment : BaseWebViewFragment() {

    companion object {
        private const val FEED_URL = "https://m.caixin.com/"

        fun newInstance(): FeedFragment {
            return FeedFragment()
        }
    }

    override fun getUrl(): String = FEED_URL
}
