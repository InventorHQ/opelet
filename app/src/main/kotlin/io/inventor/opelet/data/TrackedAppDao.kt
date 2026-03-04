package io.inventor.opelet.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedAppDao {

    @Query("SELECT * FROM tracked_apps ORDER BY addedAt ASC")
    fun observeAll(): Flow<List<TrackedApp>>

    @Query("SELECT * FROM tracked_apps ORDER BY addedAt ASC")
    suspend fun getAll(): List<TrackedApp>

    @Query("SELECT * FROM tracked_apps WHERE repoFullName = :repoFullName")
    suspend fun getByRepoFullName(repoFullName: String): TrackedApp?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(app: TrackedApp)

    @Update
    suspend fun update(app: TrackedApp)

    @Delete
    suspend fun delete(app: TrackedApp)

    @Query("SELECT COUNT(*) FROM tracked_apps")
    suspend fun count(): Int
}
