package com.constructionmanagement.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.constructionmanagement.app.data.repository.PaymentRepository
import com.constructionmanagement.app.data.repository.SiteRepository
import com.constructionmanagement.app.data.repository.WorkerRepository

class DashboardViewModelFactory(
    private val workerRepository: WorkerRepository,
    private val siteRepository: SiteRepository,
    private val paymentRepository: PaymentRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(workerRepository, siteRepository, paymentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
