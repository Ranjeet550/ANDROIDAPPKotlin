package com.constructionmanagement.app.ui.payment

import android.os.Bundle
import android.view.MenuItem
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.constructionmanagement.app.ConstructionApp
import com.constructionmanagement.app.R
import com.constructionmanagement.app.data.model.Advance
import com.constructionmanagement.app.databinding.ActivityAdvanceDetailsBinding
import com.constructionmanagement.app.util.CurrencyFormatter
import java.io.File

class AdvanceDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ADVANCE_ID = "extra_advance_id"
    }

    private lateinit var binding: ActivityAdvanceDetailsBinding
    private lateinit var viewModel: AdvanceViewModel
    private lateinit var viewModelFactory: AdvanceViewModelFactory
    private var advanceId: Long = 0
    private var advance: Advance? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdvanceDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.advance_details)

        // Get advance ID from intent
        advanceId = intent.getLongExtra(EXTRA_ADVANCE_ID, -1L)
        if (advanceId == -1L) {
            Toast.makeText(this, "Invalid advance ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize ViewModel
        val application = application as ConstructionApp
        viewModelFactory = AdvanceViewModelFactory(
            application.advanceRepository,
            application.workerRepository
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[AdvanceViewModel::class.java]

        // Load advance details
        viewModel.getAdvanceById(advanceId).observe(this) { advanceData ->
            if (advanceData != null) {
                advance = advanceData
                populateAdvanceData(advanceData)
            } else {
                Toast.makeText(this, "Advance not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // Set up save button
        binding.buttonSave.setOnClickListener {
            if (validateInputs()) {
                updateAdvance()
            }
        }

        // Set up cancel button
        binding.buttonCancel.setOnClickListener {
            finish()
        }

        // Setup status chip click listener
        setupStatusChipClickListener()
    }

    private fun populateAdvanceData(advance: Advance) {
        // Get worker name from the database
        viewModel.getWorkerById(advance.workerId).observe(this) { worker ->
            if (worker != null) {
                binding.textWorkerName.text = worker.name

                // Load worker image if available
                if (worker.profileImagePath != null) {
                    val imageFile = File(worker.profileImagePath)
                    if (imageFile.exists()) {
                        Glide.with(this)
                            .load(imageFile as File)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .circleCrop()
                            .into(binding.imageWorker)
                    } else {
                        binding.imageWorker.setImageResource(R.drawable.ic_person)
                    }
                } else {
                    binding.imageWorker.setImageResource(R.drawable.ic_person)
                }
            } else {
                binding.textWorkerName.text = "Unknown Worker"
                binding.imageWorker.setImageResource(R.drawable.ic_person)
            }
        }

        // Set amount
        binding.editAmount.setText(advance.amount.toString())

        // Set date
        binding.textDate.text = advance.advanceDate

        // Set reason
        binding.editReason.setText(advance.reason)

        // Set payment mode
        binding.textPaymentMode.text = advance.paymentMode.name

        // Set reference number
        binding.editReference.setText(advance.referenceNumber ?: "")

        // Set status
        updateStatusChip(advance.isRecovered)
    }

    private fun updateStatusChip(isRecovered: Boolean) {
        if (isRecovered) {
            binding.chipStatus.text = "Completed"
            binding.chipStatus.setChipBackgroundColorResource(R.color.success)
            binding.radioRecovered.isChecked = true
        } else {
            binding.chipStatus.text = "Pending"
            binding.chipStatus.setChipBackgroundColorResource(R.color.warning)
            binding.radioPending.isChecked = true
        }

        // Force layout update to ensure chip is properly displayed
        binding.chipStatus.invalidate()
        binding.statusContainer.requestLayout()
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validate amount
        if (binding.editAmount.text.toString().trim().isEmpty()) {
            binding.layoutAmount.error = "Amount is required"
            isValid = false
        } else {
            binding.layoutAmount.error = null
        }

        // Validate reason
        if (binding.editReason.text.toString().trim().isEmpty()) {
            binding.layoutReason.error = "Reason is required"
            isValid = false
        } else {
            binding.layoutReason.error = null
        }

        return isValid
    }

    private fun updateAdvance() {
        advance?.let {
            // Get amount
            val amountStr = binding.editAmount.text.toString().trim()
            val amount = if (amountStr.isNotEmpty()) amountStr.toDouble() else 0.0

            // Get reason
            val reason = binding.editReason.text.toString().trim()

            // Get reference number
            val referenceNumber = binding.editReference.text.toString().trim().ifEmpty { null }

            // Get status (isRecovered means "Completed")
            val isRecovered = binding.radioRecovered.isChecked

            // Create updated advance object
            val updatedAdvance = it.copy(
                amount = amount,
                reason = reason,
                referenceNumber = referenceNumber,
                isRecovered = isRecovered
            )

            // Update advance in database
            viewModel.updateAdvance(updatedAdvance)

            // Show appropriate message based on status
            val statusMessage = if (isRecovered) "Advance marked as completed" else "Advance updated successfully"
            Toast.makeText(this, statusMessage, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // Add click listener to the status chip to toggle status
    private fun setupStatusChipClickListener() {
        binding.chipStatus.setOnClickListener {
            val newStatus = !binding.radioRecovered.isChecked
            binding.radioRecovered.isChecked = newStatus
            binding.radioPending.isChecked = !newStatus

            // Update the chip immediately
            if (newStatus) {
                binding.chipStatus.text = "Completed"
                binding.chipStatus.setChipBackgroundColorResource(R.color.success)
            } else {
                binding.chipStatus.text = "Pending"
                binding.chipStatus.setChipBackgroundColorResource(R.color.warning)
            }

            // Force layout update
            binding.chipStatus.invalidate()
            binding.statusContainer.requestLayout()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
