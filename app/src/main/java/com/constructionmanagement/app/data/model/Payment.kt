package com.constructionmanagement.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
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
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("workerId"),
        Index("siteId")
    ]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val paymentId: Long = 0,
    val workerId: Long,
    val siteId: Long,
    val paymentDate: String,
    val amount: Double,
    val description: String,
    val paymentMode: PaymentMode,
    val referenceNumber: String? = null,
    val forMonth: Int = 0, // 1-12
    val forYear: Int = 0,
    val notes: String? = null
)

enum class PaymentMode {
    CASH,
    BANK_TRANSFER,
    OTHER
}
