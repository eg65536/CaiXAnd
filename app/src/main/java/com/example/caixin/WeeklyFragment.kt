package com.example.caixin

/**
 * 财新周刊 Fragment
 * 加载财新周刊页面
 */
class WeeklyFragment : BaseWebViewFragment() {

    companion object {
        private const val WEEKLY_URL = "https://m.caixin.com/weekly/"

        fun newInstance(): WeeklyFragment {
            return WeeklyFragment()
        }
    }

    override fun getUrl(): String = WEEKLY_URL
}
