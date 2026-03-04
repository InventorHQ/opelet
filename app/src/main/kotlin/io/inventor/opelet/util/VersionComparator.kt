package io.inventor.opelet.util

/**
 * Compares version strings. Handles common tag formats:
 * - v1.2.3 / 1.2.3
 * - v1.2.3-beta.1
 * - Purely numeric segments compared numerically; others lexicographically.
 *
 * Returns negative if a < b, zero if equal, positive if a > b.
 * Pre-release suffixes (anything after a hyphen) sort before the clean version.
 */
object VersionComparator : Comparator<String> {

    override fun compare(a: String, b: String): Int {
        val (aParts, aSuffix) = split(normalize(a))
        val (bParts, bSuffix) = split(normalize(b))

        // Compare numeric segments
        val maxLen = maxOf(aParts.size, bParts.size)
        for (i in 0 until maxLen) {
            val aSegment = aParts.getOrNull(i) ?: "0"
            val bSegment = bParts.getOrNull(i) ?: "0"

            val aNum = aSegment.toLongOrNull()
            val bNum = bSegment.toLongOrNull()

            val cmp = if (aNum != null && bNum != null) {
                aNum.compareTo(bNum)
            } else {
                aSegment.compareTo(bSegment)
            }
            if (cmp != 0) return cmp
        }

        // Same base version: no suffix > has suffix (1.0.0 > 1.0.0-beta)
        return when {
            aSuffix == null && bSuffix == null -> 0
            aSuffix == null -> 1
            bSuffix == null -> -1
            else -> aSuffix.compareTo(bSuffix)
        }
    }

    private fun normalize(version: String): String =
        version.trimStart('v', 'V')

    private fun split(version: String): Pair<List<String>, String?> {
        val hyphenIdx = version.indexOf('-')
        return if (hyphenIdx >= 0) {
            val base = version.substring(0, hyphenIdx)
            val suffix = version.substring(hyphenIdx + 1)
            base.split('.') to suffix
        } else {
            version.split('.') to null
        }
    }
}
