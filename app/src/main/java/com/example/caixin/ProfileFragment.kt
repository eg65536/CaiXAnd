package com.example.caixin

/**
 * 我的/账号 Fragment
 * 加载财新用户中心页面
 */
class ProfileFragment : BaseWebViewFragment() {

    companion object {
        // 财新用户中心
        private const val PROFILE_URL = "https://user.caixin.com/"

        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }

    override fun getUrl(): String = PROFILE_URL
}
