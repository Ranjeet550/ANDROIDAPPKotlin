package com.constructionmanagement.app.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.constructionmanagement.app.data.model.Site
import com.constructionmanagement.app.data.model.SiteStatus

@Dao
interface SiteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSite(site: Site): Long

    @Update
    suspend fun updateSite(site: Site)

    @Delete
    suspend fun deleteSite(site: Site)

    @Query("SELECT * FROM sites WHERE siteId = :siteId")
    fun getSiteById(siteId: Long): LiveData<Site>

    @Query("SELECT * FROM sites ORDER BY name ASC")
    fun getAllSites(): LiveData<List<Site>>

    @Query("SELECT * FROM sites WHERE status = :status ORDER BY name ASC")
    fun getSitesByStatus(status: SiteStatus): LiveData<List<Site>>

    @Query("SELECT * FROM sites WHERE name LIKE '%' || :searchQuery || '%' OR address LIKE '%' || :searchQuery || '%' OR clientName LIKE '%' || :searchQuery || '%'")
    fun searchSites(searchQuery: String): LiveData<List<Site>>

    @Query("SELECT * FROM sites WHERE startDate BETWEEN :startDate AND :endDate")
    fun getSitesByStartDateRange(startDate: String, endDate: String): LiveData<List<Site>>

    @Query("""
        SELECT COUNT(wsa.workerId)
        FROM worker_site_assignments wsa
        WHERE wsa.siteId = :siteId AND wsa.isActive = 1
    """)
    fun getWorkerCountForSite(siteId: Long): LiveData<Int>
}
