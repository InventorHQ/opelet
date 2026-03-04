package io.inventor.opelet.util

/**
 * Parses a GitHub repo URL or "owner/repo" shorthand into owner and repo components.
 *
 * Accepted formats:
 * - https://github.com/owner/repo
 * - https://github.com/owner/repo/anything/else
 * - github.com/owner/repo
 * - owner/repo
 */
object RepoParser {

    private val GITHUB_URL_REGEX =
        Regex("""(?:https?://)?(?:www\.)?github\.com/([A-Za-z0-9\-_.]+)/([A-Za-z0-9\-_.]+)""")

    private val SHORTHAND_REGEX =
        Regex("""^([A-Za-z0-9\-_.]+)/([A-Za-z0-9\-_.]+)$""")

    data class Repo(val owner: String, val repo: String) {
        val fullName: String get() = "$owner/$repo"
    }

    fun parse(input: String): Repo? {
        val trimmed = input.trim().removeSuffix("/").removeSuffix(".git")

        GITHUB_URL_REGEX.find(trimmed)?.let { match ->
            return Repo(match.groupValues[1], match.groupValues[2])
        }

        SHORTHAND_REGEX.find(trimmed)?.let { match ->
            return Repo(match.groupValues[1], match.groupValues[2])
        }

        return null
    }
}
