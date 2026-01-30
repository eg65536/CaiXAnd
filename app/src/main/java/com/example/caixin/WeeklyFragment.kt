package com.example.caixin

/**
 * 财新周刊 Fragment
 * 加载财新周刊页面
 */
class WeeklyFragment : BaseWebViewFragment() {

    companion object {
        // 财新周刊独立域名
        private const val WEEKLY_URL = "https://weekly.caixin.com/"

        fun newInstance(): WeeklyFragment {
            return WeeklyFragment()
        }
    }

    override fun getUrl(): String = WEEKLY_URL
}
