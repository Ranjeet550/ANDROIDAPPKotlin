package com.constructionmanagement.app.data.repository

import androidx.lifecycle.LiveData
import com.constructionmanagement.app.data.db.dao.SiteDao
import com.constructionmanagement.app.data.model.Site
import com.constructionmanagement.app.data.model.SiteStatus

class SiteRepository(private val siteDao: SiteDao) {

    val allSites: LiveData<List<Site>> = siteDao.getAllSites()

    fun getSiteById(siteId: Long): LiveData<Site> {
        return siteDao.getSiteById(siteId)
    }

    fun getSitesByStatus(status: SiteStatus): LiveData<List<Site>> {
        return siteDao.getSitesByStatus(status)
    }

    fun searchSites(query: String): LiveData<List<Site>> {
        return siteDao.searchSites(query)
    }

    fun getSitesByStartDateRange(startDate: String, endDate: String): LiveData<List<Site>> {
        return siteDao.getSitesByStartDateRange(startDate, endDate)
    }

    fun getWorkerCountForSite(siteId: Long): LiveData<Int> {
        return siteDao.getWorkerCountForSite(siteId)
    }

    suspend fun insertSite(site: Site): Long {
        return siteDao.insertSite(site)
    }

    suspend fun updateSite(site: Site) {
        siteDao.updateSite(site)
    }

    suspend fun deleteSite(site: Site) {
        siteDao.deleteSite(site)
    }
}
