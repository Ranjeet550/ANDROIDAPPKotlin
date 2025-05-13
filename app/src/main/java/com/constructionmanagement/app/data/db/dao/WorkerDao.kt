package com.constructionmanagement.app.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.constructionmanagement.app.data.model.Worker

@Dao
interface WorkerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: Worker): Long

    @Update
    suspend fun updateWorker(worker: Worker)

    @Delete
    suspend fun deleteWorker(worker: Worker)

    @Query("SELECT * FROM workers WHERE id = :workerId")
    fun getWorkerById(workerId: Long): LiveData<Worker>

    @Query("SELECT * FROM workers ORDER BY name ASC")
    fun getAllWorkers(): LiveData<List<Worker>>

    @Query("SELECT * FROM workers WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveWorkers(): LiveData<List<Worker>>

    @Query("SELECT * FROM workers WHERE name LIKE '%' || :searchQuery || '%' OR phoneNumber LIKE '%' || :searchQuery || '%'")
    fun searchWorkers(searchQuery: String): LiveData<List<Worker>>

    @Query("""
        SELECT w.* FROM workers w
        INNER JOIN worker_site_assignments wsa ON w.id = wsa.workerId
        WHERE wsa.siteId = :siteId AND wsa.isActive = 1
    """)
    fun getWorkersBySite(siteId: Long): LiveData<List<Worker>>
}
