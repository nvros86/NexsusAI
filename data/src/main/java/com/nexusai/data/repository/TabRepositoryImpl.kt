package com.nexusai.data.repository

import com.nexusai.data.local.TabDao
import com.nexusai.data.local.TabEntity
import com.nexusai.domain.model.Tab
import com.nexusai.domain.repository.TabRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class TabRepositoryImpl @Inject constructor(
    private val dao: TabDao
) : TabRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getAllTabs(): Flow<List<Tab>> {
        return dao.getAllTabs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTabById(id: String): Tab? {
        return dao.getTabById(id)?.toDomain()
    }

    override suspend fun createTab(tab: Tab): Tab {
        dao.insertTab(tab.toEntity())
        return tab
    }

    override suspend fun updateTab(tab: Tab) {
        dao.updateTab(tab.toEntity())
    }

    override suspend fun deleteTab(id: String) {
        dao.deleteTabById(id)
    }

    override suspend fun setActiveTab(id: String) {
        dao.deactivateAllTabs()
        dao.setActiveTab(id)
    }

    private fun TabEntity.toDomain() = Tab(
        id = id,
        title = title,
        aiProviderId = aiProviderId,
        messages = try {
            json.decodeFromString(messagesJson)
        } catch (e: Exception) {
            emptyList()
        },
        attachedFiles = try {
            json.decodeFromString(attachedFilesJson)
        } catch (e: Exception) {
            emptyList()
        },
        isActive = isActive,
        createdAt = createdAt,
        accentColor = accentColor
    )

    private fun Tab.toEntity() = TabEntity(
        id = id,
        title = title,
        aiProviderId = aiProviderId,
        messagesJson = json.encodeToString(messages),
        attachedFilesJson = json.encodeToString(attachedFiles),
        isActive = isActive,
        createdAt = createdAt,
        accentColor = accentColor
    )
}
