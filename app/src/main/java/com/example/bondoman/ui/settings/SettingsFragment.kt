package com.example.bondoman.ui.settings

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.example.bondoman.service.RetrofitClient.sharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.random.Random

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var transactionDAO: TransactionDAO
    private var fileFormatOptions = arrayOf("XLS", "XLSX")
    private val RANDOMIZE_TRANSACTIONS_ACTION = "com.example.bondoman.ACTION_RANDOMIZE_TRANSACTIONS"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using View Binding
        transactionDAO = TransactionDatabase.getDatabase(requireContext()).transactionDAO
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        binding.logout.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                val loginRepo = LoginRepository(LoginDataSource(), requireContext())
                val editor: SharedPreferences.Editor = sharedPreferences.edit()

                // Remove token
                editor.putString("token", "")
                editor.apply()

                // Logout
                loginRepo.logout()
            }
        }

        binding.saveTransactionList.setOnClickListener {
            showFileFormatDialog()
        }

        binding.sendTransactionList.setOnClickListener {
            val email = sharedPreferences.getString("username", "")
            sendEmailWithWorkbookAttachment(email)
        }

        binding.randomize.setOnClickListener {
            val intent = Intent(RANDOMIZE_TRANSACTIONS_ACTION)
            val randomValue = Random.nextInt(1000, 100001)
            intent.putExtra("amount", randomValue)
            requireContext().sendBroadcast(intent)
        }

        return binding.root
    }

    private fun showFileFormatDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose a format")

        builder.setItems(fileFormatOptions) { dialog, which ->
            when (which) {
                0 -> saveTransactionsToExcel("XLS")
                1 -> saveTransactionsToExcel("XLSX")
            }
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun createExcelWorkbook(): XSSFWorkbook {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Transactions")

        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("Title")
        header.createCell(1).setCellValue("Amount (Rupiah)")
        header.createCell(2).setCellValue("Category")
        header.createCell(3).setCellValue("Location")
        header.createCell(4).setCellValue("Created At")

        val transactions = transactionDAO.getAllTransactionsDirect()

        transactions.forEachIndexed { index, transaction ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(transaction.title)
            row.createCell(1).setCellValue(transaction.amount.toDouble())
            row.createCell(2).setCellValue(transaction.category)
            row.createCell(3).setCellValue(transaction.location.address)
            row.createCell(4).setCellValue(transaction.createdAt.toString())
        }

        return workbook
    }

    private fun saveExcelWorkbookToFile(): Uri? {
        val workbook = createExcelWorkbook()

        try {
            val fileName = "Transactions.xlsx"
            val file = File(requireContext().cacheDir, fileName)
            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }
            workbook.close()

            return FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun saveTransactionsToExcel(format: String = "XLSX") {
        lifecycleScope.launch(Dispatchers.IO) {
            // Create the excel file
            val workbook = createExcelWorkbook()

            val contentValues = ContentValues().apply {
                if (format == "XLS") {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "transactions ${SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(System.currentTimeMillis())}.xls")
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.ms-excel")
                } else {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "transactions ${SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(System.currentTimeMillis())}.xlsx")
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                }
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            // Get a URI for the file to be written
            val resolver = requireContext().contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            try {
                uri?.let { obtainedUri ->
                    resolver.openOutputStream(obtainedUri).use { outputStream ->
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

    private fun sendEmailWithWorkbookAttachment(userEmail: String?) {
        val fileUri = saveExcelWorkbookToFile()

        if (fileUri != null) {
            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "vnd.android.cursor.dir/email"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(userEmail ?: ""))
                putExtra(Intent.EXTRA_SUBJECT, "Transaction List ${SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(System.currentTimeMillis())}")
                putExtra(Intent.EXTRA_TEXT, "Here is your Bondoman transaction list.")
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            if (emailIntent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(Intent.createChooser(emailIntent, "Select an email app"))
            } else {
                Toast.makeText(requireContext(), "No email app found.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Error preparing the workbook for sending.", Toast.LENGTH_SHORT).show()
        }
    }
}

class SettingsPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
