package com.constructionmanagement.app.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.constructionmanagement.app.data.model.Attendance
import com.constructionmanagement.app.data.model.AttendanceStatus

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long

    @Update
    suspend fun updateAttendance(attendance: Attendance)

    @Delete
    suspend fun deleteAttendance(attendance: Attendance)

    @Query("SELECT * FROM attendance WHERE attendanceId = :attendanceId")
    fun getAttendanceById(attendanceId: Long): LiveData<Attendance>

    @Query("SELECT * FROM attendance ORDER BY date DESC")
    fun getAllAttendance(): LiveData<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE workerId = :workerId ORDER BY date DESC")
    fun getAttendanceForWorker(workerId: Long): LiveData<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE siteId = :siteId ORDER BY date DESC")
    fun getAttendanceForSite(siteId: Long): LiveData<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceForDate(date: String): LiveData<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE workerId = :workerId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getAttendanceForWorkerInDateRange(workerId: Long, startDate: String, endDate: String): LiveData<List<Attendance>>

    @Query("SELECT COUNT(*) FROM attendance WHERE workerId = :workerId AND status = :status AND date BETWEEN :startDate AND :endDate")
    fun getAttendanceCountByStatus(workerId: Long, status: AttendanceStatus, startDate: String, endDate: String): LiveData<Int>
}
