package com.constructionmanagement.app.ui.payment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constructionmanagement.app.data.model.Advance
import com.constructionmanagement.app.data.model.Worker
import com.constructionmanagement.app.data.repository.AdvanceRepository
import com.constructionmanagement.app.data.repository.WorkerRepository
import kotlinx.coroutines.launch

class AdvanceViewModel(
    private val advanceRepository: AdvanceRepository,
    private val workerRepository: WorkerRepository
) : ViewModel() {

    val allAdvances: LiveData<List<Advance>> = advanceRepository.allAdvances
    val allWorkers: LiveData<List<Worker>> = workerRepository.allWorkers

    private val _navigateToAdvanceDetails = MutableLiveData<Long?>()
    val navigateToAdvanceDetails: LiveData<Long?> = _navigateToAdvanceDetails

    fun getAdvancesByWorker(workerId: Long): LiveData<List<Advance>> {
        return advanceRepository.getAdvancesForWorker(workerId)
    }

    fun getAdvancesByDateRange(startDate: String, endDate: String): LiveData<List<Advance>> {
        return advanceRepository.getAdvancesByDateRange(startDate, endDate)
    }

    fun getUnrecoveredAdvances(workerId: Long): LiveData<List<Advance>> {
        return advanceRepository.getUnsettledAdvancesForWorker(workerId)
    }

    fun insertAdvance(advance: Advance) {
        viewModelScope.launch {
            advanceRepository.insertAdvance(advance)
        }
    }

    // Get advance by ID
    fun getAdvanceById(advanceId: Long): LiveData<Advance> {
        return advanceRepository.getAdvanceById(advanceId)
    }

    fun getWorkerById(workerId: Long): LiveData<Worker> {
        return workerRepository.getWorkerById(workerId)
    }

    fun updateAdvance(advance: Advance) {
        viewModelScope.launch {
            advanceRepository.updateAdvance(advance)
        }
    }

    fun deleteAdvance(advance: Advance) {
        viewModelScope.launch {
            advanceRepository.deleteAdvance(advance)
        }
    }

    fun markAdvanceAsCompleted(advance: Advance) {
        viewModelScope.launch {
            val updatedAdvance = advance.copy(isRecovered = true)
            advanceRepository.updateAdvance(updatedAdvance)
        }
    }

    fun onAdvanceClicked(advanceId: Long) {
        _navigateToAdvanceDetails.value = advanceId
    }

    fun onAdvanceDetailsNavigated() {
        _navigateToAdvanceDetails.value = null
    }
}
