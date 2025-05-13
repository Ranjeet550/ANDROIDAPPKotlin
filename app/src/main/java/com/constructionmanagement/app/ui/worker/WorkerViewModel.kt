package com.constructionmanagement.app.ui.worker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constructionmanagement.app.data.model.Worker
import com.constructionmanagement.app.data.model.WorkerSiteAssignment
import com.constructionmanagement.app.data.repository.WorkerRepository
import com.constructionmanagement.app.data.repository.WorkerSiteAssignmentRepository
import kotlinx.coroutines.launch

class WorkerViewModel(
    private val workerRepository: WorkerRepository,
    private val assignmentRepository: WorkerSiteAssignmentRepository
) : ViewModel() {

    private val _allWorkers: LiveData<List<Worker>> = workerRepository.allWorkers
    val allWorkers: LiveData<List<Worker>> = _allWorkers
    val activeWorkers: LiveData<List<Worker>> = workerRepository.activeWorkers

    private val _searchResults = MutableLiveData<List<Worker>>()
    val searchResults: LiveData<List<Worker>> = _searchResults

    private val _navigateToWorkerDetails = MutableLiveData<Long?>()
    val navigateToWorkerDetails: LiveData<Long?> = _navigateToWorkerDetails

    fun getWorkerById(workerId: Long): LiveData<Worker> {
        return workerRepository.getWorkerById(workerId)
    }



    fun searchWorkers(query: String) {
        if (query.isNotBlank()) {
            workerRepository.searchWorkers(query).observeForever { workers ->
                _searchResults.value = workers
            }
        } else {
            _searchResults.value = emptyList()
        }
    }



    fun getWorkersBySite(siteId: Long): LiveData<List<Worker>> {
        return workerRepository.getWorkersBySite(siteId)
    }

    fun insertWorker(worker: Worker) {
        viewModelScope.launch {
            workerRepository.insertWorker(worker)
        }
    }

    fun updateWorker(worker: Worker) {
        viewModelScope.launch {
            workerRepository.updateWorker(worker)
        }
    }

    fun deleteWorker(worker: Worker) {
        viewModelScope.launch {
            workerRepository.deleteWorker(worker)
        }
    }

    fun assignWorkerToSite(workerId: Long, siteId: Long, assignmentDate: String) {
        viewModelScope.launch {
            assignmentRepository.assignWorkerToSite(workerId, siteId, assignmentDate)
        }
    }

    fun insertWorkerSiteAssignments(assignments: List<WorkerSiteAssignment>) {
        viewModelScope.launch {
            assignmentRepository.insertWorkerSiteAssignments(assignments)
        }
    }

    fun getActiveAssignmentForWorker(workerId: Long): LiveData<WorkerSiteAssignment?> {
        return assignmentRepository.getActiveAssignmentForWorker(workerId)
    }

    fun updateWorkerSiteAssignment(assignment: WorkerSiteAssignment) {
        viewModelScope.launch {
            assignmentRepository.updateAssignment(assignment)
        }
    }

    fun onWorkerClicked(workerId: Long) {
        _navigateToWorkerDetails.value = workerId
    }

    fun onWorkerDetailsNavigated() {
        _navigateToWorkerDetails.value = null
    }
}
