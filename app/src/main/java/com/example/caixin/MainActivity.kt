package com.example.caixin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    // 缓存 Fragment 实例，避免重复创建
    private val feedFragment by lazy { FeedFragment.newInstance() }
    private val weeklyFragment by lazy { WeeklyFragment.newInstance() }
    private val categoryFragment by lazy { CategoryFragment.newInstance() }
    private val profileFragment by lazy { ProfileFragment.newInstance() }

    private var activeFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置边到边显示
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContentView(R.layout.activity_main)

        initViews()
        setupBottomNavigation()

        // 默认显示资讯 Fragment
        if (savedInstanceState == null) {
            showFragment(feedFragment)
        }
    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation)
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

        // 隐藏当前活动的 Fragment
        activeFragment?.let {
            transaction.hide(it)
        }

        // 如果 Fragment 尚未添加，则添加它
        if (!fragment.isAdded) {
            transaction.add(R.id.fragment_container, fragment)
        } else {
            // 否则显示它
            transaction.show(fragment)
        }

        transaction.commit()
        activeFragment = fragment
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // 如果当前 Fragment 是 WebView Fragment 且可以返回，则返回上一页
        val currentFragment = activeFragment
        if (currentFragment is BaseWebViewFragment && currentFragment.canGoBack()) {
            currentFragment.goBack()
            return
        }

        // 如果不在首页，则回到首页
        if (activeFragment !== feedFragment) {
            bottomNavigation.selectedItemId = R.id.nav_feed
            return
        }

        // 否则执行默认行为
        @Suppress("DEPRECATION")
        super.onBackPressed()
    }
}
