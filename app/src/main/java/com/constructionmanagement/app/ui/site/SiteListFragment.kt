package com.constructionmanagement.app.ui.site

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.constructionmanagement.app.ConstructionApp
import com.constructionmanagement.app.databinding.FragmentSiteListBinding

class SiteListFragment : Fragment() {

    private var _binding: FragmentSiteListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SiteViewModel
    private lateinit var viewModelFactory: SiteViewModelFactory
    private lateinit var adapter: SiteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSiteListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        val application = requireActivity().application as ConstructionApp
        viewModelFactory = SiteViewModelFactory(application.siteRepository)
        viewModel = ViewModelProvider(this, viewModelFactory)[SiteViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = SiteAdapter { siteId ->
            viewModel.onSiteClicked(siteId)
        }
        binding.recyclerSites.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSites.adapter = adapter
    }

    private fun setupObservers() {
        // Observe all sites
        viewModel.allSites.observe(viewLifecycleOwner) { sites ->
            if (binding.chipAll.isChecked) {
                adapter.submitList(sites)
                binding.textNoSites.visibility = if (sites.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        // Observe active sites
        viewModel.activeSites.observe(viewLifecycleOwner) { sites ->
            if (binding.chipActive.isChecked) {
                adapter.submitList(sites)
                binding.textNoSites.visibility = if (sites.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        // Observe search results
        viewModel.searchResults.observe(viewLifecycleOwner) { sites ->
            if (binding.searchEditText.text.toString().isNotBlank()) {
                adapter.submitList(sites)
                binding.textNoSites.visibility = if (sites.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        // Observe navigation to site details
        viewModel.navigateToSiteDetails.observe(viewLifecycleOwner) { siteId ->
            siteId?.let {
                // Navigate to site details activity
                val intent = Intent(requireContext(), SiteDetailsActivity::class.java).apply {
                    putExtra(SiteDetailsActivity.EXTRA_SITE_ID, siteId)
                }
                startActivity(intent)
                viewModel.onSiteDetailsNavigated()
            }
        }
    }

    private fun setupListeners() {
        // Search functionality
        binding.searchEditText.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrBlank()) {
                updateSiteList()
            } else {
                viewModel.searchSites(text.toString())
            }
        }

        // Filter chips
        binding.chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) updateSiteList()
        }

        binding.chipActive.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) updateSiteList()
        }

        binding.chipCompleted.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) updateSiteList()
        }

        // Add site FAB
        binding.fabAddSite.setOnClickListener {
            val intent = Intent(requireContext(), AddSiteActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateSiteList() {
        val searchText = binding.searchEditText.text.toString()

        if (searchText.isNotBlank()) {
            viewModel.searchSites(searchText)
            return
        }

        when {
            binding.chipAll.isChecked -> {
                adapter.submitList(viewModel.allSites.value)
                binding.textNoSites.visibility =
                    if (viewModel.allSites.value?.isEmpty() == true) View.VISIBLE else View.GONE
            }
            binding.chipActive.isChecked -> {
                adapter.submitList(viewModel.activeSites.value)
                binding.textNoSites.visibility =
                    if (viewModel.activeSites.value?.isEmpty() == true) View.VISIBLE else View.GONE
            }
            binding.chipCompleted.isChecked -> {
                // This would be better with a dedicated LiveData in the ViewModel
                val completedSites = viewModel.allSites.value?.filter {
                    it.status == com.constructionmanagement.app.data.model.SiteStatus.COMPLETED
                }
                adapter.submitList(completedSites)
                binding.textNoSites.visibility =
                    if (completedSites?.isEmpty() == true) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh the list when returning to this fragment
        updateSiteList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
