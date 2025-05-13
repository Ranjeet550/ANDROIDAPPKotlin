package com.constructionmanagement.app.ui.site

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.constructionmanagement.app.data.repository.SiteRepository

class SiteViewModelFactory(private val siteRepository: SiteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SiteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SiteViewModel(siteRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
