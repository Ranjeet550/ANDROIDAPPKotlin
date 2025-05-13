package com.constructionmanagement.app.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.constructionmanagement.app.data.model.WorkerSiteAssignment

@Dao
interface WorkerSiteAssignmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: WorkerSiteAssignment): Long

    @Update
    suspend fun updateAssignment(assignment: WorkerSiteAssignment)

    @Delete
    suspend fun deleteAssignment(assignment: WorkerSiteAssignment)

    @Query("SELECT * FROM worker_site_assignments WHERE assignmentId = :assignmentId")
    fun getAssignmentById(assignmentId: Long): LiveData<WorkerSiteAssignment>

    @Query("SELECT * FROM worker_site_assignments WHERE workerId = :workerId ORDER BY assignmentDate DESC")
    fun getAssignmentsForWorker(workerId: Long): LiveData<List<WorkerSiteAssignment>>

    @Query("SELECT * FROM worker_site_assignments WHERE siteId = :siteId ORDER BY assignmentDate DESC")
    fun getAssignmentsForSite(siteId: Long): LiveData<List<WorkerSiteAssignment>>

    @Query("SELECT * FROM worker_site_assignments WHERE workerId = :workerId AND isActive = 1 LIMIT 1")
    fun getActiveAssignmentForWorker(workerId: Long): LiveData<WorkerSiteAssignment?>

    @Query("""
        UPDATE worker_site_assignments
        SET isActive = 0, endDate = :endDate
        WHERE workerId = :workerId AND isActive = 1
    """)
    suspend fun deactivateCurrentAssignments(workerId: Long, endDate: String)
}
