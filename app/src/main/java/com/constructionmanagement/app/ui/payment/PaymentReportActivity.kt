package com.constructionmanagement.app.ui.payment

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.constructionmanagement.app.ConstructionApp
import com.constructionmanagement.app.R
import com.constructionmanagement.app.data.model.Payment
import com.constructionmanagement.app.databinding.ActivityPaymentReportBinding
import com.constructionmanagement.app.util.CurrencyFormatter
import com.google.android.material.appbar.AppBarLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PaymentReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentReportBinding
    private lateinit var viewModel: PaymentViewModel
    private lateinit var viewModelFactory: PaymentViewModelFactory
    private lateinit var reportAdapter: PaymentReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Hide default title
        binding.toolbar.title = getString(R.string.payment_report) // Set title on toolbar directly

        // Initialize ViewModel
        val application = application as ConstructionApp
        viewModelFactory = PaymentViewModelFactory(
            application.paymentRepository,
            application.workerRepository,
            application.siteRepository
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[PaymentViewModel::class.java]

        // Setup worker dropdown
        val workerAutoComplete = binding.spinnerWorker
        viewModel.allWorkers.observe(this) { workers ->
            val workerItems = listOf("All Workers") + workers.map { "${it.id}: ${it.name}" }
            val workerAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                workerItems
            )
            workerAutoComplete.setAdapter(workerAdapter)
            workerAutoComplete.setText("All Workers", false)
        }

        // Setup site dropdown
        val siteAutoComplete = binding.spinnerSite
        viewModel.allSites.observe(this) { sites ->
            val siteItems = listOf("All Sites") + sites.map { "${it.siteId}: ${it.name}" }
            val siteAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                siteItems
            )
            siteAutoComplete.setAdapter(siteAdapter)
            siteAutoComplete.setText("All Sites", false)
        }

        // Setup date pickers with default values (current month)
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Set end date to today
        val endDate = dateFormat.format(calendar.time)
        binding.editEndDate.setText(endDate)

        // Set start date to first day of current month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startDate = dateFormat.format(calendar.time)
        binding.editStartDate.setText(startDate)

        // Setup date picker dialogs
        binding.editStartDate.setOnClickListener {
            showDatePickerDialog(binding.editStartDate)
        }

        binding.editEndDate.setOnClickListener {
            showDatePickerDialog(binding.editEndDate)
        }

        // Setup recycler view
        reportAdapter = PaymentReportAdapter()
        binding.recyclerReport.adapter = reportAdapter

        // Provide worker data to the adapter
        viewModel.allWorkers.observe(this) { workers ->
            reportAdapter.setWorkerData(workers)
        }

        // Provide site data to the adapter
        viewModel.allSites.observe(this) { sites ->
            val siteMap = sites.associate { it.siteId to it.name }
            reportAdapter.setSiteData(siteMap)
        }

        // Hide results initially
        binding.cardSummary.visibility = View.GONE
        binding.labelResults.visibility = View.GONE
        binding.cardResults.visibility = View.GONE
        binding.textNoData.visibility = View.GONE

        // Set up generate report button
        binding.buttonGenerateReport.setOnClickListener {
            generateReport()
        }
    }

    private fun showDatePickerDialog(editText: com.google.android.material.textfield.TextInputEditText) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Parse current date from editText if available
        try {
            val date = dateFormat.parse(editText.text.toString())
            if (date != null) {
                calendar.time = date
            }
        } catch (e: Exception) {
            // Use current date if parsing fails
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                val selectedDate = dateFormat.format(calendar.time)
                editText.setText(selectedDate)
            },
            year, month, day
        )

        datePickerDialog.show()
    }

    private fun generateReport() {
        try {
            val startDate = binding.editStartDate.text.toString().trim()
            val endDate = binding.editEndDate.text.toString().trim()

            if (startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(this, "Please select date range", Toast.LENGTH_SHORT).show()
                return
            }

            // Get selected worker and site
            val workerSelection = binding.spinnerWorker.text.toString()
            val siteSelection = binding.spinnerSite.text.toString()

            if (workerSelection.isEmpty() || siteSelection.isEmpty()) {
                Toast.makeText(this, "Please select worker and site", Toast.LENGTH_SHORT).show()
                return
            }

            // Show loading indicator
            Toast.makeText(this, "Generating report...", Toast.LENGTH_SHORT).show()

            // For debugging
            Log.d("PaymentReport", "Querying date range: $startDate to $endDate")
            Log.d("PaymentReport", "Worker selection: $workerSelection")
            Log.d("PaymentReport", "Site selection: $siteSelection")

            // Create a new list to hold the filtered payments
            val filteredPayments = mutableListOf<Payment>()

            // Get all payments
            viewModel.allPayments.observe(this) { allPayments ->
                try {
                    Log.d("PaymentReport", "Total payments in database: ${allPayments.size}")

                    // Debug: Log all payment dates
                    allPayments.forEach { payment ->
                        Log.d("PaymentReport", "Payment ID: ${payment.paymentId}, Date: ${payment.paymentDate}, Worker: ${payment.workerId}, Site: ${payment.siteId}")
                    }

                    // Clear the filtered list
                    filteredPayments.clear()

                    // Add all payments that match the criteria
                    filteredPayments.addAll(allPayments)

                    // Update the UI with all payments first (for debugging)
                    Log.d("PaymentReport", "Initial payments count: ${filteredPayments.size}")

                    // Create a copy of the list for filtering
                    val tempList = ArrayList<Payment>(filteredPayments)

                    // Filter by date range if needed
                    if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
                        try {
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val startDateObj = dateFormat.parse(startDate)
                            val endDateObj = dateFormat.parse(endDate)

                            if (startDateObj != null && endDateObj != null) {
                                // Add one day to end date to make it inclusive
                                val calendar = Calendar.getInstance()
                                calendar.time = endDateObj
                                calendar.add(Calendar.DAY_OF_MONTH, 1)
                                val inclusiveEndDate = calendar.time

                                Log.d("PaymentReport", "Filtering by date range: ${dateFormat.format(startDateObj)} to ${dateFormat.format(inclusiveEndDate)}")

                                // Remove payments outside the date range
                                tempList.removeAll { payment: Payment ->
                                    try {
                                        val paymentDate = dateFormat.parse(payment.paymentDate)
                                        val isOutsideRange = paymentDate == null || paymentDate.before(startDateObj) || !paymentDate.before(inclusiveEndDate)
                                        if (isOutsideRange) {
                                            Log.d("PaymentReport", "Removing payment ID ${payment.paymentId} with date ${payment.paymentDate} - outside range")
                                        }
                                        isOutsideRange
                                    } catch (e: Exception) {
                                        Log.e("PaymentReport", "Error parsing payment date: ${payment.paymentDate}", e)
                                        true // Remove if there's an error
                                    }
                                }

                                Log.d("PaymentReport", "After date filter: ${tempList.size} payments")
                            } else {
                                Log.e("PaymentReport", "Failed to parse dates: startDate=$startDate, endDate=$endDate")
                                Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Log.e("PaymentReport", "Error filtering by date", e)
                            Toast.makeText(this, "Error filtering by date: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // Filter by worker if selected
                    if (workerSelection != "All Workers") {
                        try {
                            val workerId = workerSelection.substringBefore(":").trim().toLong()
                            Log.d("PaymentReport", "Filtering by worker ID: $workerId")

                            // Remove payments with different worker ID
                            tempList.removeAll { payment: Payment ->
                                val isWrongWorker = payment.workerId != workerId
                                if (isWrongWorker) {
                                    Log.d("PaymentReport", "Removing payment ID ${payment.paymentId} with worker ${payment.workerId} - not matching $workerId")
                                }
                                isWrongWorker
                            }

                            Log.d("PaymentReport", "After worker filter: ${tempList.size} payments")
                        } catch (e: Exception) {
                            Log.e("PaymentReport", "Error filtering by worker", e)
                            Toast.makeText(this, "Error filtering by worker: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // Filter by site if selected
                    if (siteSelection != "All Sites") {
                        try {
                            val siteId = siteSelection.substringBefore(":").trim().toLong()
                            Log.d("PaymentReport", "Filtering by site ID: $siteId")

                            // Remove payments with different site ID
                            tempList.removeAll { payment: Payment ->
                                val isWrongSite = payment.siteId != siteId
                                if (isWrongSite) {
                                    Log.d("PaymentReport", "Removing payment ID ${payment.paymentId} with site ${payment.siteId} - not matching $siteId")
                                }
                                isWrongSite
                            }

                            Log.d("PaymentReport", "After site filter: ${tempList.size} payments")
                        } catch (e: Exception) {
                            Log.e("PaymentReport", "Error filtering by site", e)
                            Toast.makeText(this, "Error filtering by site: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // Final list of filtered payments
                    Log.d("PaymentReport", "Final filtered payments: ${tempList.size}")

                    // Debug: Log all filtered payment details
                    for (payment in tempList) {
                        Log.d("PaymentReport", "Filtered Payment ID: ${payment.paymentId}, Date: ${payment.paymentDate}, Worker: ${payment.workerId}, Site: ${payment.siteId}")
                    }

                    // Update UI with the filtered list
                    reportAdapter.submitList(tempList)

                    // Show/hide no data message
                    binding.textNoData.visibility = if (tempList.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE

                    // Calculate total
                    val total = tempList.sumOf { it.amount }
                    binding.textTotal.text = CurrencyFormatter.formatRupees(total)

                    // Show summary
                    binding.textSummary.text = "Report for ${tempList.size} payments from $startDate to $endDate"

                    // Make summary card visible
                    binding.cardSummary.visibility = android.view.View.VISIBLE

                    // Show/hide results section
                    binding.labelResults.visibility = if (tempList.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
                    binding.cardResults.visibility = if (tempList.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE

                    // Scroll to top of results
                    binding.recyclerReport.scrollToPosition(0)
                } catch (e: Exception) {
                    Log.e("PaymentReport", "Error generating report", e)
                    Toast.makeText(this, "Error generating report: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("PaymentReport", "Error in generateReport", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish() // Use finish() instead of onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
