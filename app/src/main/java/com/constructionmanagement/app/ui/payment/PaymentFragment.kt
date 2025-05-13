package com.constructionmanagement.app.ui.payment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.constructionmanagement.app.ConstructionApp
import com.constructionmanagement.app.databinding.FragmentPaymentBinding

class PaymentFragment : Fragment() {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PaymentViewModel
    private lateinit var viewModelFactory: PaymentViewModelFactory
    private lateinit var paymentAdapter: PaymentAdapter
    private lateinit var advanceAdapter: AdvanceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        val application = requireActivity().application as ConstructionApp
        viewModelFactory = PaymentViewModelFactory(
            application.paymentRepository,
            application.workerRepository,
            application.siteRepository
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[PaymentViewModel::class.java]

        setupRecyclerViews()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerViews() {
        // Setup payments recycler view
        paymentAdapter = PaymentAdapter(
            onPaymentClicked = { paymentId -> viewModel.onPaymentClicked(paymentId) },
            lifecycleOwner = viewLifecycleOwner,
            workerLiveData = viewModel.allWorkers
        )
        binding.recyclerPayments.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPayments.adapter = paymentAdapter

        // Setup advances recycler view
        val advanceViewModel = ViewModelProvider(
            this,
            AdvanceViewModelFactory(
                (requireActivity().application as ConstructionApp).advanceRepository,
                (requireActivity().application as ConstructionApp).workerRepository
            )
        )[AdvanceViewModel::class.java]

        advanceAdapter = AdvanceAdapter(
            onAdvanceClicked = { advanceId -> advanceViewModel.onAdvanceClicked(advanceId) },
            lifecycleOwner = viewLifecycleOwner,
            workerLiveData = advanceViewModel.allWorkers
        )
        binding.recyclerAdvances.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerAdvances.adapter = advanceAdapter

        // Observe advances
        advanceViewModel.allAdvances.observe(viewLifecycleOwner) { advances ->
            advanceAdapter.submitList(advances)
            binding.textNoAdvances.visibility = if (advances.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observe navigation to advance details
        advanceViewModel.navigateToAdvanceDetails.observe(viewLifecycleOwner) { advanceId ->
            advanceId?.let {
                // Navigate to advance details activity
                val intent = Intent(requireContext(), AdvanceDetailsActivity::class.java).apply {
                    putExtra(AdvanceDetailsActivity.EXTRA_ADVANCE_ID, advanceId)
                }
                startActivity(intent)
                advanceViewModel.onAdvanceDetailsNavigated()
            }
        }
    }

    private fun setupObservers() {
        // Observe all payments
        viewModel.allPayments.observe(viewLifecycleOwner) { payments ->
            paymentAdapter.submitList(payments)
            binding.textNoPayments.visibility = if (payments.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observe navigation to payment details
        viewModel.navigateToPaymentDetails.observe(viewLifecycleOwner) { paymentId ->
            paymentId?.let {
                // Navigate to payment details activity
                val intent = Intent(requireContext(), PaymentDetailsActivity::class.java).apply {
                    putExtra(PaymentDetailsActivity.EXTRA_PAYMENT_ID, paymentId)
                }
                startActivity(intent)
                viewModel.onPaymentDetailsNavigated()
            }
        }
    }

    private fun setupClickListeners() {
        // Process payment button
        binding.buttonProcessPayment.setOnClickListener {
            val intent = Intent(requireContext(), ProcessPaymentActivity::class.java)
            startActivity(intent)
        }

        // Record advance button
        binding.buttonRecordAdvance.setOnClickListener {
            val intent = Intent(requireContext(), AdvancePaymentActivity::class.java)
            startActivity(intent)
        }

        // Generate report button
        binding.buttonGenerateReport.setOnClickListener {
            val intent = Intent(requireContext(), PaymentReportActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this fragment
        viewModel.allPayments.value?.let { payments ->
            paymentAdapter.submitList(payments)
            binding.textNoPayments.visibility = if (payments.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
