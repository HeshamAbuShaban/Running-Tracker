package dev.training.running_tracker.app_system.prefs

import android.content.SharedPreferences
import javax.inject.Inject


/**
 * this object singleton class
 * make the use of the dagger sharedpref instance
 * with necessary method for the use case in the app
 * */
class AppSharedPreferences @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) {

    // USERNAME
    private fun putUsername(username: String) {
        sharedPreferences.edit().apply {
            putString(Keys.NAME, username)
            apply()
        }
    }

    /*fun removeUsername() {
        sharedPreferences.edit().apply {
            remove(Keys.NAME)
            apply()
        }
    }*/


    // Weight
    private fun putWeight(weight: String) {
        sharedPreferences.edit().apply {
            putFloat(Keys.WEIGHT, weight.toFloat())
            apply()
        }
    }


    /*fun removeWeight() {
        sharedPreferences.edit().apply {
            remove(Keys.WEIGHT)
            apply()
        }
    }*/


    private fun putFirstTimeToggle(state: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean(Keys.FIRST_TIME_TOGGLE, state)
            apply()
        }
    }

    // Silly addition
    fun writePersonalDataToSharedPreferences(name: String, weight: String, state: Boolean) {
        putUsername(name)
        putWeight(weight)
        putFirstTimeToggle(state)
    }

    fun writePersonalDataToSharedPreferences(name: String, weight: String) {
        putUsername(name)
        putWeight(weight)
    }

    fun readPersonalDataToSharedPreferences(): Pair<String, Float> =
        Pair(getUsername(), getWeight())

    // ...Getters
    fun getUsername(): String =
        sharedPreferences.getString(Keys.NAME, "Undefined") ?: "Undefined.."

    fun getWeight(): Float =
        sharedPreferences.getFloat(Keys.WEIGHT, 80f)

    fun getFirstTimeToggle(): Boolean =
        sharedPreferences.getBoolean(Keys.FIRST_TIME_TOGGLE, true)
}