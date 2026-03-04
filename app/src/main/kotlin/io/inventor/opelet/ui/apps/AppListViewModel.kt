package io.inventor.opelet.ui.apps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.inventor.opelet.data.AppRepository
import io.inventor.opelet.data.OpeletDatabase
import io.inventor.opelet.data.TrackedApp
import io.inventor.opelet.data.updateStatus
import io.inventor.opelet.model.UpdateStatus
import io.inventor.opelet.network.GitHubApi
import io.inventor.opelet.util.RepoParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppListViewModel(application: Application) : AndroidViewModel(application) {

    private val db = OpeletDatabase.get(application)
    private val repo = AppRepository(db.trackedAppDao(), GitHubApi())

    val apps: StateFlow<List<TrackedApp>> = repo.observeApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _addInput = MutableStateFlow("")
    val addInput: StateFlow<String> = _addInput.asStateFlow()

    private val _addError = MutableStateFlow<String?>(null)
    val addError: StateFlow<String?> = _addError.asStateFlow()

    private val _isAdding = MutableStateFlow(false)
    val isAdding: StateFlow<Boolean> = _isAdding.asStateFlow()

    init {
        viewModelScope.launch {
            repo.ensureSelfTracked()
        }
    }

    fun onAddInputChanged(value: String) {
        _addInput.value = value
        _addError.value = null
    }

    fun addApp() {
        val input = _addInput.value.trim()
        if (input.isEmpty()) return

        val parsed = RepoParser.parse(input)
        if (parsed == null) {
            _addError.value = "not a valid repo: owner/repo or github url"
            return
        }

        _isAdding.value = true
        _addError.value = null

        viewModelScope.launch {
            val result = repo.addApp(parsed.owner, parsed.repo)
            result.fold(
                onSuccess = {
                    _addInput.value = ""
                },
                onFailure = { error ->
                    _addError.value = error.message ?: "failed to add repo"
                },
            )
            _isAdding.value = false
        }
    }

    fun refresh() {
        if (_isRefreshing.value) return
        _isRefreshing.value = true
        viewModelScope.launch {
            repo.refreshAll()
            _isRefreshing.value = false
        }
    }

    fun removeApp(app: TrackedApp) {
        viewModelScope.launch {
            repo.removeApp(app)
        }
    }
}
