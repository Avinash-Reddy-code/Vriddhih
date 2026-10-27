package com.vrddhih.app

import android.app.Application
import com.vrddhih.app.core.data.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class VrddhihApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Seed built-in categories idempotently on every launch. ON CONFLICT IGNORE means
        // existing rows are left untouched, so no duplicates are ever inserted.
        applicationScope.launch {
            container.categoryRepository.seedBuiltIns()
        }
    }
}
