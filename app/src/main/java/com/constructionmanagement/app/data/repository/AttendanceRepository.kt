package com.constructionmanagement.app.data.repository

import androidx.lifecycle.LiveData
import com.constructionmanagement.app.data.db.dao.AttendanceDao
import com.constructionmanagement.app.data.model.Attendance
import com.constructionmanagement.app.data.model.AttendanceStatus

class AttendanceRepository(private val attendanceDao: AttendanceDao) {

    val allAttendance: LiveData<List<Attendance>> = attendanceDao.getAllAttendance()

    fun getAttendanceById(attendanceId: Long): LiveData<Attendance> {
        return attendanceDao.getAttendanceById(attendanceId)
    }

    fun getAttendanceForWorker(workerId: Long): LiveData<List<Attendance>> {
        return attendanceDao.getAttendanceForWorker(workerId)
    }

    fun getAttendanceForSite(siteId: Long): LiveData<List<Attendance>> {
        return attendanceDao.getAttendanceForSite(siteId)
    }

    fun getAttendanceForDate(date: String): LiveData<List<Attendance>> {
        return attendanceDao.getAttendanceForDate(date)
    }

    fun getAttendanceForWorkerInDateRange(workerId: Long, startDate: String, endDate: String): LiveData<List<Attendance>> {
        return attendanceDao.getAttendanceForWorkerInDateRange(workerId, startDate, endDate)
    }

    fun getAttendanceCountByStatus(workerId: Long, status: AttendanceStatus, startDate: String, endDate: String): LiveData<Int> {
        return attendanceDao.getAttendanceCountByStatus(workerId, status, startDate, endDate)
    }

    suspend fun insertAttendance(attendance: Attendance): Long {
        return attendanceDao.insertAttendance(attendance)
    }

    suspend fun updateAttendance(attendance: Attendance) {
        attendanceDao.updateAttendance(attendance)
    }

    suspend fun deleteAttendance(attendance: Attendance) {
        attendanceDao.deleteAttendance(attendance)
    }
}
