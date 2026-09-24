package io.github.chos1n11111.dongqiudipure

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.chos1n11111.dongqiudipure.core.data.SessionRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class DqdApplication : Application() {
    @Inject
    lateinit var sessionRepository: SessionRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch { sessionRepository.restore() }
    }
}
