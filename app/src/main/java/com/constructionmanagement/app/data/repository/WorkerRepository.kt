package com.constructionmanagement.app.data.repository

import androidx.lifecycle.LiveData
import com.constructionmanagement.app.data.db.dao.WorkerDao
import com.constructionmanagement.app.data.model.Worker

class WorkerRepository(private val workerDao: WorkerDao) {

    val allWorkers: LiveData<List<Worker>> = workerDao.getAllWorkers()
    val activeWorkers: LiveData<List<Worker>> = workerDao.getActiveWorkers()

    fun getWorkerById(workerId: Long): LiveData<Worker> {
        return workerDao.getWorkerById(workerId)
    }

    fun searchWorkers(query: String): LiveData<List<Worker>> {
        return workerDao.searchWorkers(query)
    }

    fun getWorkersBySite(siteId: Long): LiveData<List<Worker>> {
        return workerDao.getWorkersBySite(siteId)
    }

    suspend fun insertWorker(worker: Worker): Long {
        return workerDao.insertWorker(worker)
    }

    suspend fun updateWorker(worker: Worker) {
        workerDao.updateWorker(worker)
    }

    suspend fun deleteWorker(worker: Worker) {
        workerDao.deleteWorker(worker)
    }
}
