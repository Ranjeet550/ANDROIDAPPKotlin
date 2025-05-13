package com.constructionmanagement.app.ui.payment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constructionmanagement.app.data.model.Payment
import com.constructionmanagement.app.data.model.Site
import com.constructionmanagement.app.data.model.Worker
import com.constructionmanagement.app.data.repository.PaymentRepository
import com.constructionmanagement.app.data.repository.SiteRepository
import com.constructionmanagement.app.data.repository.WorkerRepository
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val paymentRepository: PaymentRepository,
    private val workerRepository: WorkerRepository,
    private val siteRepository: SiteRepository
) : ViewModel() {

    val allPayments: LiveData<List<Payment>> = paymentRepository.allPayments
    val allWorkers: LiveData<List<Worker>> = workerRepository.allWorkers
    val allSites: LiveData<List<Site>> = siteRepository.allSites

    private val _navigateToPaymentDetails = MutableLiveData<Long?>()
    val navigateToPaymentDetails: LiveData<Long?> = _navigateToPaymentDetails

    fun getPaymentById(paymentId: Long): LiveData<Payment> {
        return paymentRepository.getPaymentById(paymentId)
    }

    fun getPaymentsByWorker(workerId: Long): LiveData<List<Payment>> {
        return paymentRepository.getPaymentsForWorker(workerId)
    }

    fun getPaymentsBySite(siteId: Long): LiveData<List<Payment>> {
        return paymentRepository.getPaymentsForSite(siteId)
    }

    fun getPaymentsByDateRange(startDate: String, endDate: String): LiveData<List<Payment>> {
        return paymentRepository.getPaymentsByDateRange(startDate, endDate)
    }

    fun insertPayment(payment: Payment) {
        viewModelScope.launch {
            paymentRepository.insertPayment(payment)
        }
    }

    fun updatePayment(payment: Payment) {
        viewModelScope.launch {
            paymentRepository.updatePayment(payment)
        }
    }

    fun deletePayment(payment: Payment) {
        viewModelScope.launch {
            paymentRepository.deletePayment(payment)
        }
    }

    fun onPaymentClicked(paymentId: Long) {
        _navigateToPaymentDetails.value = paymentId
    }

    fun onPaymentDetailsNavigated() {
        _navigateToPaymentDetails.value = null
    }
}
