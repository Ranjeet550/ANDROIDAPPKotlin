package com.constructionmanagement.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance",
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
data class Attendance(
    @PrimaryKey(autoGenerate = true)
    val attendanceId: Long = 0,
    val workerId: Long,
    val siteId: Long,
    val date: String,
    val status: AttendanceStatus,
    val hoursWorked: Double? = null, // For hourly workers
    val notes: String?
)

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    HALF_DAY,
    LEAVE
}
