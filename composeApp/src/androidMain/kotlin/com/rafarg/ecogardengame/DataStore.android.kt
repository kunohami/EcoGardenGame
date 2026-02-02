
package com.rafarg.ecogardengame

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.rafarg.ecogardengame.data.DATA_STORE_FILE_NAME

/**
 * --- ANDROID DATASTORE SETUP ---
 * This file handles the physical creation of the DataStore file on Android.
 */

private var dataStore: DataStore<Preferences>? = null

/**
 * Creates and provides a singleton instance of DataStore.
 * 
 * --- DESIGN PATTERN: SINGLETON & THREAD-SAFETY ---
 * We use 'synchronized' to ensure that if multiple parts of the app 
 * try to create the DataStore at once, only ONE instance is ever created.
 */
fun createDataStore(context: Context): DataStore<Preferences> {
    return dataStore ?: synchronized(Any()) {
        dataStore ?: PreferenceDataStoreFactory.create(
            /**
             * --- FILE SYSTEM ACCESS ---
             * produceFile tells DataStore WHERE to save the data.
             * 'context.filesDir' is a private folder on Android that only 
             * our app can access, making it secure.
             */
            produceFile = { context.filesDir.resolve(DATA_STORE_FILE_NAME) }
        ).also { dataStore = it }
    }
}
