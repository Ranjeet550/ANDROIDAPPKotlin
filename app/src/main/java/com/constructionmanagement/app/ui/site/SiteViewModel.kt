package com.constructionmanagement.app.ui.site

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constructionmanagement.app.data.model.Site
import com.constructionmanagement.app.data.model.SiteStatus
import com.constructionmanagement.app.data.repository.SiteRepository
import kotlinx.coroutines.launch

class SiteViewModel(private val siteRepository: SiteRepository) : ViewModel() {

    val allSites: LiveData<List<Site>> = siteRepository.allSites
    val activeSites: LiveData<List<Site>> = siteRepository.getSitesByStatus(SiteStatus.ACTIVE)

    private val _navigateToSiteDetails = MutableLiveData<Long?>()
    val navigateToSiteDetails: LiveData<Long?> = _navigateToSiteDetails

    private val _searchResults = MutableLiveData<List<Site>>()
    val searchResults: LiveData<List<Site>> = _searchResults

    fun getSiteById(siteId: Long): LiveData<Site> {
        return siteRepository.getSiteById(siteId)
    }

    fun searchSites(query: String) {
        if (query.isNotBlank()) {
            siteRepository.searchSites(query).observeForever { sites ->
                _searchResults.value = sites
            }
        } else {
            _searchResults.value = emptyList()
        }
    }

    fun insertSite(site: Site, callback: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val siteId = siteRepository.insertSite(site)
            callback?.invoke(siteId)
        }
    }

    fun updateSite(site: Site) {
        viewModelScope.launch {
            siteRepository.updateSite(site)
        }
    }

    fun deleteSite(site: Site) {
        viewModelScope.launch {
            siteRepository.deleteSite(site)
        }
    }

    fun onSiteClicked(siteId: Long) {
        _navigateToSiteDetails.value = siteId
    }

    fun onSiteDetailsNavigated() {
        _navigateToSiteDetails.value = null
    }
}
