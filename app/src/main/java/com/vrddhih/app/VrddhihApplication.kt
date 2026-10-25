package com.vrddhih.app

import android.app.Application
import com.vrddhih.app.core.data.AppContainer

class VrddhihApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
