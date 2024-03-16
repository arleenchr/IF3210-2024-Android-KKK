package com.example.bondoman.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceFragmentCompat
import com.example.bondoman.R
import com.example.bondoman.data.LoginDataSource
import com.example.bondoman.databinding.FragmentSettingsBinding
import com.example.bondoman.repository.LoginRepository
import com.example.bondoman.service.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using View Binding
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        binding.logout.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                val loginRepo = LoginRepository(LoginDataSource(), requireContext())
                val editor: SharedPreferences.Editor = RetrofitClient.sharedPreferences.edit()

                // Remove token
                editor.putString("token", "")
                editor.apply()

                // Logout
                loginRepo.logout()
            }
        }

        return binding.root
    }
}

class SettingsPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
