package com.constructionmanagement.app.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.constructionmanagement.app.data.repository.PaymentRepository
import com.constructionmanagement.app.data.repository.SiteRepository
import com.constructionmanagement.app.data.repository.WorkerRepository

class PaymentViewModelFactory(
    private val paymentRepository: PaymentRepository,
    private val workerRepository: WorkerRepository,
    private val siteRepository: SiteRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaymentViewModel(paymentRepository, workerRepository, siteRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
