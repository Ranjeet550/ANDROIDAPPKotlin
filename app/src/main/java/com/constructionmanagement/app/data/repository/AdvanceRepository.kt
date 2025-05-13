package com.constructionmanagement.app.data.repository

import androidx.lifecycle.LiveData
import com.constructionmanagement.app.data.db.dao.AdvanceDao
import com.constructionmanagement.app.data.model.Advance

class AdvanceRepository(private val advanceDao: AdvanceDao) {

    val allAdvances: LiveData<List<Advance>> = advanceDao.getAllAdvances()

    fun getAdvanceById(advanceId: Long): LiveData<Advance> {
        return advanceDao.getAdvanceById(advanceId)
    }

    fun getAdvancesForWorker(workerId: Long): LiveData<List<Advance>> {
        return advanceDao.getAdvancesForWorker(workerId)
    }

    fun getUnsettledAdvancesForWorker(workerId: Long): LiveData<List<Advance>> {
        return advanceDao.getUnsettledAdvancesForWorker(workerId)
    }

    fun getAdvancesByDateRange(startDate: String, endDate: String): LiveData<List<Advance>> {
        return advanceDao.getAdvancesByDateRange(startDate, endDate)
    }

    fun getTotalUnsettledAdvancesForWorker(workerId: Long): LiveData<Double> {
        return advanceDao.getTotalUnsettledAdvancesForWorker(workerId)
    }

    suspend fun insertAdvance(advance: Advance): Long {
        return advanceDao.insertAdvance(advance)
    }

    suspend fun updateAdvance(advance: Advance) {
        advanceDao.updateAdvance(advance)
    }

    suspend fun deleteAdvance(advance: Advance) {
        advanceDao.deleteAdvance(advance)
    }

    suspend fun settleAdvances(advanceIds: List<Long>) {
        advanceDao.settleAdvances(advanceIds)
    }
}
