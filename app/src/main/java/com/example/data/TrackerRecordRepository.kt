package com.example.data

import kotlinx.coroutines.flow.Flow

class TrackerRecordRepository(private val trackerRecordDao: TrackerRecordDao) {
    val allRecords: Flow<List<TrackerRecord>> = trackerRecordDao.getAllRecords()

    fun getRecordsForUser(userId: String): Flow<List<TrackerRecord>> {
        return trackerRecordDao.getRecordsForUser(userId)
    }

    suspend fun insertRecord(record: TrackerRecord): Long {
        return trackerRecordDao.insertRecord(record)
    }

    suspend fun updateRecord(record: TrackerRecord) {
        trackerRecordDao.updateRecord(record)
    }

    suspend fun deleteRecord(record: TrackerRecord) {
        trackerRecordDao.deleteRecord(record)
    }

    suspend fun deleteRecordById(id: Int) {
        trackerRecordDao.deleteRecordById(id)
    }

    suspend fun deleteAllRecords() {
        trackerRecordDao.deleteAllRecords()
    }
}
