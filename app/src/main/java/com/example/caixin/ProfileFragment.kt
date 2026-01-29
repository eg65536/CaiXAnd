package com.example.caixin

/**
 * 我的/账号 Fragment
 * 加载财新账号设置页面
 */
class ProfileFragment : BaseWebViewFragment() {

    companion object {
        private const val PROFILE_URL = "https://m.caixin.com/m/user/"

        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }

    override fun getUrl(): String = PROFILE_URL
}
