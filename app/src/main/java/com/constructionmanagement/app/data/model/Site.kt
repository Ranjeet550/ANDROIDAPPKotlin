package com.constructionmanagement.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sites")
data class Site(
    @PrimaryKey(autoGenerate = true)
    val siteId: Long = 0,
    val name: String,
    val address: String,
    val clientName: String,
    val clientContact: String,
    val startDate: String,
    val expectedEndDate: String?,
    val status: SiteStatus,
    val notes: String?
)

enum class SiteStatus {
    ACTIVE,
    COMPLETED,
    ON_HOLD
}
