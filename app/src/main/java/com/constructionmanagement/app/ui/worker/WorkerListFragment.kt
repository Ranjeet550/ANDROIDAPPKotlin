package com.constructionmanagement.app.ui.worker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.constructionmanagement.app.ConstructionApp
import com.constructionmanagement.app.databinding.FragmentWorkerListBinding

class WorkerListFragment : Fragment() {

    private var _binding: FragmentWorkerListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: WorkerViewModel
    private lateinit var viewModelFactory: WorkerViewModelFactory
    private lateinit var adapter: WorkerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkerListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel
        val application = requireActivity().application as ConstructionApp
        viewModelFactory = WorkerViewModelFactory(
            application.workerRepository,
            application.workerSiteAssignmentRepository
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[WorkerViewModel::class.java]
        
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }
    
    private fun setupRecyclerView() {
        adapter = WorkerAdapter { workerId ->
            viewModel.onWorkerClicked(workerId)
        }
        binding.recyclerWorkers.adapter = adapter
    }
    
    private fun setupObservers() {
        // Observe all workers
        viewModel.allWorkers.observe(viewLifecycleOwner) { workers ->
            if (binding.chipAll.isChecked) {
                adapter.submitList(workers)
                binding.textNoWorkers.visibility = if (workers.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        
        // Observe active workers
        viewModel.activeWorkers.observe(viewLifecycleOwner) { workers ->
            if (binding.chipActive.isChecked) {
                adapter.submitList(workers)
                binding.textNoWorkers.visibility = if (workers.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        
        // Observe search results
        viewModel.searchResults.observe(viewLifecycleOwner) { workers ->
            if (binding.searchEditText.text.toString().isNotBlank()) {
                adapter.submitList(workers)
                binding.textNoWorkers.visibility = if (workers.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        
        // Observe navigation to worker details
        viewModel.navigateToWorkerDetails.observe(viewLifecycleOwner) { workerId ->
            workerId?.let {
                // Navigate to worker details activity
                val intent = Intent(requireContext(), WorkerDetailsActivity::class.java).apply {
                    putExtra(WorkerDetailsActivity.EXTRA_WORKER_ID, workerId)
                }
                startActivity(intent)
                viewModel.onWorkerDetailsNavigated()
            }
        }
    }
    
    private fun setupListeners() {
        // Search functionality
        binding.searchEditText.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrBlank()) {
                updateWorkerList()
            } else {
                viewModel.searchWorkers(text.toString())
            }
        }
        
        // Filter chips
        binding.chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) updateWorkerList()
        }
        
        binding.chipActive.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) updateWorkerList()
        }
        
        binding.chipInactive.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) updateWorkerList()
        }
        
        // Add worker FAB
        binding.fabAddWorker.setOnClickListener {
            val intent = Intent(requireContext(), AddWorkerActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun updateWorkerList() {
        val searchText = binding.searchEditText.text.toString()
        
        if (searchText.isNotBlank()) {
            viewModel.searchWorkers(searchText)
            return
        }
        
        when {
            binding.chipAll.isChecked -> {
                adapter.submitList(viewModel.allWorkers.value)
                binding.textNoWorkers.visibility = 
                    if (viewModel.allWorkers.value?.isEmpty() == true) View.VISIBLE else View.GONE
            }
            binding.chipActive.isChecked -> {
                adapter.submitList(viewModel.activeWorkers.value)
                binding.textNoWorkers.visibility = 
                    if (viewModel.activeWorkers.value?.isEmpty() == true) View.VISIBLE else View.GONE
            }
            binding.chipInactive.isChecked -> {
                val inactiveWorkers = viewModel.allWorkers.value?.filter { !it.isActive }
                adapter.submitList(inactiveWorkers)
                binding.textNoWorkers.visibility = 
                    if (inactiveWorkers?.isEmpty() == true) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
