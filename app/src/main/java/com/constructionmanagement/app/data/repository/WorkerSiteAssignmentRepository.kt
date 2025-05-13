package com.constructionmanagement.app.data.repository

import androidx.lifecycle.LiveData
import com.constructionmanagement.app.data.db.dao.WorkerSiteAssignmentDao
import com.constructionmanagement.app.data.model.WorkerSiteAssignment
import java.util.Date

class WorkerSiteAssignmentRepository(private val assignmentDao: WorkerSiteAssignmentDao) {

    fun getAssignmentById(assignmentId: Long): LiveData<WorkerSiteAssignment> {
        return assignmentDao.getAssignmentById(assignmentId)
    }

    fun getAssignmentsForWorker(workerId: Long): LiveData<List<WorkerSiteAssignment>> {
        return assignmentDao.getAssignmentsForWorker(workerId)
    }

    fun getAssignmentsForSite(siteId: Long): LiveData<List<WorkerSiteAssignment>> {
        return assignmentDao.getAssignmentsForSite(siteId)
    }

    fun getActiveAssignmentForWorker(workerId: Long): LiveData<WorkerSiteAssignment?> {
        return assignmentDao.getActiveAssignmentForWorker(workerId)
    }

    suspend fun insertAssignment(assignment: WorkerSiteAssignment): Long {
        return assignmentDao.insertAssignment(assignment)
    }

    suspend fun updateAssignment(assignment: WorkerSiteAssignment) {
        assignmentDao.updateAssignment(assignment)
    }

    suspend fun deleteAssignment(assignment: WorkerSiteAssignment) {
        assignmentDao.deleteAssignment(assignment)
    }

    suspend fun deactivateCurrentAssignments(workerId: Long, endDate: String) {
        assignmentDao.deactivateCurrentAssignments(workerId, endDate)
    }

    /**
     * Assigns a worker to a new site, deactivating any current assignments
     */
    suspend fun assignWorkerToSite(workerId: Long, siteId: Long, assignmentDate: String): Long {
        // First deactivate any current assignments
        deactivateCurrentAssignments(workerId, assignmentDate)

        // Then create a new assignment
        val newAssignment = WorkerSiteAssignment(
            workerId = workerId,
            siteId = siteId,
            assignmentDate = assignmentDate,
            isActive = true
        )

        return insertAssignment(newAssignment)
    }

    /**
     * Inserts multiple worker-site assignments
     */
    suspend fun insertWorkerSiteAssignments(assignments: List<WorkerSiteAssignment>) {
        assignments.forEach { assignment ->
            // Deactivate any current assignments for this worker
            deactivateCurrentAssignments(assignment.workerId, assignment.assignmentDate)

            // Create a new assignment with isActive set to true
            val newAssignment = assignment.copy(isActive = true)
            insertAssignment(newAssignment)
        }
    }
}
