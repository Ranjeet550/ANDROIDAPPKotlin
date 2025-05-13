package com.constructionmanagement.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.constructionmanagement.app.data.db.dao.*
import com.constructionmanagement.app.data.model.*
import com.constructionmanagement.app.util.DateConverter

@Database(
    entities = [
        Worker::class,
        Site::class,
        WorkerSiteAssignment::class,
        Payment::class,
        Advance::class,
        Attendance::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workerDao(): WorkerDao
    abstract fun siteDao(): SiteDao
    abstract fun workerSiteAssignmentDao(): WorkerSiteAssignmentDao
    abstract fun paymentDao(): PaymentDao
    abstract fun advanceDao(): AdvanceDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "construction_management_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
