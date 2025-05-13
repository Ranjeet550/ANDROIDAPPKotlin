package com.constructionmanagement.app.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.constructionmanagement.app.data.model.Advance

@Dao
interface AdvanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvance(advance: Advance): Long

    @Update
    suspend fun updateAdvance(advance: Advance)

    @Delete
    suspend fun deleteAdvance(advance: Advance)

    @Query("SELECT * FROM advances WHERE advanceId = :advanceId")
    fun getAdvanceById(advanceId: Long): LiveData<Advance>

    @Query("SELECT * FROM advances ORDER BY advanceDate DESC")
    fun getAllAdvances(): LiveData<List<Advance>>

    @Query("SELECT * FROM advances WHERE workerId = :workerId ORDER BY advanceDate DESC")
    fun getAdvancesForWorker(workerId: Long): LiveData<List<Advance>>

    @Query("SELECT * FROM advances WHERE workerId = :workerId AND isRecovered = 0 ORDER BY advanceDate DESC")
    fun getUnsettledAdvancesForWorker(workerId: Long): LiveData<List<Advance>>

    @Query("SELECT * FROM advances WHERE advanceDate BETWEEN :startDate AND :endDate ORDER BY advanceDate DESC")
    fun getAdvancesByDateRange(startDate: String, endDate: String): LiveData<List<Advance>>

    @Query("SELECT SUM(amount) FROM advances WHERE workerId = :workerId AND isRecovered = 0")
    fun getTotalUnsettledAdvancesForWorker(workerId: Long): LiveData<Double>

    @Query("""
        UPDATE advances
        SET isRecovered = 1
        WHERE advanceId IN (:advanceIds)
    """)
    suspend fun settleAdvances(advanceIds: List<Long>)
}
