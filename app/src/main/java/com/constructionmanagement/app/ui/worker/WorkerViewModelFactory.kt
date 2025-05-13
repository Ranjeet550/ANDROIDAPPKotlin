package com.constructionmanagement.app.ui.worker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.constructionmanagement.app.data.repository.WorkerRepository
import com.constructionmanagement.app.data.repository.WorkerSiteAssignmentRepository

class WorkerViewModelFactory(
    private val workerRepository: WorkerRepository,
    private val assignmentRepository: WorkerSiteAssignmentRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkerViewModel::class.java)) {
            return WorkerViewModel(workerRepository, assignmentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
