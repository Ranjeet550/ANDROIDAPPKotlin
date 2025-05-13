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
import com.constructionmanagement.app.databinding.ActivityAddSiteBinding
import com.constructionmanagement.app.databinding.DialogSelectWorkersBinding
import com.constructionmanagement.app.ui.worker.WorkerViewModel
import com.constructionmanagement.app.ui.worker.WorkerViewModelFactory
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddSiteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddSiteBinding
    private lateinit var siteViewModel: SiteViewModel
    private lateinit var siteViewModelFactory: SiteViewModelFactory
    private lateinit var workerViewModel: WorkerViewModel
    private lateinit var workerViewModelFactory: WorkerViewModelFactory

    private lateinit var selectedWorkerAdapter: SelectedWorkerAdapter
    private val selectedWorkers = mutableListOf<Worker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSiteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.add_site)
        // Hide action bar since we have our own title
        supportActionBar?.hide()

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

        // Setup status dropdown
        setupStatusDropdown()

        // Setup date picker
        setupDatePicker()

        // Setup worker selection
        setupWorkerSelection()

        // Set up save button
        binding.buttonSave.setOnClickListener {
            if (validateInputs()) {
                saveSite()
            }
        }
    }

    private fun setupStatusDropdown() {
        val statusItems = SiteStatus.values().map { it.name }
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, statusItems)
        (binding.dropdownStatus as? MaterialAutoCompleteTextView)?.setAdapter(adapter)

        // Set default selection to ACTIVE
        binding.dropdownStatus.setText(SiteStatus.ACTIVE.name, false)
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

        binding.layoutExpectedEndDate.setEndIconOnClickListener {
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupWorkerSelection() {
        // Initialize selected worker adapter
        selectedWorkerAdapter = SelectedWorkerAdapter { worker ->
            // Remove worker from selection
            selectedWorkers.remove(worker)
            updateSelectedWorkersUI()
        }

        binding.recyclerSelectedWorkers.apply {
            layoutManager = LinearLayoutManager(this@AddSiteActivity)
            adapter = selectedWorkerAdapter
        }

        // Set up add workers button
        binding.buttonAddWorkers.setOnClickListener {
            showWorkerSelectionDialog()
        }

        // Initial UI update
        updateSelectedWorkersUI()
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
            layoutManager = LinearLayoutManager(this@AddSiteActivity)
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

        // Validate status
        if (binding.dropdownStatus.text.toString().trim().isEmpty()) {
            binding.layoutStatus.error = "Status is required"
            isValid = false
        } else {
            binding.layoutStatus.error = null
        }

        return isValid
    }

    private fun saveSite() {
        val name = binding.editName.text.toString().trim()
        val address = binding.editAddress.text.toString().trim()
        val clientName = binding.editClientName.text.toString().trim()
        val clientContact = binding.editClientContact.text.toString().trim()
        val notes = binding.editNotes.text.toString().trim()

        // Get current date as start date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDate = dateFormat.format(Date())

        // Get expected end date if provided
        val expectedEndDate = if (binding.editExpectedEndDate.text.toString().trim().isNotEmpty()) {
            binding.editExpectedEndDate.text.toString().trim()
        } else {
            null
        }

        // Get status from dropdown
        val statusString = binding.dropdownStatus.text.toString()
        val status = try {
            SiteStatus.valueOf(statusString)
        } catch (e: IllegalArgumentException) {
            SiteStatus.ACTIVE // Default to ACTIVE if there's an issue
        }

        // Create site object
        val site = Site(
            siteId = 0, // Room will auto-generate the ID
            name = name,
            address = address,
            clientName = clientName,
            clientContact = clientContact,
            startDate = startDate,
            expectedEndDate = expectedEndDate,
            status = status,
            notes = notes.ifEmpty { null }
        )

        // Save site to database and get the generated ID
        siteViewModel.insertSite(site) { siteId ->
            // Assign selected workers to the site
            if (selectedWorkers.isNotEmpty()) {
                val assignments = selectedWorkers.map { worker ->
                    WorkerSiteAssignment(
                        workerId = worker.id,
                        siteId = siteId,
                        assignmentDate = startDate
                    )
                }

                // Insert worker-site assignments
                workerViewModel.insertWorkerSiteAssignments(assignments)
            }

            Toast.makeText(this, "Site added successfully", Toast.LENGTH_SHORT).show()
            finish()
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
