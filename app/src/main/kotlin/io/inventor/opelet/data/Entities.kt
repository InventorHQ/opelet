package io.inventor.opelet.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A tracked GitHub repository.
 */
@Entity(tableName = "tracked_apps")
data class TrackedApp(
    @PrimaryKey
    val repoFullName: String, // "owner/repo"
    val owner: String,
    val repo: String,
    val installedVersion: String? = null,
    val latestVersion: String? = null,
    val latestStableVersion: String? = null,
    val pinnedVersion: String? = null,
    val preferredAssetPattern: String? = null, // remembered APK choice
    val description: String? = null,
    val lastChecked: Long = 0L,
    val addedAt: Long = System.currentTimeMillis(),
    val isSelf: Boolean = false, // true for opelet's own entry
)
