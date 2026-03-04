package io.inventor.opelet.model

/**
 * A GitHub release with its assets, parsed from the API response.
 * This is a domain model — not persisted directly.
 */
data class GitHubRelease(
    val id: Long,
    val tagName: String,
    val name: String,
    val body: String,
    val prerelease: Boolean,
    val publishedAt: String,
    val assets: List<GitHubAsset>,
)

data class GitHubAsset(
    val id: Long,
    val name: String,
    val size: Long,
    val downloadUrl: String,
)

/**
 * Represents the update status for a tracked app.
 */
enum class UpdateStatus {
    UP_TO_DATE,
    UPDATE_AVAILABLE,
    PINNED,
    UNKNOWN,
}
