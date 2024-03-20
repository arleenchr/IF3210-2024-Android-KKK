package com.example.bondoman.ui.settings

import android.content.ContentValues
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceFragmentCompat
import com.example.bondoman.R
import com.example.bondoman.data.LoginDataSource
import com.example.bondoman.databinding.FragmentSettingsBinding
import com.example.bondoman.repository.LoginRepository
import com.example.bondoman.room.TransactionDAO
import com.example.bondoman.room.TransactionDatabase
import com.example.bondoman.service.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.IOException

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var transactionDAO: TransactionDAO

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using View Binding
        transactionDAO = TransactionDatabase.getDatabase(requireContext()).transactionDAO
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

        binding.saveTransactionList.setOnClickListener {
            saveTransactionsToExcel()
        }

        return binding.root
    }

    private fun saveTransactionsToExcel() {
        lifecycleScope.launch(Dispatchers.IO) {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Transactions")

            // Create a header row
            val header = sheet.createRow(0)
            header.createCell(0).setCellValue("Title")
            header.createCell(1).setCellValue("Amount")
            header.createCell(2).setCellValue("Category")
            header.createCell(3).setCellValue("Location")
            header.createCell(4).setCellValue("Created At")

            // Fetch transactions from the database
            val transactions = transactionDAO.getAllTransactionsDirect()

            // Fill the sheet with transaction data
            transactions?.forEachIndexed { index, transaction ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(transaction.title)
                row.createCell(1).setCellValue(transaction.amount.toDouble())
                row.createCell(2).setCellValue(transaction.category)
                row.createCell(3).setCellValue(transaction.location.address)
                row.createCell(4).setCellValue(transaction.createdAt.toString())
            }

            // Write the workbook to a file
            // Prepare ContentValues to create a new MediaStore entry
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "transactions.xlsx")
                put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            // Get a URI for the file to be written
            val resolver = requireContext().contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            try {
                // Use the obtained URI to write the workbook
                uri?.let { uri ->
                    resolver.openOutputStream(uri).use { outputStream ->
                        workbook.write(outputStream)
                    }
                }
                workbook.close()

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Transactions saved to Downloads", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Failed to save transactions: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}

class SettingsPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
