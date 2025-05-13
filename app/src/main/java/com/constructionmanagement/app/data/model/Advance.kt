package com.constructionmanagement.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "advances",
    foreignKeys = [
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["workerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workerId")]
)
data class Advance(
    @PrimaryKey(autoGenerate = true)
    val advanceId: Long = 0,
    val workerId: Long,
    val amount: Double,
    val advanceDate: String,
    val reason: String,
    val notes: String? = null,
    val paymentMode: PaymentMode,
    val referenceNumber: String? = null,
    val isRecovered: Boolean = false
)
