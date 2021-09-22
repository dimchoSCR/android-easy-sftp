package apps.dcoder.easysftp.services.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.preference.PreferenceManager

class SharedPrefsStorageService(private val appContext: Context) {

    private fun getSharedPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(appContext)
    }

    private fun doPrefsWrite(sharedPrefsEditor: SharedPreferences.Editor, writeAsync: Boolean) {
        if (writeAsync) {
            sharedPrefsEditor.apply()
        } else {
            if (!sharedPrefsEditor.commit()) {
                Log.e(this.javaClass.simpleName, "Could not write to shared preferences!")
            }
        }
    }

    fun putString(key: String, data: String, writeAsync: Boolean = true) {
        val sharedPrefsEditor = getSharedPreferences().edit().putString(key, data)
        doPrefsWrite(sharedPrefsEditor, writeAsync)
    }

    fun readValue(key: String, defaultValue: String? = null): String? {
        return getSharedPreferences().getString(key, defaultValue)
    }

    @WorkerThread
    fun readAll(): Map<String, *> {
        return getSharedPreferences().all
    }

    fun removeKey(key: String, writeAsync: Boolean = true) {
        val sharedPrefsEditor = getSharedPreferences().edit().remove(key)
        doPrefsWrite(sharedPrefsEditor, writeAsync)
    }

    fun containsKey(key: String): Boolean = getSharedPreferences().contains(key)
}