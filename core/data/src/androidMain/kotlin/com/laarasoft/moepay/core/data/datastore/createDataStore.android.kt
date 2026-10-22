package com.laarasoft.moepay.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

internal fun createDataStore(context: Context): DataStore<Preferences> {
    return createDataStore {
        context.applicationContext.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath
    }
}
