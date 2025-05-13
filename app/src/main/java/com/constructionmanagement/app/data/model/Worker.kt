package com.constructionmanagement.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val address: String,
    val role: String,
    val aadharNumber: String,
    val joinDate: String,
    val isActive: Boolean = true,
    val profileImagePath: String? = null
)

enum class WageType {
    DAILY,
    MONTHLY
}
