package com.example.bondoman

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.bondoman.databinding.ActivityMainBinding
import com.example.bondoman.ui.home.HomeFragment
import com.example.bondoman.ui.pie_chart.PieChartFragment
import com.example.bondoman.ui.settings.SettingsFragment
import com.example.bondoman.ui.transaction.TransactionFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.content.Intent
import com.example.bondoman.service.RetrofitClient
import com.example.bondoman.service.TokenCheckService
import com.example.bondoman.utils.NetworkUtils
import com.google.android.libraries.places.api.Places

class MainActivity : AppCompatActivity() {

    lateinit var bottomNav: BottomNavigationView
    private lateinit var fab: FloatingActionButton
    private lateinit var networkUtils: NetworkUtils

    private lateinit var homeFragment: HomeFragment
    private lateinit var transactionFragment: TransactionFragment
    private lateinit var pieChartFragment: PieChartFragment
    private lateinit var settingsFragment: SettingsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val serviceIntent = Intent(this, TokenCheckService::class.java)
        startService(serviceIntent)

        val actionBar: ActionBar? = supportActionBar

        // Hide the action bar
        actionBar?.hide()

        // Set shared preferences
        RetrofitClient.sharedPreferences = getSharedPreferences("identity", Context.MODE_PRIVATE)

        // Initialize Places SDK
        Places.initialize(applicationContext, BuildConfig.API_KEY)

        // Add network sensing
        networkUtils = NetworkUtils(this)
        networkUtils.registerNetworkCallback()

        bottomNav = binding.bottomNav
        fab = binding.fab

        homeFragment = HomeFragment()
        transactionFragment = TransactionFragment()
        pieChartFragment = PieChartFragment()
        settingsFragment = SettingsFragment()

        bottomNav.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    loadFragment(homeFragment)
                    true
                }
                R.id.transaction -> {
                    loadFragment(transactionFragment)
                    true
                }
                R.id.scan -> {
                    startActivity(Intent(this, ScanActivity::class.java))
                    true
                }
                R.id.pie_chart -> {
                    loadFragment(pieChartFragment)
                    true
                }
                R.id.settings -> {
                    setTheme(R.style.SettingsStyle)
                    loadFragment(settingsFragment)
                    true
                }
                else -> false
            }
        }

        // Set OnClickListener for the FloatingActionButton
        fab.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }

        // Load the initial fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        networkUtils.unregisterNetworkCallback()
    }
}
