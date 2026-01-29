package com.example.caixin

/**
 * 分类 Fragment
 * 加载财新分类/频道页面
 */
class CategoryFragment : BaseWebViewFragment() {

    companion object {
        private const val CATEGORY_URL = "https://m.caixin.com/channels/"

        fun newInstance(): CategoryFragment {
            return CategoryFragment()
        }
    }

    override fun getUrl(): String = CATEGORY_URL
}
