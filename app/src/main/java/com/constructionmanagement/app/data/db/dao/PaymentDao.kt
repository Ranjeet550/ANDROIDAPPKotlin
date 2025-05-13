package com.constructionmanagement.app.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.constructionmanagement.app.data.model.Payment

@Dao
interface PaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Update
    suspend fun updatePayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment)

    @Query("SELECT * FROM payments WHERE paymentId = :paymentId")
    fun getPaymentById(paymentId: Long): LiveData<Payment>

    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPayments(): LiveData<List<Payment>>

    @Query("SELECT * FROM payments WHERE workerId = :workerId ORDER BY paymentDate DESC")
    fun getPaymentsForWorker(workerId: Long): LiveData<List<Payment>>

    @Query("SELECT * FROM payments WHERE siteId = :siteId ORDER BY paymentDate DESC")
    fun getPaymentsForSite(siteId: Long): LiveData<List<Payment>>

    @Query("SELECT * FROM payments WHERE forMonth = :month AND forYear = :year ORDER BY paymentDate DESC")
    fun getPaymentsForMonthYear(month: Int, year: Int): LiveData<List<Payment>>

    @Query("SELECT * FROM payments WHERE paymentDate BETWEEN :startDate AND :endDate ORDER BY paymentDate DESC")
    fun getPaymentsByDateRange(startDate: String, endDate: String): LiveData<List<Payment>>

    @Query("SELECT SUM(amount) FROM payments WHERE workerId = :workerId")
    fun getTotalPaymentsForWorker(workerId: Long): LiveData<Double>

    @Query("SELECT SUM(amount) FROM payments WHERE siteId = :siteId")
    fun getTotalPaymentsForSite(siteId: Long): LiveData<Double>

    @Query("SELECT SUM(amount) FROM payments WHERE forMonth = :month AND forYear = :year")
    fun getTotalPaymentsForMonthYear(month: Int, year: Int): LiveData<Double>
}
