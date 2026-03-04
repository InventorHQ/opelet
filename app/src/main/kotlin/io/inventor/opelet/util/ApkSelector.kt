package io.inventor.opelet.util

import io.inventor.opelet.model.GitHubAsset

/**
 * Selects the best APK asset from a release's asset list.
 *
 * Strategy:
 * 1. Filter to .apk files only.
 * 2. If a preferred pattern is set (remembered choice), use it.
 * 3. Prefer arm64-v8a / arm64 in filename.
 * 4. Fall back to universal/no-arch indicators.
 * 5. Fall back to the largest APK (heuristic: universal builds are biggest).
 * 6. If only one APK, use it.
 * 7. If ambiguous, return null — caller should show a picker.
 */
object ApkSelector {

    private val ARM64_PATTERNS = listOf("arm64-v8a", "arm64", "aarch64")
    private val UNIVERSAL_PATTERNS = listOf("universal", "all", "fat")

    fun selectApk(
        assets: List<GitHubAsset>,
        preferredPattern: String? = null,
    ): ApkSelection {
        val apks = assets.filter { it.name.endsWith(".apk", ignoreCase = true) }

        if (apks.isEmpty()) return ApkSelection.NoApk
        if (apks.size == 1) return ApkSelection.Selected(apks.first())

        // Preferred pattern match (exact substring in filename)
        if (preferredPattern != null) {
            val match = apks.firstOrNull { it.name.contains(preferredPattern, ignoreCase = true) }
            if (match != null) return ApkSelection.Selected(match)
        }

        // arm64 match
        val arm64 = apks.filter { asset ->
            ARM64_PATTERNS.any { asset.name.contains(it, ignoreCase = true) }
        }
        if (arm64.size == 1) return ApkSelection.Selected(arm64.first())

        // Universal match
        val universal = apks.filter { asset ->
            UNIVERSAL_PATTERNS.any { asset.name.contains(it, ignoreCase = true) }
        }
        if (universal.size == 1) return ApkSelection.Selected(universal.first())

        // No arch-specific indicators at all — might all be universal, pick largest
        val hasArchIndicator = apks.any { asset ->
            val name = asset.name.lowercase()
            ARM64_PATTERNS.any { name.contains(it) } ||
                listOf("armeabi", "x86", "x86_64", "mips").any { name.contains(it) }
        }
        if (!hasArchIndicator) {
            return ApkSelection.Selected(apks.maxBy { it.size })
        }

        // Ambiguous — need user to pick
        return ApkSelection.Ambiguous(apks)
    }
}

sealed class ApkSelection {
    data object NoApk : ApkSelection()
    data class Selected(val asset: GitHubAsset) : ApkSelection()
    data class Ambiguous(val candidates: List<GitHubAsset>) : ApkSelection()
}
