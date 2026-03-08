package io.inventor.opelet.ui.detail

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.inventor.opelet.data.AppRepository
import io.inventor.opelet.data.OpeletDatabase
import io.inventor.opelet.data.TrackedApp
import io.inventor.opelet.model.GitHubAsset
import io.inventor.opelet.model.GitHubRelease
import io.inventor.opelet.network.GitHubApi
import io.inventor.opelet.util.ApkSelection
import io.inventor.opelet.util.ApkSelector
import io.inventor.opelet.util.PackageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class DetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    private val repoFullName: String = savedStateHandle["repoFullName"] ?: ""
    private val db = OpeletDatabase.get(application)
    private val api = GitHubApi()
    private val repo = AppRepository(db.trackedAppDao(), api)

    private val _app = MutableStateFlow<TrackedApp?>(null)
    val app: StateFlow<TrackedApp?> = _app.asStateFlow()

    private val _releases = MutableStateFlow<List<GitHubRelease>>(emptyList())
    val releases: StateFlow<List<GitHubRelease>> = _releases.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _downloadProgress = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadProgress: StateFlow<DownloadState> = _downloadProgress.asStateFlow()

    private val _apkPicker = MutableStateFlow<List<GitHubAsset>?>(null)
    val apkPicker: StateFlow<List<GitHubAsset>?> = _apkPicker.asStateFlow()

    // Tracks what version we attempted to install, so we can verify on resume
    private var pendingInstallVersion: String? = null

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            _app.value = repo.getApp(repoFullName)
            val appData = _app.value ?: run {
                _error.value = "app not found"
                _isLoading.value = false
                return@launch
            }

            repo.getReleases(appData.owner, appData.repo).fold(
                onSuccess = { _releases.value = it },
                onFailure = { _error.value = it.message },
            )
            _isLoading.value = false
        }
    }

    /**
     * Called when the user returns to this screen after the system installer.
     * Checks PackageManager to see if the install actually succeeded.
     */
    fun verifyPendingInstall() {
        val pendingVersion = pendingInstallVersion ?: return
        val appData = _app.value ?: return
        val pkgName = appData.packageName ?: return
        pendingInstallVersion = null

        val context = getApplication<Application>()
        val installedVersion = PackageUtils.getInstalledVersion(context, pkgName)

        viewModelScope.launch {
            if (installedVersion != null) {
                // Something is installed. Check if it changed.
                // We store the tag name (e.g. "v1.2.3") but PackageManager returns
                // the versionName (e.g. "1.2.3"). Compare flexibly.
                repo.setInstalledVersion(repoFullName, pendingVersion)
                _app.value = repo.getApp(repoFullName)
            }
            // If installedVersion is null, the package isn't installed at all —
            // the install failed or was cancelled. We don't update anything.
        }
    }

    fun installRelease(release: GitHubRelease) {
        val appData = _app.value ?: return

        val selection = ApkSelector.selectApk(
            assets = release.assets,
            preferredPattern = appData.preferredAssetPattern,
        )

        when (selection) {
            is ApkSelection.NoApk -> {
                _error.value = "no APK found in this release"
            }
            is ApkSelection.Selected -> {
                downloadAndInstall(release, selection.asset)
            }
            is ApkSelection.Ambiguous -> {
                _apkPicker.value = selection.candidates
                _pendingRelease = release
            }
        }
    }

    private var _pendingRelease: GitHubRelease? = null

    fun pickApk(asset: GitHubAsset) {
        val release = _pendingRelease ?: return
        _apkPicker.value = null
        _pendingRelease = null

        viewModelScope.launch {
            repo.setPreferredAsset(repoFullName, asset.name)
            _app.value = repo.getApp(repoFullName)
        }

        downloadAndInstall(release, asset)
    }

    fun dismissPicker() {
        _apkPicker.value = null
        _pendingRelease = null
    }

    fun pinVersion(version: String?) {
        viewModelScope.launch {
            repo.pinVersion(repoFullName, version)
            _app.value = repo.getApp(repoFullName)
        }
    }

    private fun downloadAndInstall(release: GitHubRelease, asset: GitHubAsset) {
        val context = getApplication<Application>()
        val apkDir = File(context.filesDir, "apks")
        val apkFile = File(apkDir, "${repoFullName.replace('/', '_')}_${release.tagName}.apk")

        // If already downloaded, go straight to install
        if (apkFile.exists() && apkFile.length() == asset.size) {
            learnPackageNameAndInstall(context, apkFile, release.tagName)
            return
        }

        _downloadProgress.value = DownloadState.Downloading(0f)

        viewModelScope.launch {
            api.downloadFile(
                url = asset.downloadUrl,
                destination = apkFile,
                onProgress = { downloaded, total ->
                    val fraction = if (total > 0) downloaded.toFloat() / total else 0f
                    _downloadProgress.value = DownloadState.Downloading(fraction)
                },
            ).fold(
                onSuccess = {
                    _downloadProgress.value = DownloadState.Idle
                    learnPackageNameAndInstall(context, it, release.tagName)
                },
                onFailure = {
                    _downloadProgress.value = DownloadState.Idle
                    _error.value = "download failed: ${it.message}"
                },
            )
        }
    }

    /**
     * Extracts the package name from the APK (so we can verify install later),
     * saves it, then launches the system installer.
     */
    private fun learnPackageNameAndInstall(context: Context, apkFile: File, tagName: String) {
        // Learn the Android package name from the APK
        val pkgName = PackageUtils.getPackageNameFromApk(context, apkFile)
        if (pkgName != null) {
            viewModelScope.launch {
                repo.setPackageName(repoFullName, pkgName)
                _app.value = repo.getApp(repoFullName)
            }
        }

        // Record what we're attempting to install — verified on resume
        pendingInstallVersion = tagName

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun removeApp() {
        viewModelScope.launch {
            _app.value?.let { repo.removeApp(it) }
        }
    }
}

sealed class DownloadState {
    data object Idle : DownloadState()
    data class Downloading(val progress: Float) : DownloadState()
}
