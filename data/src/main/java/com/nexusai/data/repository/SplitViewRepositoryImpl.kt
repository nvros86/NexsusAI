package com.nexusai.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nexusai.domain.model.SplitSession
import com.nexusai.domain.repository.SplitViewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.splitDataStore: DataStore<Preferences> by preferencesDataStore(name = "split_view")
private val json = Json { ignoreUnknownKeys = true; isLenient = true }

@Singleton
class SplitViewRepositoryImpl @Inject constructor(
    private val context: Context
) : SplitViewRepository {

    private val sessions = MutableStateFlow<Map<String, SplitSession>>(emptyMap())

    override fun getAllSessions(): Flow<List<SplitSession>> {
        return sessions.map { map -> map.values.sortedByDescending { it.timestamp } }
    }

    override suspend fun getSession(id: String): SplitSession? {
        return sessions.value[id]
    }

    override suspend fun saveSession(session: SplitSession) {
        sessions.value = sessions.value + (session.id to session)
        context.splitDataStore.edit { prefs ->
            prefs[stringPreferencesKey(session.id)] = json.encodeToString(SplitSession.serializer(), session)
        }
    }

    override suspend fun deleteSession(id: String) {
        sessions.value = sessions.value - id
        context.splitDataStore.edit { prefs ->
            prefs.remove(stringPreferencesKey(id))
        }
    }
}
