package io.y2k.coeffects

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import kotlinx.coroutines.*

object SharedPrefsSyncer {

    fun <T, R> register(s: Store<T>, p: SharedPreferences, listenKey: String, f: (T, R) -> T, fo: (T) -> R): Job {
        val l = OnSharedPreferenceChangeListener { _, key ->
            if (listenKey == key) {
                @Suppress("UNCHECKED_CAST")
                val value = p.all[key] as R
                s.update {
                    f(it, value)
                }
            }
        }

        p.registerOnSharedPreferenceChangeListener(l)

        return GlobalScope.launch(Dispatchers.Main) {
            try {
                while (true) {
                    val r = s.read(fo)
                    @Suppress("UNCHECKED_CAST")
                    val old = p.all[listenKey] as R

                    if (old != r) {
                        when (r) {
                            is String -> p.edit().putString(listenKey, r).apply()
                            else -> TODO()
                        }
                    }

                    delay(500)
                }
            } finally {
                p.unregisterOnSharedPreferenceChangeListener(l)
            }
        }
    }
}
