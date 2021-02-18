package goodtime.training.wod.timer.ui.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.Nullable
import androidx.preference.PreferenceDataStore

class PreferenceDataStore(context: Context) : PreferenceDataStore() {
    val preferences: SharedPreferences =
        context.getSharedPreferences("GoodtimeTrainingSharedPreferences", Context.MODE_PRIVATE)

    override fun putString(key: String?, @Nullable value: String?) {
        preferences.edit().putString(key, value).apply()
    }

    override fun putStringSet(key: String?, @Nullable values: Set<String?>?) {
        preferences.edit().putStringSet(key, values).apply()
    }

    override fun putInt(key: String?, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }

    override fun putLong(key: String?, value: Long) {
        preferences.edit().putLong(key, value).apply()
    }

    override fun putFloat(key: String?, value: Float) {
        preferences.edit().putFloat(key, value).apply()
    }

    override fun putBoolean(key: String?, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    @Nullable
    override fun getString(key: String?, @Nullable defValue: String?): String? {
        return preferences.getString(key, defValue)
    }

    @Nullable
    override fun getStringSet(key: String?, @Nullable defValues: Set<String?>?): Set<String>? {
        return preferences.getStringSet(key, defValues)
    }

    override fun getInt(key: String?, defValue: Int): Int {
        return preferences.getInt(key, defValue)
    }

    override fun getLong(key: String?, defValue: Long): Long {
        return preferences.getLong(key, defValue)
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return preferences.getFloat(key, defValue)
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return preferences.getBoolean(key, defValue)
    }

    fun getBooleanArray(key: String, size: Int): BooleanArray {
        val result = BooleanArray(size)
        for (i in 0 until size) {
            result[i] = preferences.getBoolean(key + "_" + i, false)
        }
        return result
    }
}