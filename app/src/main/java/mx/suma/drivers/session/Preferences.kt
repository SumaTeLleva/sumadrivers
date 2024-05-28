package mx.suma.drivers.session

import android.content.SharedPreferences

interface Preferences {

    fun getEditor(): SharedPreferences.Editor
    fun getPreferences(): SharedPreferences
    fun shouldBulkUpdate(): Boolean
    fun settingsName(): String

    fun put(key: PreferenceKey, value: String): Preferences {
        getEditor().putString(key.name, value)
        doCommit()

        return this
    }

    fun put(key: PreferenceKey, value: Int): Preferences {
        getEditor().putInt(key.name, value)
        doCommit()

        return this
    }

    fun put(key: PreferenceKey, value: Boolean): Preferences {
        getEditor().putBoolean(key.name, value)
        doCommit()
        return this
    }

    fun put(key: PreferenceKey, value: Float): Preferences {
        getEditor().putFloat(key.name, value)
        doCommit()

        return this
    }

    /**
     * Convenience method for storing doubles.
     *
     *
     * There may be instances where the accuracy of a double is desired.
     * SharedPreferences does not handle doubles so they have to
     * cast to and from String.

     * @param key The enum of the preference to store.
     * *
     * @param val The new value for the preference.
     */
    fun put(key: PreferenceKey, value: Double): Preferences {
        getEditor().putString(key.name, value.toString())
        doCommit()

        return this
    }

    fun put(key: PreferenceKey, value: Long): Preferences {
        getEditor().putLong(key.name, value)
        doCommit()

        return this
    }

    fun getString(key: PreferenceKey, defaultValue: String): String {
        return getPreferences().getString(key.name, defaultValue)!!
    }

    fun getString(key: PreferenceKey): String {
        return getPreferences().getString(key.name, "")!!
    }

    fun getInt(key: PreferenceKey): Int {
        return getPreferences().getInt(key.name, 0)
    }

    fun getInt(key: PreferenceKey, defaultValue: Int): Int {
        return getPreferences().getInt(key.name, defaultValue)
    }

    fun getLong(key: PreferenceKey): Long {
        return getPreferences().getLong(key.name, 0)
    }

    fun getLong(key: PreferenceKey, defaultValue: Long): Long {
        return getPreferences().getLong(key.name, defaultValue)
    }


    fun getFloat(key: PreferenceKey): Float {
        return getPreferences().getFloat(key.name, 0f)
    }

    fun getFloat(key: PreferenceKey, defaultValue: Float): Float {
        return getPreferences().getFloat(key.name, defaultValue)
    }

    /**
     * Convenience method for retrieving doubles.
     *
     *
     * There may be instances where the accuracy of a double is desired.
     * SharedPreferences does not handle doubles so they have to
     * cast to and from String.

     * @param key The enum of the preference to fetch.
     */
    fun getDouble(key: PreferenceKey, defaultValue: Double = 0.0): Double {
        return try {
            getPreferences().getString(key.name, defaultValue.toString())!!.toDouble()
        } catch (nfe: NumberFormatException) {
            defaultValue
        }
    }

    fun getBoolean(key: PreferenceKey, defaultValue: Boolean): Boolean {
        return getPreferences().getBoolean(key.name, defaultValue)
    }

    fun getBoolean(key: PreferenceKey): Boolean {
        return getPreferences().getBoolean(key.name, false)
    }

    /**
     * Remove keys from SharedPreferences.

     * @param keys The enum of the key(s) to be removed.
     */
    fun remove(vararg keys: PreferenceKey) {
        for (key in keys) {
            getEditor().remove(key.name)
        }

        doCommit()
    }

    /**
     * Remove all keys from SharedPreferences.
     */
    fun clear() {
        this.getEditor().clear()
//        getEditor().clear()
        this.doCommit()
    }

    private fun doCommit() {
        this.getEditor().apply()
    }
}