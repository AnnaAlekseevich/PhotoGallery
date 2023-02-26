package com.bignerdranch.android.photogallery

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager.getDefaultSharedPreferences

private const val PREF_SEARCH_QUERY = "searchQuery"
private const val PREF_LAST_RESULT_ID = "lastResultId"
private const val PREF_IS_POLLING = "isPolling"

object QueryPreferences {

    fun getStoredQuery(context: Context): String {
        val prefs = getDefaultSharedPreferences(context)
        return prefs.getString(PREF_SEARCH_QUERY, "")!!
    }

    fun setStoredQuery(context: Context, query: String) {
        getDefaultSharedPreferences(context)
            .edit{
                putString(PREF_SEARCH_QUERY, query)
            }
    }

    fun getLastResultId(context: Context): String {
        return getDefaultSharedPreferences(context)
            .getString(PREF_LAST_RESULT_ID, "")!!
    }
    fun setLastResultId(context: Context, lastResultId: String) {
        getDefaultSharedPreferences(context).edit {
            putString(PREF_LAST_RESULT_ID, lastResultId)
        }
    }

    fun isPolling(context: Context): Boolean {
        return getDefaultSharedPreferences(context)
            .getBoolean(PREF_IS_POLLING, false)
    }
    fun setPolling(context: Context, isOn: Boolean) {
        getDefaultSharedPreferences(context).edit {
            putBoolean(PREF_IS_POLLING, isOn)
        }
    }

}