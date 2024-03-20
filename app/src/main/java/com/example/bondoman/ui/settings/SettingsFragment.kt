package com.example.bondoman.ui.settings

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceFragmentCompat
import com.example.bondoman.R
import com.example.bondoman.data.LoginDataSource
import com.example.bondoman.databinding.FragmentSettingsBinding
import com.example.bondoman.repository.LoginRepository
import com.example.bondoman.room.TransactionDAO
import com.example.bondoman.room.TransactionDatabase
import com.example.bondoman.room.TransactionEntity
import com.example.bondoman.service.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var transactionDAO: TransactionDAO
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using View Binding
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        transactionDAO = TransactionDatabase.getDatabase(requireContext()).transactionDAO

        binding.saveTransactionList.setOnClickListener{
            saveTransactionListToExcel()
        }

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

    private fun checkAndRequestStoragePermission() {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            saveTransactionListToExcel()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), REQUEST_STORAGE_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveTransactionListToExcel()
            } else {
                Toast.makeText(requireContext(), "Storage permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveTransactionListToExcel(){
        transactionDAO.getAllTransaction().observe(viewLifecycleOwner) { transactions ->
            lifecycleScope.launch(Dispatchers.IO) {
                val fileName = "transaction_list.xlsx"
                val folder = File(requireContext().filesDir, "Transactions")
                folder.mkdirs()

                val file = File(folder, fileName)

                try {
                    val outputStream = FileOutputStream(file)
                    outputStream.write("Title,Amount,Category,Location\n".toByteArray())

                    transactions.forEach { transaction ->
                        val line = "${transaction.title},${transaction.amount},${transaction.category},${transaction.location}\n"
                        outputStream.write(line.toByteArray())
                    }

                    outputStream.close()

                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Excel file downloaded to internal storage", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Error saving transaction list", Toast.LENGTH_SHORT).show()
                    }
                }
                Log.d("SaveToExcel", "File path: ${file.absolutePath}")
            }
        }
    }


    companion object {
        private const val REQUEST_STORAGE_PERMISSION = 100
    }
}

class SettingsPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
