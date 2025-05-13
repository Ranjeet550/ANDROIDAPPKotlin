package com.constructionmanagement.app.ui.site

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.constructionmanagement.app.ConstructionApp
import com.constructionmanagement.app.R
import com.constructionmanagement.app.data.model.Site
import com.constructionmanagement.app.data.model.SiteStatus
import com.constructionmanagement.app.data.model.Worker
import com.constructionmanagement.app.data.model.WorkerSiteAssignment
import com.constructionmanagement.app.databinding.ActivityEditSiteBinding
import com.constructionmanagement.app.databinding.DialogSelectWorkersBinding
import com.constructionmanagement.app.ui.worker.WorkerViewModel
import com.constructionmanagement.app.ui.worker.WorkerViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditSiteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditSiteBinding
    private lateinit var siteViewModel: SiteViewModel
    private lateinit var siteViewModelFactory: SiteViewModelFactory
    private lateinit var workerViewModel: WorkerViewModel
    private lateinit var workerViewModelFactory: WorkerViewModelFactory

    private lateinit var selectedWorkerAdapter: SelectedWorkerAdapter
    private val selectedWorkers = mutableListOf<Worker>()

    private var siteId: Long = 0
    private var site: Site? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditSiteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get site ID from intent
        siteId = intent.getLongExtra("site_id", 0)
        if (siteId == 0L) {
            Toast.makeText(this, "Invalid site ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.edit_site)

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

        // Setup status spinner
        val statusAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            SiteStatus.values().map { it.name }
        )
        binding.spinnerStatus.adapter = statusAdapter

        // Setup worker selection
        setupWorkerSelection()

        // Setup date picker
        setupDatePicker()

        // Load site details
        siteViewModel.getSiteById(siteId).observe(this) { siteData ->
            if (siteData != null) {
                site = siteData
                populateSiteData(siteData)

                // Load workers assigned to this site
                workerViewModel.getWorkersBySite(siteId).observe(this) { workers ->
                    selectedWorkers.clear()
                    selectedWorkers.addAll(workers)
                    updateSelectedWorkersUI()
                }
            } else {
                Toast.makeText(this, "Site not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // Set up save button
        binding.buttonSave.setOnClickListener {
            if (validateInputs()) {
                updateSite()
            }
        }
    }

    private fun populateSiteData(site: Site) {
        binding.editName.setText(site.name)
        binding.editAddress.setText(site.address)
        binding.editClientName.setText(site.clientName)
        binding.editClientContact.setText(site.clientContact)
        binding.editExpectedEndDate.setText(site.expectedEndDate ?: "")
        binding.editNotes.setText(site.notes ?: "")

        // Set spinner selection
        val statusPosition = SiteStatus.values().indexOfFirst { it == site.status }
        if (statusPosition != -1) {
            binding.spinnerStatus.setSelection(statusPosition)
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validate name
        if (binding.editName.text.toString().trim().isEmpty()) {
            binding.layoutName.error = "Site name is required"
            isValid = false
        } else {
            binding.layoutName.error = null
        }

        // Validate address
        if (binding.editAddress.text.toString().trim().isEmpty()) {
            binding.layoutAddress.error = "Site address is required"
            isValid = false
        } else {
            binding.layoutAddress.error = null
        }

        // Validate client name
        if (binding.editClientName.text.toString().trim().isEmpty()) {
            binding.layoutClientName.error = "Client name is required"
            isValid = false
        } else {
            binding.layoutClientName.error = null
        }

        return isValid
    }

    private fun setupWorkerSelection() {
        // Initialize selected worker adapter
        selectedWorkerAdapter = SelectedWorkerAdapter { worker ->
            // Remove worker from selection
            selectedWorkers.remove(worker)
            updateSelectedWorkersUI()
        }

        binding.recyclerSelectedWorkers.apply {
            layoutManager = LinearLayoutManager(this@EditSiteActivity)
            adapter = selectedWorkerAdapter
        }

        // Set up add workers button
        binding.buttonAddWorkers.setOnClickListener {
            showWorkerSelectionDialog()
        }
    }

    private fun updateSelectedWorkersUI() {
        if (selectedWorkers.isEmpty()) {
            binding.recyclerSelectedWorkers.visibility = View.GONE
            binding.textNoWorkersSelected.visibility = View.VISIBLE
        } else {
            binding.recyclerSelectedWorkers.visibility = View.VISIBLE
            binding.textNoWorkersSelected.visibility = View.GONE
            selectedWorkerAdapter.submitList(selectedWorkers.toList())
        }
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            binding.editExpectedEndDate.setText(dateFormat.format(calendar.time))
        }

        binding.editExpectedEndDate.setOnClickListener {
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun showWorkerSelectionDialog() {
        val dialogBinding = DialogSelectWorkersBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        // Initialize worker selection adapter
        val workerSelectionAdapter = WorkerSelectionAdapter { worker, isSelected ->
            if (isSelected) {
                if (!selectedWorkers.contains(worker)) {
                    selectedWorkers.add(worker)
                }
            } else {
                selectedWorkers.remove(worker)
            }
        }

        dialogBinding.recyclerWorkers.apply {
            layoutManager = LinearLayoutManager(this@EditSiteActivity)
            adapter = workerSelectionAdapter
        }

        // Load workers
        workerViewModel.allWorkers.observe(this) { workers ->
            val selectionItems = workers.map { worker ->
                WorkerSelectionAdapter.WorkerSelectionItem(
                    worker = worker,
                    isSelected = selectedWorkers.contains(worker)
                )
            }
            workerSelectionAdapter.submitList(selectionItems)
        }

        // Set up search functionality
        dialogBinding.editSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                workerViewModel.allWorkers.value?.let { workers ->
                    val filteredWorkers = if (query.isBlank()) {
                        workers
                    } else {
                        workers.filter { worker ->
                            worker.name.lowercase().contains(query) ||
                            worker.role.lowercase().contains(query)
                        }
                    }

                    val selectionItems = filteredWorkers.map { worker ->
                        WorkerSelectionAdapter.WorkerSelectionItem(
                            worker = worker,
                            isSelected = selectedWorkers.contains(worker)
                        )
                    }
                    workerSelectionAdapter.submitList(selectionItems)
                }
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Set up buttons
        dialogBinding.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.buttonConfirm.setOnClickListener {
            updateSelectedWorkersUI()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateSite() {
        val name = binding.editName.text.toString().trim()
        val address = binding.editAddress.text.toString().trim()
        val clientName = binding.editClientName.text.toString().trim()
        val clientContact = binding.editClientContact.text.toString().trim()
        val notes = binding.editNotes.text.toString().trim()

        // Get expected end date if provided
        val expectedEndDate = if (binding.editExpectedEndDate.text.toString().trim().isNotEmpty()) {
            binding.editExpectedEndDate.text.toString().trim()
        } else {
            null
        }

        // Get status from spinner
        val statusString = binding.spinnerStatus.selectedItem.toString()
        val status = SiteStatus.valueOf(statusString)

        // Create updated site object
        site?.let {
            val updatedSite = Site(
                siteId = it.siteId,
                name = name,
                address = address,
                clientName = clientName,
                clientContact = clientContact,
                startDate = it.startDate,
                expectedEndDate = expectedEndDate,
                status = status,
                notes = notes.ifEmpty { null }
            )

            // Update site in database
            siteViewModel.updateSite(updatedSite)

            // Update worker assignments
            updateWorkerAssignments(it.siteId)

            Toast.makeText(this, "Site updated successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updateWorkerAssignments(siteId: Long) {
        // Get current date for assignment date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val assignmentDate = dateFormat.format(Calendar.getInstance().time)

        // Get currently assigned workers
        workerViewModel.getWorkersBySite(siteId).value?.let { currentWorkers ->
            // Workers to remove (in current but not in selected)
            val workersToRemove = currentWorkers.filter { !selectedWorkers.contains(it) }

            // Workers to add (in selected but not in current)
            val workersToAdd = selectedWorkers.filter { worker ->
                !currentWorkers.any { it.id == worker.id }
            }

            // Remove workers
            for (worker in workersToRemove) {
                workerViewModel.getActiveAssignmentForWorker(worker.id).value?.let { assignment ->
                    val updatedAssignment = assignment.copy(isActive = false, endDate = assignmentDate)
                    workerViewModel.updateWorkerSiteAssignment(updatedAssignment)
                }
            }

            // Add workers
            for (worker in workersToAdd) {
                workerViewModel.assignWorkerToSite(worker.id, siteId, assignmentDate)
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
}
