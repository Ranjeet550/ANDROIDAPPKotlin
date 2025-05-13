package com.constructionmanagement.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "worker_site_assignments",
    foreignKeys = [
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["workerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Site::class,
            parentColumns = ["siteId"],
            childColumns = ["siteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("workerId"),
        Index("siteId")
    ]
)
data class WorkerSiteAssignment(
    @PrimaryKey(autoGenerate = true)
    val assignmentId: Long = 0,
    val workerId: Long,
    val siteId: Long,
    val assignmentDate: String,
    val endDate: String? = null,
    val isActive: Boolean = true
)
