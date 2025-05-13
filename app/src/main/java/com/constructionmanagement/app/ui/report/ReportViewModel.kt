package com.constructionmanagement.app.ui.report

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.constructionmanagement.app.data.model.*
import com.constructionmanagement.app.data.repository.*
import com.constructionmanagement.app.util.CurrencyFormatter
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

enum class ReportType {
    WORKER_LIST,
    PAYMENT_HISTORY,
    ADVANCE_PAYMENT,
    SITE_SUMMARY,
    ATTENDANCE
}

enum class ExportFormat {
    PDF,
    EXCEL
}

data class ReportFilter(
    val reportType: ReportType = ReportType.WORKER_LIST,
    val startDate: String? = null,
    val endDate: String? = null,
    val workerId: Long? = null,
    val siteId: Long? = null,
    val exportFormat: ExportFormat = ExportFormat.PDF
)

class ReportViewModel(
    private val application: Application,
    private val workerRepository: WorkerRepository,
    private val siteRepository: SiteRepository,
    private val paymentRepository: PaymentRepository,
    private val advanceRepository: AdvanceRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    private val _reportGenerated = MutableLiveData<Boolean>()
    val reportGenerated: LiveData<Boolean> = _reportGenerated

    private val _reportUri = MutableLiveData<Uri?>()
    val reportUri: LiveData<Uri?> = _reportUri

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    val allWorkers = workerRepository.allWorkers
    val allSites = siteRepository.allSites
    val allPayments = paymentRepository.allPayments
    val allAdvances = advanceRepository.allAdvances
    val allAttendance = attendanceRepository.allAttendance

    fun generateReport(filter: ReportFilter) {
        _isLoading.value = true
        _errorMessage.value = null
        _reportUri.value = null
        _reportGenerated.value = false

        try {
            when (filter.exportFormat) {
                ExportFormat.PDF -> generatePdfReport(filter)
                ExportFormat.EXCEL -> generateExcelReport(filter)
            }
        } catch (e: Exception) {
            Log.e("ReportViewModel", "Error generating report", e)
            _errorMessage.value = "Error generating report: ${e.message}"
            _isLoading.value = false
        }
    }

    private fun generatePdfReport(filter: ReportFilter) {
        try {
            // Create a directory for reports if it doesn't exist
            val reportsDir = File(application.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Reports")
            if (!reportsDir.exists()) {
                reportsDir.mkdirs()
            }

            // Create a file for the report
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val reportName = "${filter.reportType.name.lowercase()}_$timestamp.pdf"
            val reportFile = File(reportsDir, reportName)

            // Create a PDF document
            val document = Document(PageSize.A4)
            PdfWriter.getInstance(document, FileOutputStream(reportFile))
            document.open()

            // Add title
            val titleFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD, BaseColor(0, 102, 204))
            val title = Paragraph(getReportTitle(filter), titleFont)
            title.alignment = Element.ALIGN_CENTER
            title.spacingAfter = 20f
            document.add(title)

            // Add date range if applicable
            if (filter.startDate != null && filter.endDate != null) {
                val dateRangeFont = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL)
                val dateRange = Paragraph("Date Range: ${filter.startDate} to ${filter.endDate}", dateRangeFont)
                dateRange.alignment = Element.ALIGN_CENTER
                dateRange.spacingAfter = 20f
                document.add(dateRange)
            }

            // Add report content based on type
            when (filter.reportType) {
                ReportType.WORKER_LIST -> addWorkerListToPdf(document)
                ReportType.PAYMENT_HISTORY -> addPaymentHistoryToPdf(document, filter)
                ReportType.ADVANCE_PAYMENT -> addAdvancePaymentToPdf(document, filter)
                ReportType.SITE_SUMMARY -> addSiteSummaryToPdf(document, filter)
                ReportType.ATTENDANCE -> addAttendanceToPdf(document, filter)
            }

            document.close()

            // Get URI for the file using FileProvider
            val uri = FileProvider.getUriForFile(
                application,
                "com.constructionmanagement.app.fileprovider",
                reportFile
            )

            _reportUri.value = uri
            _reportGenerated.value = true
            _isLoading.value = false
        } catch (e: Exception) {
            Log.e("ReportViewModel", "Error generating PDF report", e)
            _errorMessage.value = "Error generating PDF report: ${e.message}"
            _isLoading.value = false
        }
    }

    private fun generateExcelReport(filter: ReportFilter) {
        try {
            Log.d("ReportViewModel", "Starting Excel report generation for type: ${filter.reportType}")

            // Create a directory for reports if it doesn't exist
            val reportsDir = File(application.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Reports")
            if (!reportsDir.exists()) {
                reportsDir.mkdirs()
            }

            // Create a file for the report
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val reportName = "${filter.reportType.name.lowercase()}_$timestamp.xls"
            val reportFile = File(reportsDir, reportName)

            Log.d("ReportViewModel", "Excel file path: ${reportFile.absolutePath}")

            try {
                // Create a workbook
                val workbook = HSSFWorkbook()
                val sheet = workbook.createSheet(getReportTitle(filter))

                Log.d("ReportViewModel", "Created workbook and sheet")

                // Add report content based on type
                when (filter.reportType) {
                    ReportType.WORKER_LIST -> {
                        Log.d("ReportViewModel", "Adding worker list to Excel")
                        addWorkerListToExcel(sheet)
                    }
                    ReportType.PAYMENT_HISTORY -> {
                        Log.d("ReportViewModel", "Adding payment history to Excel")
                        addPaymentHistoryToExcel(sheet, filter)
                    }
                    ReportType.ADVANCE_PAYMENT -> {
                        Log.d("ReportViewModel", "Adding advance payment to Excel")
                        addAdvancePaymentToExcel(sheet, filter)
                    }
                    ReportType.SITE_SUMMARY -> {
                        Log.d("ReportViewModel", "Adding site summary to Excel")
                        addSiteSummaryToExcel(sheet, filter)
                    }
                    ReportType.ATTENDANCE -> {
                        Log.d("ReportViewModel", "Adding attendance to Excel")
                        addAttendanceToExcel(sheet, filter)
                    }
                }

                Log.d("ReportViewModel", "Writing workbook to file")

                // Write the workbook to the file
                val fileOutputStream = FileOutputStream(reportFile)
                workbook.write(fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
                workbook.close()

                Log.d("ReportViewModel", "Workbook written to file successfully")

                // Get URI for the file using FileProvider
                val uri = FileProvider.getUriForFile(
                    application,
                    "com.constructionmanagement.app.fileprovider",
                    reportFile
                )

                Log.d("ReportViewModel", "File URI created: $uri")

                _reportUri.value = uri
                _reportGenerated.value = true
                _isLoading.value = false

                Log.d("ReportViewModel", "Excel report generation completed successfully")
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Error in Excel workbook operations", e)
                throw e
            }
        } catch (e: Exception) {
            Log.e("ReportViewModel", "Error generating Excel report", e)
            _errorMessage.value = "Error generating Excel report: ${e.message}"
            _isLoading.value = false
        }
    }

    private fun getReportTitle(filter: ReportFilter): String {
        return when (filter.reportType) {
            ReportType.WORKER_LIST -> "Worker List Report"
            ReportType.PAYMENT_HISTORY -> "Payment History Report"
            ReportType.ADVANCE_PAYMENT -> "Advance Payment Report"
            ReportType.SITE_SUMMARY -> "Site Summary Report"
            ReportType.ATTENDANCE -> "Attendance Report"
        }
    }

    // Helper methods for PDF report generation
    private fun addWorkerListToPdf(document: Document) {
        try {
            // Create a table for worker data
            val table = PdfPTable(5)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(1f, 3f, 2f, 2f, 2f))

            // Add table headers
            val headerFont = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD, BaseColor(255, 255, 255))
            val headerCellColor = BaseColor(63, 81, 181) // Primary color

            addTableHeader(table, arrayOf("ID", "Name", "Phone", "Role", "Status"), headerFont, headerCellColor)

            // Add worker data
            val dataFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL, BaseColor(0, 0, 0))

            allWorkers.value?.forEach { worker ->
                table.addCell(createCell(worker.id.toString(), dataFont))
                table.addCell(createCell(worker.name, dataFont))
                table.addCell(createCell(worker.phoneNumber, dataFont))
                table.addCell(createCell(worker.role, dataFont))
                table.addCell(createCell(if (worker.isActive) "Active" else "Inactive", dataFont))
            }

            document.add(table)

            // Add summary
            val summaryFont = Font(Font.FontFamily.HELVETICA, 12f, Font.ITALIC, BaseColor(0, 102, 204))
            val summary = Paragraph("Total Workers: ${allWorkers.value?.size ?: 0}", summaryFont)
            summary.alignment = Element.ALIGN_RIGHT
            summary.spacingBefore = 20f
            document.add(summary)
        } catch (e: Exception) {
            Log.e("ReportViewModel", "Error adding worker list to PDF", e)
        }
    }

    private fun addPaymentHistoryToPdf(document: Document, filter: ReportFilter) {
        try {
            // Filter payments based on date range, worker, and site
            val filteredPayments = filterPayments(filter)

            // Create a table for payment data
            val table = PdfPTable(6)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(1f, 2f, 2f, 2f, 1.5f, 1.5f))

            // Add table headers
            val headerFont = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD, BaseColor(255, 255, 255))
            val headerCellColor = BaseColor(63, 81, 181) // Primary color

            addTableHeader(table, arrayOf("ID", "Worker", "Site", "Date", "Amount (₹)", "Mode"), headerFont, headerCellColor)

            // Add payment data
            val dataFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL, BaseColor(0, 0, 0))

            var totalAmount = 0.0

            filteredPayments.forEach { payment ->
                val workerName = allWorkers.value?.find { it.id == payment.workerId }?.name ?: "Unknown"
                val siteName = allSites.value?.find { it.siteId == payment.siteId }?.name ?: "Unknown"

                table.addCell(createCell(payment.paymentId.toString(), dataFont))
                table.addCell(createCell(workerName, dataFont))
                table.addCell(createCell(siteName, dataFont))
                table.addCell(createCell(payment.paymentDate, dataFont))
                table.addCell(createCell(String.format("%.2f", payment.amount), dataFont))
                table.addCell(createCell(payment.paymentMode.name, dataFont))

                totalAmount += payment.amount
            }

            document.add(table)

            // Add summary
            val summaryFont = Font(Font.FontFamily.HELVETICA, 12f, Font.ITALIC, BaseColor(0, 102, 204))
            val summary = Paragraph("Total Payments: ${filteredPayments.size} | Total Amount: ₹${String.format("%.2f", totalAmount)}", summaryFont)
            summary.alignment = Element.ALIGN_RIGHT
            summary.spacingBefore = 20f
            document.add(summary)
        } catch (e: Exception) {
            Log.e("ReportViewModel", "Error adding payment history to PDF", e)
        }
    }

    private fun addAdvancePaymentToPdf(document: Document, filter: ReportFilter) {
        try {
            // Filter advances based on date range and worker
            val filteredAdvances = filterAdvances(filter)

            // Create a table for advance data
            val table = PdfPTable(5)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(1f, 2f, 2f, 1.5f, 3.5f))

            // Add table headers
            val headerFont = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD, BaseColor(255, 255, 255))
            val headerCellColor = BaseColor(63, 81, 181) // Primary color

            addTableHeader(table, arrayOf("ID", "Worker", "Date", "Amount (₹)", "Reason"), headerFont, headerCellColor)

            // Add advance data
            val dataFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL, BaseColor(0, 0, 0))

            var totalAmount = 0.0

            filteredAdvances.forEach { advance ->
                val workerName = allWorkers.value?.find { it.id == advance.workerId }?.name ?: "Unknown"

                table.addCell(createCell(advance.advanceId.toString(), dataFont))
                table.addCell(createCell(workerName, dataFont))
                table.addCell(createCell(advance.advanceDate, dataFont))
                table.addCell(createCell(String.format("%.2f", advance.amount), dataFont))
                table.addCell(createCell(advance.reason, dataFont))

                totalAmount += advance.amount
            }

            document.add(table)

            // Add summary
            val summaryFont = Font(Font.FontFamily.HELVETICA, 12f, Font.ITALIC, BaseColor(0, 102, 204))
            val summary = Paragraph("Total Advances: ${filteredAdvances.size} | Total Amount: ₹${String.format("%.2f", totalAmount)}", summaryFont)
            summary.alignment = Element.ALIGN_RIGHT
            summary.spacingBefore = 20f
            document.add(summary)
        } catch (e: Exception) {
            Log.e("ReportViewModel", "Error adding advance payment to PDF", e)
        }
    }

    private fun addSiteSummaryToPdf(document: Document, filter: ReportFilter) {
        try {
            // Filter sites based on site ID
            val filteredSites = if (filter.siteId != null) {
                allSites.value?.filter { it.siteId == filter.siteId } ?: emptyList()
            } else {
                allSites.value ?: emptyList()
            }

            // Create a table for site data
            val table = PdfPTable(5)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(1f, 3f, 2f, 2f, 2f))

            // Add table headers
            val headerFont = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD, BaseColor(255, 255, 255))
            val headerCellColor = BaseColor(63, 81, 181) // Primary color

            addTableHeader(table, arrayOf("ID", "Name", "Client", "Start Date", "Status"), headerFont, headerCellColor)

            // Add site data
            val dataFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL, BaseColor(0, 0, 0))

            filteredSites.forEach { site ->
                table.addCell(createCell(site.siteId.toString(), dataFont))
                table.addCell(createCell(site.name, dataFont))
                table.addCell(createCell(site.clientName, dataFont))
                table.addCell(createCell(site.startDate, dataFont))
                table.addCell(createCell(site.status.name, dataFont))
            }

            document.add(table)

            // Add worker assignment summary for each site
            val assignmentFont = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD, BaseColor(0, 102, 204))
            val workerFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL, BaseColor(0, 0, 0))

            filteredSites.forEach { site ->
                document.add(Paragraph("\n"))
                document.add(Paragraph("Workers assigned to ${site.name}:", assignmentFont))

                // Get workers assigned to this site
                val assignedWorkers = allWorkers.value?.filter { worker ->
                    // In a real app, you would check the worker-site assignments
                    // This is a placeholder implementation
                    true
                } ?: emptyList()

                if (assignedWorkers.isNotEmpty()) {
                    val workerTable = PdfPTable(3)
                    workerTable.widthPercentage = 100f
                    workerTable.setWidths(floatArrayOf(1f, 3f, 2f))

                    addTableHeader(workerTable, arrayOf("ID", "Name", "Role"), headerFont, headerCellColor)

                    assignedWorkers.forEach { worker ->
                        workerTable.addCell(createCell(worker.id.toString(), workerFont))
                        workerTable.addCell(createCell(worker.name, workerFont))
                        workerTable.addCell(createCell(worker.role, workerFont))
                    }

                    document.add(workerTable)
                } else {
                    document.add(Paragraph("No workers assigned to this site.", workerFont))
                }
            }

            // Add summary
            val summaryFont = Font(Font.FontFamily.HELVETICA, 12f, Font.ITALIC, BaseColor(0, 102, 204))
            val summary = Paragraph("Total Sites: ${filteredSites.size}", summaryFont)
            summary.alignment = Element.ALIGN_RIGHT
            summary.spacingBefore = 20f
            document.add(summary)
        } catch (e: Exception) {
            Log.e("ReportViewModel", "Error adding site summary to PDF", e)
        }
    }

    private fun addAttendanceToPdf(document: Document, filter: ReportFilter) {
        try {
            // Filter attendance based on date range, worker, and site
            val filteredAttendance = filterAttendance(filter)

            // Create a table for attendance data
            val table = PdfPTable(5)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(1f, 2f, 2f, 2f, 3f))

            // Add table headers
            val headerFont = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD, BaseColor(255, 255, 255))
            val headerCellColor = BaseColor(63, 81, 181) // Primary color

            addTableHeader(table, arrayOf("ID", "Worker", "Site", "Date", "Status"), headerFont, headerCellColor)

            // Add attendance data
            val dataFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL, BaseColor(0, 0, 0))

            filteredAttendance.forEach { attendance ->
                val workerName = allWorkers.value?.find { it.id == attendance.workerId }?.name ?: "Unknown"
                val siteName = allSites.value?.find { it.siteId == attendance.siteId }?.name ?: "Unknown"

                table.addCell(createCell(attendance.attendanceId.toString(), dataFont))
                table.addCell(createCell(workerName, dataFont))
                table.addCell(createCell(siteName, dataFont))
                table.addCell(createCell(attendance.date, dataFont))
                table.addCell(createCell(attendance.status.name, dataFont))
            }

            document.add(table)

            // Add summary
            val summaryFont = Font(Font.FontFamily.HELVETICA, 12f, Font.ITALIC, BaseColor(0, 102, 204))
            val summary = Paragraph("Total Attendance Records: ${filteredAttendance.size}", summaryFont)
            summary.alignment = Element.ALIGN_RIGHT
            summary.spacingBefore = 20f
            document.add(summary)
        } catch (e: Exception) {
            Log.e("ReportViewModel", "Error adding attendance to PDF", e)
        }
    }

    // Helper methods for Excel report generation
    private fun addWorkerListToExcel(sheet: Sheet) {
        try {
            Log.d("ReportViewModel", "Creating worker list Excel content")

            // Create header row
            val headerRow = sheet.createRow(0)
            val headers = arrayOf("ID", "Name", "Phone", "Role", "Status")

            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
            }

            // Add worker data
            val workers = allWorkers.value ?: emptyList()
            Log.d("ReportViewModel", "Adding ${workers.size} workers to Excel")

            workers.forEachIndexed { index, worker ->
                try {
                    val row = sheet.createRow(index + 1)

                    row.createCell(0).setCellValue(worker.id.toString())
                    row.createCell(1).setCellValue(worker.name)
                    row.createCell(2).setCellValue(worker.phoneNumber)
                    row.createCell(3).setCellValue(worker.role)
                    row.createCell(4).setCellValue(if (worker.isActive) "Active" else "Inactive")
                } catch (e: Exception) {
                    Log.e("ReportViewModel", "Error adding worker row to Excel: ${worker.id}", e)
                }
            }

            Log.d("ReportViewModel", "Worker list Excel content created successfully")
        } catch (e: Exception) {
            Log.e("ReportViewModel", "Error adding worker list to Excel", e)
        }
    }

    private fun addPaymentHistoryToExcel(sheet: Sheet, filter: ReportFilter) {
        try {
            Log.d("ReportViewModel", "Creating payment history Excel content")

            // Filter payments based on date range, worker, and site
            val filteredPayments = filterPayments(filter)
            Log.d("ReportViewModel", "Filtered ${filteredPayments.size} payments")

            // Create header row
            val headerRow = sheet.createRow(0)
            val headers = arrayOf("ID", "Worker", "Site", "Date", "Amount (₹)", "Mode")

            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
            }

            // Add payment data
            filteredPayments.forEachIndexed { index, payment ->
                try {
                    val row = sheet.createRow(index + 1)
                    val workerName = allWorkers.value?.find { it.id == payment.workerId }?.name ?: "Unknown"
                    val siteName = allSites.value?.find { it.siteId == payment.siteId }?.name ?: "Unknown"

                    row.createCell(0).setCellValue(payment.paymentId.toString())
                    row.createCell(1).setCellValue(workerName)
                    row.createCell(2).setCellValue(siteName)
                    row.createCell(3).setCellValue(payment.paymentDate)
                    row.createCell(4).setCellValue(payment.amount.toString())
                    row.createCell(5).setCellValue(payment.paymentMode.name)
                } catch (e: Exception) {
                    Log.e("ReportViewModel", "Error adding payment row to Excel: ${payment.paymentId}", e)
                }
            }

            // Add summary row
            try {
                val summaryRow = sheet.createRow(filteredPayments.size + 2)
                summaryRow.createCell(0).setCellValue("Total")
                summaryRow.createCell(4).setCellValue(filteredPayments.sumOf { it.amount }.toString())
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Error adding payment summary row to Excel", e)
            }

            Log.d("ReportViewModel", "Payment history Excel content created successfully")
        } catch (e: Exception) {
            Log.e("ReportViewModel", "Error adding payment history to Excel", e)
        }
    }

    private fun addAdvancePaymentToExcel(sheet: Sheet, filter: ReportFilter) {
        try {
            Log.d("ReportViewModel", "Creating advance payment Excel content")

            // Filter advances based on date range and worker
            val filteredAdvances = filterAdvances(filter)
            Log.d("ReportViewModel", "Filtered ${filteredAdvances.size} advances")

            // Create header row
            val headerRow = sheet.createRow(0)
            val headers = arrayOf("ID", "Worker", "Date", "Amount (₹)", "Reason")

            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
            }

            // Add advance data
            filteredAdvances.forEachIndexed { index, advance ->
                try {
                    val row = sheet.createRow(index + 1)
                    val workerName = allWorkers.value?.find { it.id == advance.workerId }?.name ?: "Unknown"

                    row.createCell(0).setCellValue(advance.advanceId.toString())
                    row.createCell(1).setCellValue(workerName)
                    row.createCell(2).setCellValue(advance.advanceDate)
                    row.createCell(3).setCellValue(advance.amount.toString())
                    row.createCell(4).setCellValue(advance.reason ?: "")
                } catch (e: Exception) {
                    Log.e("ReportViewModel", "Error adding advance row to Excel: ${advance.advanceId}", e)
                }
            }

            // Add summary row
            try {
                val summaryRow = sheet.createRow(filteredAdvances.size + 2)
                summaryRow.createCell(0).setCellValue("Total")
                summaryRow.createCell(3).setCellValue(filteredAdvances.sumOf { it.amount }.toString())
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Error adding advance summary row to Excel", e)
            }

            Log.d("ReportViewModel", "Advance payment Excel content created successfully")
        } catch (e: Exception) {
            Log.e("ReportViewModel", "Error adding advance payment to Excel", e)
        }
    }

    private fun addSiteSummaryToExcel(sheet: Sheet, filter: ReportFilter) {
        try {
            Log.d("ReportViewModel", "Creating site summary Excel content")

            // Filter sites based on site ID
            val filteredSites = if (filter.siteId != null) {
                allSites.value?.filter { it.siteId == filter.siteId } ?: emptyList()
            } else {
                allSites.value ?: emptyList()
            }
            Log.d("ReportViewModel", "Filtered ${filteredSites.size} sites")

            // Create header row
            val headerRow = sheet.createRow(0)
            val headers = arrayOf("ID", "Name", "Client", "Start Date", "Status")

            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
            }

            // Add site data
            filteredSites.forEachIndexed { index, site ->
                try {
                    val row = sheet.createRow(index + 1)

                    row.createCell(0).setCellValue(site.siteId.toString())
                    row.createCell(1).setCellValue(site.name)
                    row.createCell(2).setCellValue(site.clientName)
                    row.createCell(3).setCellValue(site.startDate)
                    row.createCell(4).setCellValue(site.status.name)
                } catch (e: Exception) {
                    Log.e("ReportViewModel", "Error adding site row to Excel: ${site.siteId}", e)
                }
            }

            Log.d("ReportViewModel", "Site summary Excel content created successfully")
        } catch (e: Exception) {
            Log.e("ReportViewModel", "Error adding site summary to Excel", e)
        }
    }

    private fun addAttendanceToExcel(sheet: Sheet, filter: ReportFilter) {
        try {
            Log.d("ReportViewModel", "Creating attendance Excel content")

            // Filter attendance based on date range, worker, and site
            val filteredAttendance = filterAttendance(filter)
            Log.d("ReportViewModel", "Filtered ${filteredAttendance.size} attendance records")

            // Create header row
            val headerRow = sheet.createRow(0)
            val headers = arrayOf("ID", "Worker", "Site", "Date", "Status")

            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
            }

            // Add attendance data
            filteredAttendance.forEachIndexed { index, attendance ->
                try {
                    val row = sheet.createRow(index + 1)
                    val workerName = allWorkers.value?.find { it.id == attendance.workerId }?.name ?: "Unknown"
                    val siteName = allSites.value?.find { it.siteId == attendance.siteId }?.name ?: "Unknown"

                    row.createCell(0).setCellValue(attendance.attendanceId.toString())
                    row.createCell(1).setCellValue(workerName)
                    row.createCell(2).setCellValue(siteName)
                    row.createCell(3).setCellValue(attendance.date)
                    row.createCell(4).setCellValue(attendance.status.name)
                } catch (e: Exception) {
                    Log.e("ReportViewModel", "Error adding attendance row to Excel: ${attendance.attendanceId}", e)
                }
            }

            Log.d("ReportViewModel", "Attendance Excel content created successfully")
        } catch (e: Exception) {
            Log.e("ReportViewModel", "Error adding attendance to Excel", e)
        }
    }

    // Helper methods for filtering data
    private fun filterPayments(filter: ReportFilter): List<Payment> {
        return allPayments.value?.filter { payment ->
            var matches = true

            // Filter by date range
            if (filter.startDate != null && filter.endDate != null) {
                val paymentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(payment.paymentDate)
                val startDate = if (filter.startDate != null) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(filter.startDate) else null
                val endDate = if (filter.endDate != null) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(filter.endDate) else null

                if (paymentDate == null || startDate == null || endDate == null || paymentDate < startDate || paymentDate > endDate) {
                    matches = false
                }
            }

            // Filter by worker
            if (filter.workerId != null && payment.workerId != filter.workerId) {
                matches = false
            }

            // Filter by site
            if (filter.siteId != null && payment.siteId != filter.siteId) {
                matches = false
            }

            matches
        } ?: emptyList()
    }

    private fun filterAdvances(filter: ReportFilter): List<Advance> {
        return allAdvances.value?.filter { advance ->
            var matches = true

            // Filter by date range
            if (filter.startDate != null && filter.endDate != null) {
                val advanceDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(advance.advanceDate)
                val startDate = if (filter.startDate != null) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(filter.startDate) else null
                val endDate = if (filter.endDate != null) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(filter.endDate) else null

                if (advanceDate == null || startDate == null || endDate == null || advanceDate < startDate || advanceDate > endDate) {
                    matches = false
                }
            }

            // Filter by worker
            if (filter.workerId != null && advance.workerId != filter.workerId) {
                matches = false
            }

            matches
        } ?: emptyList()
    }

    private fun filterAttendance(filter: ReportFilter): List<Attendance> {
        return allAttendance.value?.filter { attendance ->
            var matches = true

            // Filter by date range
            if (filter.startDate != null && filter.endDate != null) {
                val attendanceDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(attendance.date)
                val startDate = if (filter.startDate != null) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(filter.startDate) else null
                val endDate = if (filter.endDate != null) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(filter.endDate) else null

                if (attendanceDate == null || startDate == null || endDate == null || attendanceDate < startDate || attendanceDate > endDate) {
                    matches = false
                }
            }

            // Filter by worker
            if (filter.workerId != null && attendance.workerId != filter.workerId) {
                matches = false
            }

            // Filter by site
            if (filter.siteId != null && attendance.siteId != filter.siteId) {
                matches = false
            }

            matches
        } ?: emptyList()
    }

    // Helper methods for creating PDF table cells
    private fun createCell(text: String, font: Font): PdfPCell {
        val cell = PdfPCell(Phrase(text, font))
        cell.paddingBottom = 5f
        cell.paddingTop = 5f
        return cell
    }

    private fun addTableHeader(table: PdfPTable, headers: Array<String>, font: Font, backgroundColor: BaseColor) {
        headers.forEach { header ->
            val cell = PdfPCell(Phrase(header, font))
            cell.backgroundColor = backgroundColor
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.verticalAlignment = Element.ALIGN_MIDDLE
            cell.paddingBottom = 8f
            cell.paddingTop = 8f
            table.addCell(cell)
        }
    }
}

class ReportViewModelFactory(
    private val application: Application,
    private val workerRepository: WorkerRepository,
    private val siteRepository: SiteRepository,
    private val paymentRepository: PaymentRepository,
    private val advanceRepository: AdvanceRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportViewModel(
                application,
                workerRepository,
                siteRepository,
                paymentRepository,
                advanceRepository,
                attendanceRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
