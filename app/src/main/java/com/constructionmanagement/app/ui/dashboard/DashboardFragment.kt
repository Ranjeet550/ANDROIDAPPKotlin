package com.constructionmanagement.app.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.constructionmanagement.app.ConstructionApp
import com.constructionmanagement.app.R
import com.constructionmanagement.app.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DashboardViewModel
    private lateinit var viewModelFactory: DashboardViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up toolbar
        setupToolbar()

        // Initialize ViewModel
        val application = requireActivity().application as ConstructionApp
        viewModelFactory = DashboardViewModelFactory(
            application.workerRepository,
            application.siteRepository,
            application.paymentRepository
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[DashboardViewModel::class.java]

        setupObservers()
        setupClickListeners()
    }

    private fun setupToolbar() {
        // Hide the action bar since we're using our own toolbar
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        // Set up our toolbar
        binding.toolbar.title = "Dashboard"
    }

    private fun setupObservers() {
        // Observe total workers
        viewModel.totalWorkers.observe(viewLifecycleOwner) { workers ->
            binding.textTotalWorkers.text = workers.size.toString()
        }

        // Observe active workers
        viewModel.activeWorkers.observe(viewLifecycleOwner) { workers ->
            binding.textActiveWorkers.text = workers.size.toString()
        }

        // Observe all sites
        viewModel.allSites.observe(viewLifecycleOwner) { sites ->
            binding.textTotalSites.text = sites.size.toString()
        }

        // Observe active sites
        viewModel.activeSites.observe(viewLifecycleOwner) { sites ->
            binding.textActiveSites.text = sites.size.toString()
        }

        // Observe recent payments
        viewModel.recentPayments.observe(viewLifecycleOwner) { payments ->
            // TODO: Setup RecyclerView adapter for recent payments
        }
    }

    private fun setupClickListeners() {
        // View all workers
        binding.buttonViewWorkers.setOnClickListener {
            findNavController().navigate(R.id.navigation_workers)
        }

        // View all sites
        binding.buttonViewSites.setOnClickListener {
            findNavController().navigate(R.id.navigation_sites)
        }

        // View all payments
        binding.buttonViewPayments.setOnClickListener {
            findNavController().navigate(R.id.navigation_payments)
        }

        // Add new worker
        binding.buttonAddWorker.setOnClickListener {
            val intent = Intent(requireContext(), com.constructionmanagement.app.ui.worker.AddWorkerActivity::class.java)
            startActivity(intent)
        }

        // Add new site
        binding.buttonAddSite.setOnClickListener {
            val intent = Intent(requireContext(), com.constructionmanagement.app.ui.site.AddSiteActivity::class.java)
            startActivity(intent)
        }

        // Process monthly payment
        binding.buttonProcessPayment.setOnClickListener {
            val intent = Intent(requireContext(), com.constructionmanagement.app.ui.payment.ProcessPaymentActivity::class.java)
            startActivity(intent)
        }

        // Record advance payment
        binding.buttonRecordAdvance.setOnClickListener {
            val intent = Intent(requireContext(), com.constructionmanagement.app.ui.payment.AdvancePaymentActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Show the action bar again when leaving this fragment
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        _binding = null
    }
}
