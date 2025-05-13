package com.constructionmanagement.app.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.constructionmanagement.app.data.repository.AdvanceRepository
import com.constructionmanagement.app.data.repository.WorkerRepository

class AdvanceViewModelFactory(
    private val advanceRepository: AdvanceRepository,
    private val workerRepository: WorkerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdvanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdvanceViewModel(advanceRepository, workerRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
