package io.inventor.opelet

import android.app.Application
import io.inventor.opelet.worker.UpdateWorker

class OpeletApp : Application() {
    override fun onCreate() {
        super.onCreate()
        UpdateWorker.schedule(this)
    }
}
