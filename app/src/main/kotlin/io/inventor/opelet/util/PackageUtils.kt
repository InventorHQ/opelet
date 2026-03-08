package io.inventor.opelet.util

import android.content.Context
import android.content.pm.PackageManager
import java.io.File

/**
 * Queries the device's PackageManager to find what version of a package is installed.
 * Returns the versionName or null if not installed.
 */
object PackageUtils {

    fun getInstalledVersion(context: Context, packageName: String): String? {
        return try {
            val info = context.packageManager.getPackageInfo(packageName, 0)
            info.versionName
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }

    /**
     * Extracts the package name from a downloaded APK file without installing it.
     */
    fun getPackageNameFromApk(context: Context, apkFile: File): String? {
        val info = context.packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
        return info?.packageName
    }
}
