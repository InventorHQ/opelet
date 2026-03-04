package io.inventor.opelet.data

import io.inventor.opelet.model.GitHubRelease
import io.inventor.opelet.model.UpdateStatus
import io.inventor.opelet.network.GitHubApi
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val dao: TrackedAppDao,
    private val api: GitHubApi,
) {
    fun observeApps(): Flow<List<TrackedApp>> = dao.observeAll()

    suspend fun getApp(repoFullName: String): TrackedApp? =
        dao.getByRepoFullName(repoFullName)

    suspend fun addApp(owner: String, repo: String): Result<TrackedApp> {
        val fullName = "$owner/$repo"
        val existing = dao.getByRepoFullName(fullName)
        if (existing != null) return Result.success(existing)

        // Validate that the repo has releases
        val releases = api.getReleases(owner, repo).getOrElse { return Result.failure(it) }
        if (releases.isEmpty()) {
            return Result.failure(IllegalStateException("No releases found for $fullName"))
        }

        val latestStable = releases.firstOrNull { !it.prerelease }
        val latest = releases.first()

        val app = TrackedApp(
            repoFullName = fullName,
            owner = owner,
            repo = repo,
            latestVersion = latest.tagName,
            latestStableVersion = latestStable?.tagName,
            lastChecked = System.currentTimeMillis(),
        )
        dao.upsert(app)
        return Result.success(app)
    }

    suspend fun removeApp(app: TrackedApp) = dao.delete(app)

    suspend fun refreshApp(app: TrackedApp): Result<TrackedApp> {
        val releases = api.getReleases(app.owner, app.repo).getOrElse {
            return Result.failure(it)
        }
        if (releases.isEmpty()) return Result.success(app)

        val latestStable = releases.firstOrNull { !it.prerelease }
        val latest = releases.first()

        val updated = app.copy(
            latestVersion = latest.tagName,
            latestStableVersion = latestStable?.tagName,
            lastChecked = System.currentTimeMillis(),
        )
        dao.update(updated)
        return Result.success(updated)
    }

    suspend fun refreshAll(): List<Result<TrackedApp>> {
        return dao.getAll().map { refreshApp(it) }
    }

    suspend fun getReleases(owner: String, repo: String): Result<List<GitHubRelease>> =
        api.getReleases(owner, repo)

    suspend fun setInstalledVersion(repoFullName: String, version: String) {
        val app = dao.getByRepoFullName(repoFullName) ?: return
        dao.update(app.copy(installedVersion = version))
    }

    suspend fun pinVersion(repoFullName: String, version: String?) {
        val app = dao.getByRepoFullName(repoFullName) ?: return
        dao.update(app.copy(pinnedVersion = version))
    }

    suspend fun setPreferredAsset(repoFullName: String, pattern: String) {
        val app = dao.getByRepoFullName(repoFullName) ?: return
        dao.update(app.copy(preferredAssetPattern = pattern))
    }

    suspend fun appCount(): Int = dao.count()

    suspend fun ensureSelfTracked() {
        val self = dao.getByRepoFullName(SELF_REPO)
        if (self == null) {
            val app = TrackedApp(
                repoFullName = SELF_REPO,
                owner = SELF_OWNER,
                repo = SELF_REPO_NAME,
                isSelf = true,
            )
            dao.upsert(app)
            // Try to fetch latest info, but don't fail if network is unavailable
            try { refreshApp(app) } catch (_: Exception) {}
        }
    }

    companion object {
        const val SELF_OWNER = "InventorHQ"
        const val SELF_REPO_NAME = "opelet"
        const val SELF_REPO = "$SELF_OWNER/$SELF_REPO_NAME"
    }
}

fun TrackedApp.updateStatus(): UpdateStatus {
    if (pinnedVersion != null) return UpdateStatus.PINNED
    val installed = installedVersion ?: return UpdateStatus.UNKNOWN
    val target = latestStableVersion ?: latestVersion ?: return UpdateStatus.UNKNOWN
    return if (installed == target) UpdateStatus.UP_TO_DATE else UpdateStatus.UPDATE_AVAILABLE
}
