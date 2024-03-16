package com.example.bondoman

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.bondoman.databinding.ActivityMainBinding
import com.example.bondoman.ui.home.HomeFragment
import com.example.bondoman.ui.pie_chart.PieChartFragment
import com.example.bondoman.ui.scan.ScanFragment
import com.example.bondoman.ui.settings.SettingsFragment
import com.example.bondoman.ui.transaction.TransactionFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.content.Intent

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        actionBar?.setCustomView(R.layout.header)

        bottomNav = binding.bottomNav
        fab = binding.fab

        bottomNav.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.transaction -> {
                    loadFragment(TransactionFragment())
                    true
                }
                R.id.scan -> {
                    startActivity(Intent(this, ScanActivity::class.java))
                    true
                }
                R.id.pie_chart -> {
                    loadFragment(PieChartFragment())
                    true
                }
                R.id.settings -> {
                    setTheme(R.style.SettingsStyle)
                    loadFragment(SettingsFragment())
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
}
