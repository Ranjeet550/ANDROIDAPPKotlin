package com.constructionmanagement.app.ui.report

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.constructionmanagement.app.ConstructionApp
import com.constructionmanagement.app.R
import com.constructionmanagement.app.data.model.Site
import com.constructionmanagement.app.data.model.Worker
import com.constructionmanagement.app.databinding.FragmentReportBinding
import java.text.SimpleDateFormat
import java.util.*

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ReportViewModel
    private lateinit var viewModelFactory: ReportViewModelFactory

    private var selectedStartDate: Calendar = Calendar.getInstance()
    private var selectedEndDate: Calendar = Calendar.getInstance()
    private var selectedWorkerId: Long = -1
    private var selectedSiteId: Long = -1
    private var workers = listOf<Worker>()
    private var sites = listOf<Site>()

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        val application = requireActivity().application as ConstructionApp
        viewModelFactory = ReportViewModelFactory(
            application,
            application.workerRepository,
            application.siteRepository,
            application.paymentRepository,
            application.advanceRepository,
            application.attendanceRepository
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[ReportViewModel::class.java]

        // Setup UI components
        setupDatePickers()
        setupSpinners()
        setupReportTypeRadioGroup()
        setupGenerateButton()

        // Observe data
        observeData()

        // Set default dates
        val today = Calendar.getInstance()
        selectedEndDate = today

        val startOfMonth = Calendar.getInstance()
        startOfMonth.set(Calendar.DAY_OF_MONTH, 1)
        selectedStartDate = startOfMonth

        updateDateButtons()
    }

    private fun setupDatePickers() {
        // Start date picker
        binding.buttonStartDate.setOnClickListener {
            showDatePickerDialog(selectedStartDate) { year, month, day ->
                selectedStartDate.set(year, month, day)
                updateDateButtons()
            }
        }

        // End date picker
        binding.buttonEndDate.setOnClickListener {
            showDatePickerDialog(selectedEndDate) { year, month, day ->
                selectedEndDate.set(year, month, day)
                updateDateButtons()
            }
        }
    }

    private fun showDatePickerDialog(calendar: Calendar, onDateSet: (Int, Int, Int) -> Unit) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                onDateSet(selectedYear, selectedMonth, selectedDay)
            },
            year,
            month,
            day
        ).show()
    }

    private fun updateDateButtons() {
        binding.buttonStartDate.text = dateFormat.format(selectedStartDate.time)
        binding.buttonEndDate.text = dateFormat.format(selectedEndDate.time)
    }

    private fun setupSpinners() {
        // Worker spinner
        binding.spinnerWorker.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0 && workers.isNotEmpty()) {
                    selectedWorkerId = workers[position - 1].id
                } else {
                    selectedWorkerId = -1 // All workers
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedWorkerId = -1
            }
        }

        // Site spinner
        binding.spinnerSite.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0 && sites.isNotEmpty()) {
                    selectedSiteId = sites[position - 1].siteId
                } else {
                    selectedSiteId = -1 // All sites
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedSiteId = -1
            }
        }
    }

    private fun setupReportTypeRadioGroup() {
        binding.radioGroupReportType.setOnCheckedChangeListener { _, checkedId ->
            // Show/hide relevant filters based on report type
            when (checkedId) {
                R.id.radio_worker_list -> {
                    binding.layoutDateRange.visibility = View.GONE
                    binding.layoutSiteSelection.visibility = View.GONE
                    binding.layoutWorkerSelection.visibility = View.GONE
                }
                R.id.radio_payment_history -> {
                    binding.layoutDateRange.visibility = View.VISIBLE
                    binding.layoutSiteSelection.visibility = View.VISIBLE
                    binding.layoutWorkerSelection.visibility = View.VISIBLE
                }
                R.id.radio_advance_payment -> {
                    binding.layoutDateRange.visibility = View.VISIBLE
                    binding.layoutSiteSelection.visibility = View.GONE
                    binding.layoutWorkerSelection.visibility = View.VISIBLE
                }
                R.id.radio_site_summary -> {
                    binding.layoutDateRange.visibility = View.VISIBLE
                    binding.layoutSiteSelection.visibility = View.VISIBLE
                    binding.layoutWorkerSelection.visibility = View.GONE
                }
                R.id.radio_attendance -> {
                    binding.layoutDateRange.visibility = View.VISIBLE
                    binding.layoutSiteSelection.visibility = View.VISIBLE
                    binding.layoutWorkerSelection.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupGenerateButton() {
        binding.buttonGenerateReport.setOnClickListener {
            generateReport()
        }
    }

    private fun generateReport() {
        // Get report type
        val reportType = when (binding.radioGroupReportType.checkedRadioButtonId) {
            R.id.radio_worker_list -> ReportType.WORKER_LIST
            R.id.radio_payment_history -> ReportType.PAYMENT_HISTORY
            R.id.radio_advance_payment -> ReportType.ADVANCE_PAYMENT
            R.id.radio_site_summary -> ReportType.SITE_SUMMARY
            R.id.radio_attendance -> ReportType.ATTENDANCE
            else -> ReportType.WORKER_LIST
        }

        // Get export format
        val exportFormat = when (binding.radioGroupExportFormat.checkedRadioButtonId) {
            R.id.radio_pdf -> ExportFormat.PDF
            R.id.radio_excel -> ExportFormat.EXCEL
            else -> ExportFormat.PDF
        }

        // Validate dates if needed
        if (reportType != ReportType.WORKER_LIST) {
            if (selectedStartDate.after(selectedEndDate)) {
                Toast.makeText(
                    requireContext(),
                    "Start date cannot be after end date",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }

        // Launch report generation activity
        val intent = Intent(requireContext(), ReportGenerationActivity::class.java).apply {
            putExtra("reportType", reportType)
            putExtra("exportFormat", exportFormat)

            // Add date range if applicable
            if (reportType != ReportType.WORKER_LIST) {
                putExtra("startDate", dateFormat.format(selectedStartDate.time))
                putExtra("endDate", dateFormat.format(selectedEndDate.time))
            }

            // Add worker ID if applicable
            if (reportType == ReportType.PAYMENT_HISTORY ||
                reportType == ReportType.ADVANCE_PAYMENT ||
                reportType == ReportType.ATTENDANCE) {
                if (selectedWorkerId != -1L) {
                    putExtra("workerId", selectedWorkerId)
                }
            }

            // Add site ID if applicable
            if (reportType == ReportType.PAYMENT_HISTORY ||
                reportType == ReportType.SITE_SUMMARY ||
                reportType == ReportType.ATTENDANCE) {
                if (selectedSiteId != -1L) {
                    putExtra("siteId", selectedSiteId)
                }
            }
        }

        startActivity(intent)
    }

    private fun observeData() {
        // Observe workers
        viewModel.allWorkers.observe(viewLifecycleOwner) { workerList ->
            workers = workerList

            // Create spinner adapter with "All Workers" option
            val workerNames = mutableListOf("All Workers")
            workerNames.addAll(workerList.map { it.name })

            val workerAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                workerNames
            )
            workerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerWorker.adapter = workerAdapter
        }

        // Observe sites
        viewModel.allSites.observe(viewLifecycleOwner) { siteList ->
            sites = siteList

            // Create spinner adapter with "All Sites" option
            val siteNames = mutableListOf("All Sites")
            siteNames.addAll(siteList.map { it.name })

            val siteAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                siteNames
            )
            siteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerSite.adapter = siteAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
