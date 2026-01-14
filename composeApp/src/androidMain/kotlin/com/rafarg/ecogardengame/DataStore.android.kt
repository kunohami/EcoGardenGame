
package com.rafarg.ecogardengame

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.rafarg.ecogardengame.data.DATA_STORE_FILE_NAME

// import okio.Path.Companion.toPath // Remove this import

private var dataStore: DataStore<Preferences>? = null

fun createDataStore(context: Context): DataStore<Preferences> {
    return dataStore ?: synchronized(Any()) {
        dataStore ?: PreferenceDataStoreFactory.create(
            // produceFile expects a () -> File, not Path
            produceFile = { context.filesDir.resolve(DATA_STORE_FILE_NAME) }
        ).also { dataStore = it }
    }
}
