package com.example.caixin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), CategoryFragment.OnChannelClickListener {

    private lateinit var bottomNavigation: BottomNavigationView

    // 缓存 Fragment 实例，避免重复创建
    private val feedFragment by lazy { FeedFragment.newInstance() }
    private val weeklyFragment by lazy { WeeklyFragment.newInstance() }
    private val categoryFragment by lazy {
        CategoryFragment.newInstance().also {
            it.onChannelClickListener = this
        }
    }
    private val profileFragment by lazy { ProfileFragment.newInstance() }

    private var activeFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 启用边到边显示
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_main)

        setupWindowInsets()
        initViews()
        setupBottomNavigation()

        // 默认显示资讯 Fragment
        if (savedInstanceState == null) {
            showFragment(feedFragment)
        }
    }

    private fun setupWindowInsets() {
        val rootView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = insets.top)
            windowInsets
        }
    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation)

        // 处理底部导航栏的系统栏内边距
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigation) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = insets.bottom)
            windowInsets
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_feed -> {
                    showFragment(feedFragment)
                    true
                }
                R.id.nav_weekly -> {
                    showFragment(weeklyFragment)
                    true
                }
                R.id.nav_category -> {
                    showFragment(categoryFragment)
                    true
                }
                R.id.nav_profile -> {
                    showFragment(profileFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        if (activeFragment === fragment) return

        val transaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )

        // 隐藏当前活动的 Fragment
        activeFragment?.let {
            transaction.hide(it)
        }

        // 如果 Fragment 尚未添加，则添加它
        if (!fragment.isAdded) {
            transaction.add(R.id.fragment_container, fragment)
        } else {
            transaction.show(fragment)
        }

        transaction.commit()
        activeFragment = fragment
    }

    // 处理分类页面点击事件
    override fun onChannelClick(channel: CategoryFragment.Channel) {
        // 切换到首页 Fragment 并加载选中的频道
        feedFragment.loadUrl(channel.url)
        bottomNavigation.selectedItemId = R.id.nav_feed
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val currentFragment = activeFragment

        // 如果当前是 WebView Fragment 且可以返回，则返回上一页
        if (currentFragment is BaseWebViewFragment && currentFragment.canGoBack()) {
            currentFragment.goBack()
            return
        }

        // 如果不在首页，则回到首页
        if (activeFragment !== feedFragment) {
            bottomNavigation.selectedItemId = R.id.nav_feed
            return
        }

        @Suppress("DEPRECATION")
        super.onBackPressed()
    }
}
