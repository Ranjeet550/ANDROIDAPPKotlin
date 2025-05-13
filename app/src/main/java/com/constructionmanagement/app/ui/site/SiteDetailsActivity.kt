package com.constructionmanagement.app.ui.site

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.constructionmanagement.app.ConstructionApp
import com.constructionmanagement.app.R
import com.constructionmanagement.app.data.model.Site
import com.constructionmanagement.app.data.model.SiteStatus
import com.constructionmanagement.app.data.model.Worker
import com.constructionmanagement.app.databinding.ActivitySiteDetailsBinding
import com.constructionmanagement.app.ui.worker.WorkerViewModel
import com.constructionmanagement.app.ui.worker.WorkerViewModelFactory

class SiteDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySiteDetailsBinding
    private lateinit var siteViewModel: SiteViewModel
    private lateinit var siteViewModelFactory: SiteViewModelFactory
    private lateinit var workerViewModel: WorkerViewModel
    private lateinit var workerViewModelFactory: WorkerViewModelFactory
    private lateinit var workerAdapter: WorkerDetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySiteDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.site_details)

        // Get site ID from intent
        val siteId = intent.getLongExtra(EXTRA_SITE_ID, -1L)
        if (siteId == -1L) {
            finish()
            return
        }

        // Initialize ViewModels
        val application = application as ConstructionApp

        // Site ViewModel
        siteViewModelFactory = SiteViewModelFactory(application.siteRepository)
        siteViewModel = ViewModelProvider(this, siteViewModelFactory)[SiteViewModel::class.java]

        // Worker ViewModel
        workerViewModelFactory = WorkerViewModelFactory(
            application.workerRepository,
            application.workerSiteAssignmentRepository
        )
        workerViewModel = ViewModelProvider(this, workerViewModelFactory)[WorkerViewModel::class.java]

        // Setup worker adapter
        setupWorkerAdapter()

        // Load site details
        siteViewModel.getSiteById(siteId).observe(this) { site ->
            if (site != null) {
                displaySiteDetails(site)
                setupEditButton(site.siteId)
            } else {
                finish()
            }
        }

        // Load worker count
        application.siteRepository.getWorkerCountForSite(siteId).observe(this) { count ->
            binding.textWorkerCount.text = "($count)"
        }

        // Load workers assigned to this site
        workerViewModel.getWorkersBySite(siteId).observe(this) { workers ->
            workerAdapter.submitList(workers)
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

    private fun displaySiteDetails(site: Site) {
        // Set header text
        binding.textNameHeader.text = site.name
        binding.textAddressHeader.text = site.address

        // Set status chip
        binding.chipSiteStatus.text = site.status.name
        val statusColor = when (site.status) {
            SiteStatus.ACTIVE -> R.color.success
            SiteStatus.COMPLETED -> R.color.completed
            SiteStatus.ON_HOLD -> R.color.warning
        }
        binding.chipSiteStatus.setChipBackgroundColorResource(statusColor)

        // Set details text
        binding.textName.text = site.name
        binding.textAddress.text = site.address
        binding.textClientName.text = site.clientName
        binding.textClientContact.text = site.clientContact

        // Set status with color
        binding.textStatus.text = site.status.name
        binding.textStatus.setTextColor(
            ContextCompat.getColor(
                this,
                when (site.status) {
                    SiteStatus.ACTIVE -> R.color.success
                    SiteStatus.COMPLETED -> R.color.completed
                    SiteStatus.ON_HOLD -> R.color.warning
                }
            )
        )

        binding.textStartDate.text = site.startDate
        binding.textEndDate.text = site.expectedEndDate ?: "Not specified"
        binding.textNotes.text = site.notes ?: "No notes"
    }

    private fun setupEditButton(siteId: Long) {
        binding.buttonEditSite.setOnClickListener {
            val intent = Intent(this, EditSiteActivity::class.java).apply {
                putExtra("site_id", siteId)
            }
            startActivity(intent)
        }
    }

    private fun setupWorkerAdapter() {
        workerAdapter = WorkerDetailAdapter()
        binding.recyclerWorkers.apply {
            layoutManager = LinearLayoutManager(this@SiteDetailsActivity)
            adapter = workerAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh site details when returning from edit screen
        val siteId = intent.getLongExtra(EXTRA_SITE_ID, -1L)
        if (siteId != -1L) {
            siteViewModel.getSiteById(siteId).observe(this) { site ->
                if (site != null) {
                    displaySiteDetails(site)
                }
            }

            // Refresh workers
            workerViewModel.getWorkersBySite(siteId).observe(this) { workers ->
                workerAdapter.submitList(workers)
            }
        }
    }

    companion object {
        const val EXTRA_SITE_ID = "extra_site_id"
    }
}
