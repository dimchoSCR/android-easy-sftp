package apps.dcoder.easysftp.services.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager

class SharedPrefsStorageService(private val appContext: Context) {

    private fun getSharedPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(appContext)
    }

    fun putString(key: String, data: String, writeAsync: Boolean = false) {
        val sharedPrefsEditor = getSharedPreferences().edit().putString(key, data)

        if (writeAsync) {
            sharedPrefsEditor.apply()
        } else {
            if (!sharedPrefsEditor.commit()) {
                Log.e(this.javaClass.simpleName, "Could not write to shared preferences!")
            }
        }
    }

    fun readValue(key: String, defaultValue: String? = null): String? {
        return getSharedPreferences().getString(key, defaultValue)
    }

}