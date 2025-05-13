package com.constructionmanagement.app.ui.worker

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.constructionmanagement.app.ConstructionApp
import com.constructionmanagement.app.R
import com.constructionmanagement.app.data.model.Worker
import com.constructionmanagement.app.databinding.ActivityWorkerDetailsBinding
import java.io.File

class WorkerDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkerDetailsBinding
    private lateinit var viewModel: WorkerViewModel
    private lateinit var viewModelFactory: WorkerViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkerDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.worker_details)

        // Get worker ID from intent
        val workerId = intent.getLongExtra(EXTRA_WORKER_ID, -1L)
        if (workerId == -1L) {
            finish()
            return
        }

        // Initialize ViewModel
        val application = application as ConstructionApp
        viewModelFactory = WorkerViewModelFactory(
            application.workerRepository,
            application.workerSiteAssignmentRepository
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[WorkerViewModel::class.java]

        // Load worker details
        viewModel.getWorkerById(workerId).observe(this) { worker ->
            if (worker != null) {
                displayWorkerDetails(worker)
                setupEditButton(worker.id)
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

    private fun displayWorkerDetails(worker: Worker) {
        // Set header text
        binding.textNameHeader.text = worker.name
        binding.textRoleHeader.text = worker.role

        // Set details text
        binding.textName.text = worker.name
        binding.textPhone.text = worker.phoneNumber
        binding.textAddress.text = worker.address

        // Set status with color
        val statusText = if (worker.isActive) "Active" else "Inactive"
        binding.textStatus.text = statusText
        binding.textStatus.setTextColor(
            ContextCompat.getColor(
                this,
                if (worker.isActive) R.color.success else R.color.secondary_text
            )
        )

        binding.textRole.text = worker.role
        binding.textJoinDate.text = worker.joinDate
        binding.textAadhar.text = worker.aadharNumber

        // Load profile image if available
        if (worker.profileImagePath != null) {
            binding.imageWorker.visibility = View.VISIBLE
            val imageFile = File(worker.profileImagePath)
            if (imageFile.exists()) {
                Glide.with(this)
                    .load(imageFile)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .centerCrop()
                    .into(binding.imageWorker)
            } else {
                binding.imageWorker.setImageResource(R.drawable.ic_launcher_foreground)
            }
        } else {
            binding.imageWorker.visibility = View.GONE
        }
    }

    private fun setupEditButton(workerId: Long) {
        binding.buttonEditWorker.setOnClickListener {
            val intent = Intent(this, EditWorkerActivity::class.java).apply {
                putExtra("worker_id", workerId)
            }
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh worker details when returning from edit screen
        val workerId = intent.getLongExtra(EXTRA_WORKER_ID, -1L)
        if (workerId != -1L) {
            viewModel.getWorkerById(workerId).observe(this) { worker ->
                if (worker != null) {
                    displayWorkerDetails(worker)
                }
            }
        }
    }

    companion object {
        const val EXTRA_WORKER_ID = "extra_worker_id"
    }
}
