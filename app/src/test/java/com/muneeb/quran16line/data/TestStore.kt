package com.muneeb.quran16line.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import java.io.File
import java.nio.file.Files

fun testStore(scope: CoroutineScope): DataStore<Preferences> {
    val dir = Files.createTempDirectory("ds").toFile()
    return PreferenceDataStoreFactory.create(scope = scope, produceFile = { File(dir, "test.preferences_pb") })
}
