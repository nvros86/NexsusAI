package com.nexusai.domain.usecase

import com.nexusai.domain.model.Tab
import com.nexusai.domain.repository.TabRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class GetTabsUseCase @Inject constructor(
    private val repository: TabRepository
) {
    operator fun invoke(): Flow<List<Tab>> = repository.getAllTabs()
}

class CreateTabUseCase @Inject constructor(
    private val repository: TabRepository
) {
    suspend operator fun invoke(title: String = "New Tab"): Tab {
        val tab = Tab(
            id = UUID.randomUUID().toString(),
            title = title
        )
        return repository.createTab(tab)
    }
}

class DeleteTabUseCase @Inject constructor(
    private val repository: TabRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteTab(id)
}

class SetActiveTabUseCase @Inject constructor(
    private val repository: TabRepository
) {
    suspend operator fun invoke(id: String) = repository.setActiveTab(id)
}
