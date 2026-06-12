package com.juanpcf.caloriestracker.core.logging

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Árbol de Timber para release: manda los logs a Crashlytics. Las prioridades WARN/ERROR registran
 * un mensaje, y los ERROR con throwable se reportan como non-fatal. VERBOSE/DEBUG/INFO se ignoran
 * para no inflar Crashlytics con ruido.
 */
class CrashlyticsTree : Timber.Tree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean =
        priority >= Log.WARN

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.log(message)
        if (t != null && priority >= Log.ERROR) {
            crashlytics.recordException(t)
        }
    }
}
