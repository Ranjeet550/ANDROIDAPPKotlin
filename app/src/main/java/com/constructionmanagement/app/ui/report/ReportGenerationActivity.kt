package com.constructionmanagement.app.ui.report

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.constructionmanagement.app.ConstructionApp
import com.constructionmanagement.app.R
import com.constructionmanagement.app.databinding.ActivityReportGenerationBinding
import java.io.File
import java.io.FileOutputStream

class ReportGenerationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportGenerationBinding
    private lateinit var viewModel: ReportViewModel
    private lateinit var viewModelFactory: ReportViewModelFactory
    private var reportUri: Uri? = null
    private var reportType: ReportType = ReportType.WORKER_LIST
    private var exportFormat: ExportFormat = ExportFormat.PDF

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportGenerationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbarTitle.text = getString(R.string.report_preview)

        // Get report parameters from intent
        reportType = intent.getSerializableExtra("reportType") as? ReportType ?: ReportType.WORKER_LIST
        val startDate = intent.getStringExtra("startDate")
        val endDate = intent.getStringExtra("endDate")
        val workerId = intent.getLongExtra("workerId", -1L).takeIf { it != -1L }
        val siteId = intent.getLongExtra("siteId", -1L).takeIf { it != -1L }
        exportFormat = intent.getSerializableExtra("exportFormat") as? ExportFormat ?: ExportFormat.PDF

        // Initialize ViewModel
        val application = application as ConstructionApp
        viewModelFactory = ReportViewModelFactory(
            application,
            application.workerRepository,
            application.siteRepository,
            application.paymentRepository,
            application.advanceRepository,
            application.attendanceRepository
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[ReportViewModel::class.java]

        // Set report title based on report type
        binding.textReportTitle.text = when (reportType) {
            ReportType.WORKER_LIST -> "Worker List Report"
            ReportType.PAYMENT_HISTORY -> "Payment History Report"
            ReportType.ADVANCE_PAYMENT -> "Advance Payment Report"
            ReportType.SITE_SUMMARY -> "Site Summary Report"
            ReportType.ATTENDANCE -> "Attendance Report"
        }

        // Set date range if available
        if (startDate != null && endDate != null) {
            binding.textDateRange.text = "Date Range: $startDate to $endDate"
            binding.textDateRange.visibility = View.VISIBLE
        } else {
            binding.textDateRange.visibility = View.GONE
        }

        // Setup observers
        setupObservers()

        // Generate report
        val filter = ReportFilter(
            reportType = reportType,
            startDate = startDate,
            endDate = endDate,
            workerId = workerId,
            siteId = siteId,
            exportFormat = exportFormat
        )
        viewModel.generateReport(filter)

        // Setup buttons
        setupButtons()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.buttonDownload.isEnabled = !isLoading
            binding.buttonShare.isEnabled = !isLoading
        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.reportUri.observe(this) { uri ->
            reportUri = uri
            if (uri != null) {
                // Show appropriate preview based on format
                if (exportFormat == ExportFormat.PDF) {
                    binding.pdfPreviewLayout.visibility = View.VISIBLE
                    binding.excelPreviewLayout.visibility = View.GONE

                    // Open PDF in external viewer when preview button is clicked
                    binding.buttonPreviewPdf.setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(uri, "application/pdf")
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        startActivity(Intent.createChooser(intent, "Open PDF with"))
                    }
                } else {
                    // Show Excel icon for Excel reports
                    binding.pdfPreviewLayout.visibility = View.GONE
                    binding.excelPreviewLayout.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupButtons() {
        // Download button
        binding.buttonDownload.setOnClickListener {
            reportUri?.let { uri ->
                try {
                    val extension = if (exportFormat == ExportFormat.PDF) "pdf" else "xls"
                    val fileName = "report_${System.currentTimeMillis()}.$extension"

                    // Copy the file to Downloads folder
                    val inputStream = contentResolver.openInputStream(uri)
                    val downloadsDir = getExternalFilesDir(null)
                    val outputFile = File(downloadsDir, fileName)

                    inputStream?.use { input ->
                        val outputStream = FileOutputStream(outputFile)
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }

                    // Show success message
                    Toast.makeText(this, "Report saved to Downloads folder", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error saving report: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(this, "No report available to download", Toast.LENGTH_SHORT).show()
            }
        }

        // Share button
        binding.buttonShare.setOnClickListener {
            reportUri?.let { uri ->
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = if (exportFormat == ExportFormat.PDF) "application/pdf" else "application/vnd.ms-excel"
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(Intent.createChooser(shareIntent, "Share Report"))
            } ?: run {
                Toast.makeText(this, "No report available to share", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
