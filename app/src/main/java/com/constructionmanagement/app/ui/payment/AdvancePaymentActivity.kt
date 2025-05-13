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
import com.constructionmanagement.app.data.model.Advance
import com.constructionmanagement.app.data.model.PaymentMode
import com.constructionmanagement.app.databinding.ActivityAdvancePaymentBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdvancePaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdvancePaymentBinding
    private lateinit var viewModel: AdvanceViewModel
    private lateinit var viewModelFactory: AdvanceViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdvancePaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.advance_payment)

        // Initialize ViewModel
        val application = application as ConstructionApp
        viewModelFactory = AdvanceViewModelFactory(
            application.advanceRepository,
            application.workerRepository
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[AdvanceViewModel::class.java]

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
                saveAdvance()
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

        // Validate reason
        if (binding.editReason.text.toString().trim().isEmpty()) {
            binding.editReason.error = "Reason is required"
            isValid = false
        }

        return isValid
    }

    private fun saveAdvance() {
        // Get worker ID from dropdown
        val workerSelection = (binding.spinnerWorker as AutoCompleteTextView).text.toString()
        val workerId = workerSelection.substringBefore(":").trim().toLong()

        // Get amount
        val amount = binding.editAmount.text.toString().trim().toDouble()

        // Get reason
        val reason = binding.editReason.text.toString().trim()

        // Get payment mode
        val paymentModeString = (binding.spinnerPaymentMode as AutoCompleteTextView).text.toString()
        val paymentMode = PaymentMode.valueOf(paymentModeString)

        // Get current date as advance date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val advanceDate = dateFormat.format(Date())

        // Get reference number if provided
        val referenceNumber = binding.editReferenceNumber.text.toString().trim().ifEmpty { null }

        // Create advance object
        val advance = Advance(
            advanceId = 0, // Room will auto-generate the ID
            workerId = workerId,
            amount = amount,
            advanceDate = advanceDate,
            reason = reason,
            paymentMode = paymentMode,
            referenceNumber = referenceNumber,
            isRecovered = false
        )

        // Save advance to database
        viewModel.insertAdvance(advance)
        Toast.makeText(this, "Advance payment recorded successfully", Toast.LENGTH_SHORT).show()
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
