package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerRecordDao {
    @Query("SELECT * FROM tracker_records ORDER BY id DESC")
    fun getAllRecords(): Flow<List<TrackerRecord>>

    @Query("SELECT * FROM tracker_records WHERE userId = :userId ORDER BY id DESC")
    fun getRecordsForUser(userId: String): Flow<List<TrackerRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: TrackerRecord): Long

    @Update
    suspend fun updateRecord(record: TrackerRecord)

    @Delete
    suspend fun deleteRecord(record: TrackerRecord)

    @Query("DELETE FROM tracker_records WHERE id = :id")
    suspend fun deleteRecordById(id: Int)

    @Query("DELETE FROM tracker_records")
    suspend fun deleteAllRecords()
}
