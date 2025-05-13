package com.constructionmanagement.app.ui.payment

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.constructionmanagement.app.ConstructionApp
import com.constructionmanagement.app.R
import com.constructionmanagement.app.data.model.Payment
import com.constructionmanagement.app.data.model.PaymentMode
import com.constructionmanagement.app.databinding.ActivityProcessPaymentBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProcessPaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProcessPaymentBinding
    private lateinit var viewModel: PaymentViewModel
    private lateinit var viewModelFactory: PaymentViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProcessPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.process_payment)

        // Initialize ViewModel
        val application = application as ConstructionApp
        viewModelFactory = PaymentViewModelFactory(
            application.paymentRepository,
            application.workerRepository,
            application.siteRepository
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[PaymentViewModel::class.java]

        // Setup worker dropdown
        val workerAutoComplete = binding.spinnerWorker as AutoCompleteTextView
        viewModel.allWorkers.observe(this) { workers ->
            val workerAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                workers.map { "${it.id}: ${it.name}" }
            )
            workerAutoComplete.setAdapter(workerAdapter)
            if (workers.isNotEmpty()) {
                workerAutoComplete.setText(workerAdapter.getItem(0).toString(), false)
            }
        }

        // Setup site dropdown
        val siteAutoComplete = binding.spinnerSite as AutoCompleteTextView
        viewModel.allSites.observe(this) { sites ->
            val siteAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                sites.map { "${it.siteId}: ${it.name}" }
            )
            siteAutoComplete.setAdapter(siteAdapter)
            if (sites.isNotEmpty()) {
                siteAutoComplete.setText(siteAdapter.getItem(0).toString(), false)
            }
        }

        // Setup payment mode dropdown
        val paymentModeAutoComplete = binding.spinnerPaymentMode as AutoCompleteTextView
        val paymentModeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            PaymentMode.values().map { it.name }
        )
        paymentModeAutoComplete.setAdapter(paymentModeAdapter)
        paymentModeAutoComplete.setText(paymentModeAdapter.getItem(0).toString(), false)

        // Set up save button
        binding.buttonSave.setOnClickListener {
            if (validateInputs()) {
                savePayment()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validate amount
        if (binding.editAmount.text.toString().trim().isEmpty()) {
            binding.editAmount.error = "Amount is required"
            isValid = false
        }

        // Validate description
        if (binding.editDescription.text.toString().trim().isEmpty()) {
            binding.editDescription.error = "Description is required"
            isValid = false
        }

        return isValid
    }

    private fun savePayment() {
        // Get worker ID from dropdown
        val workerSelection = (binding.spinnerWorker as AutoCompleteTextView).text.toString()
        val workerId = workerSelection.substringBefore(":").trim().toLong()

        // Get site ID from dropdown
        val siteSelection = (binding.spinnerSite as AutoCompleteTextView).text.toString()
        val siteId = siteSelection.substringBefore(":").trim().toLong()

        // Get amount
        val amount = binding.editAmount.text.toString().trim().toDouble()

        // Get description
        val description = binding.editDescription.text.toString().trim()

        // Get payment mode
        val paymentModeString = (binding.spinnerPaymentMode as AutoCompleteTextView).text.toString()
        val paymentMode = PaymentMode.valueOf(paymentModeString)

        // Get current date as payment date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val paymentDate = dateFormat.format(Date())

        // Get reference number if provided
        val referenceNumber = binding.editReferenceNumber.text.toString().trim().ifEmpty { null }

        // Create payment object
        val payment = Payment(
            paymentId = 0, // Room will auto-generate the ID
            workerId = workerId,
            siteId = siteId,
            amount = amount,
            paymentDate = paymentDate,
            description = description,
            paymentMode = paymentMode,
            referenceNumber = referenceNumber
        )

        // Save payment to database
        viewModel.insertPayment(payment)
        Toast.makeText(this, "Payment processed successfully", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
