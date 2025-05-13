package com.constructionmanagement.app.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.constructionmanagement.app.data.model.Site
import com.constructionmanagement.app.data.model.SiteStatus
import com.constructionmanagement.app.data.model.Worker
import com.constructionmanagement.app.data.repository.PaymentRepository
import com.constructionmanagement.app.data.repository.SiteRepository
import com.constructionmanagement.app.data.repository.WorkerRepository

class DashboardViewModel(
    private val workerRepository: WorkerRepository,
    private val siteRepository: SiteRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    // Workers
    val totalWorkers: LiveData<List<Worker>> = workerRepository.allWorkers
    val activeWorkers: LiveData<List<Worker>> = workerRepository.activeWorkers
    
    // Sites
    val allSites: LiveData<List<Site>> = siteRepository.allSites
    
    // Active sites
    private val _activeSites = MediatorLiveData<List<Site>>()
    val activeSites: LiveData<List<Site>> = _activeSites
    
    // Recent payments
    val recentPayments = paymentRepository.allPayments
    
    init {
        _activeSites.addSource(allSites) { sites ->
            _activeSites.value = sites.filter { it.status == SiteStatus.ACTIVE }
        }
    }
}
