package io.github.chos1n11111.dongqiudipure.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chos1n11111.dongqiudipure.core.data.SessionRepository
import io.github.chos1n11111.dongqiudipure.core.data.SessionState
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
) : ViewModel() {
    val sessionState: StateFlow<SessionState> = sessionRepository.state
    private var loginJob: Job? = null

    fun login(identifier: String, password: String) {
        if (loginJob?.isActive == true) return
        loginJob = viewModelScope.launch {
            try {
                sessionRepository.login(identifier, password)
            } finally {
                loginJob = null
            }
        }
    }

    fun retrySessionValidation() {
        viewModelScope.launch { sessionRepository.restore() }
    }

    fun logout() {
        viewModelScope.launch { sessionRepository.logout() }
    }
}
