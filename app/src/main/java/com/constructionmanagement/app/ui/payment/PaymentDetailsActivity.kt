package com.constructionmanagement.app.ui.payment

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.constructionmanagement.app.ConstructionApp
import com.constructionmanagement.app.R
import com.constructionmanagement.app.databinding.ActivityPaymentDetailsBinding
import com.constructionmanagement.app.util.CurrencyFormatter

class PaymentDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentDetailsBinding
    private lateinit var viewModel: PaymentViewModel
    private lateinit var viewModelFactory: PaymentViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.payment_history)

        // Get payment ID from intent
        val paymentId = intent.getLongExtra(EXTRA_PAYMENT_ID, -1L)
        if (paymentId == -1L) {
            finish()
            return
        }

        // Initialize ViewModel
        val application = application as ConstructionApp
        viewModelFactory = PaymentViewModelFactory(
            application.paymentRepository,
            application.workerRepository,
            application.siteRepository
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[PaymentViewModel::class.java]

        // Load payment details
        viewModel.getPaymentById(paymentId).observe(this) { payment ->
            if (payment != null) {
                binding.textAmount.text = CurrencyFormatter.formatRupees(payment.amount)
                binding.textDate.text = payment.paymentDate
                binding.textDescription.text = payment.description
                binding.textPaymentMode.text = payment.paymentMode.name
                binding.textReferenceNumber.text = payment.referenceNumber ?: "N/A"

                // Load worker and site details
                viewModel.allWorkers.observe(this) { workers ->
                    val worker = workers.find { it.id == payment.workerId }
                    binding.textWorkerName.text = worker?.name ?: "Worker ID: ${payment.workerId}"
                }

                viewModel.allSites.observe(this) { sites ->
                    val site = sites.find { it.siteId == payment.siteId }
                    binding.textSiteName.text = site?.name ?: "Site ID: ${payment.siteId}"
                }
            } else {
                finish()
            }
        }
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

    companion object {
        const val EXTRA_PAYMENT_ID = "extra_payment_id"
    }
}
