package com.constructionmanagement.app.data.repository

import androidx.lifecycle.LiveData
import com.constructionmanagement.app.data.db.dao.PaymentDao
import com.constructionmanagement.app.data.model.Payment

class PaymentRepository(private val paymentDao: PaymentDao) {

    val allPayments: LiveData<List<Payment>> = paymentDao.getAllPayments()

    fun getPaymentById(paymentId: Long): LiveData<Payment> {
        return paymentDao.getPaymentById(paymentId)
    }

    fun getPaymentsForWorker(workerId: Long): LiveData<List<Payment>> {
        return paymentDao.getPaymentsForWorker(workerId)
    }

    fun getPaymentsForSite(siteId: Long): LiveData<List<Payment>> {
        return paymentDao.getPaymentsForSite(siteId)
    }

    fun getPaymentsForMonthYear(month: Int, year: Int): LiveData<List<Payment>> {
        return paymentDao.getPaymentsForMonthYear(month, year)
    }

    fun getPaymentsByDateRange(startDate: String, endDate: String): LiveData<List<Payment>> {
        return paymentDao.getPaymentsByDateRange(startDate, endDate)
    }

    fun getTotalPaymentsForWorker(workerId: Long): LiveData<Double> {
        return paymentDao.getTotalPaymentsForWorker(workerId)
    }

    fun getTotalPaymentsForSite(siteId: Long): LiveData<Double> {
        return paymentDao.getTotalPaymentsForSite(siteId)
    }

    fun getTotalPaymentsForMonthYear(month: Int, year: Int): LiveData<Double> {
        return paymentDao.getTotalPaymentsForMonthYear(month, year)
    }

    suspend fun insertPayment(payment: Payment): Long {
        return paymentDao.insertPayment(payment)
    }

    suspend fun updatePayment(payment: Payment) {
        paymentDao.updatePayment(payment)
    }

    suspend fun deletePayment(payment: Payment) {
        paymentDao.deletePayment(payment)
    }
}
