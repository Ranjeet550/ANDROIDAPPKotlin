package com.constructionmanagement.app

import android.app.Application
import android.util.Log
import com.constructionmanagement.app.data.db.AppDatabase
import com.constructionmanagement.app.data.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class ConstructionApp : Application() {
    // No need to cancel this scope as it'll be torn down with the process
    private val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repositories are only created when they're needed
    val database by lazy { AppDatabase.getDatabase(this) }

    // Repositories
    val workerRepository by lazy { WorkerRepository(database.workerDao()) }
    val siteRepository by lazy { SiteRepository(database.siteDao()) }
    val workerSiteAssignmentRepository by lazy { WorkerSiteAssignmentRepository(database.workerSiteAssignmentDao()) }
    val paymentRepository by lazy { PaymentRepository(database.paymentDao()) }
    val advanceRepository by lazy { AdvanceRepository(database.advanceDao()) }
    val attendanceRepository by lazy { AttendanceRepository(database.attendanceDao()) }

    override fun onCreate() {
        super.onCreate()

        try {
            Log.d("ConstructionApp", "Application initialized successfully")
        } catch (e: Exception) {
            Log.e("ConstructionApp", "Error initializing application", e)
        }
    }
}
