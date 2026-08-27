package com.nexusai.feature.tabs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.domain.model.Tab
import com.nexusai.domain.usecase.CreateTabUseCase
import com.nexusai.domain.usecase.DeleteTabUseCase
import com.nexusai.domain.usecase.GetTabsUseCase
import com.nexusai.domain.usecase.SetActiveTabUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TabsUiState(
    val tabs: List<Tab> = emptyList(),
    val activeTabId: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class TabsViewModel @Inject constructor(
    private val getTabsUseCase: GetTabsUseCase,
    private val createTabUseCase: CreateTabUseCase,
    private val deleteTabUseCase: DeleteTabUseCase,
    private val setActiveTabUseCase: SetActiveTabUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TabsUiState())
    val uiState: StateFlow<TabsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getTabsUseCase().collect { tabs ->
                _uiState.value = _uiState.value.copy(
                    tabs = tabs,
                    activeTabId = tabs.firstOrNull { it.isActive }?.id
                )
            }
        }
    }

    fun createTab(title: String = "New Tab") {
        viewModelScope.launch {
            val tab = createTabUseCase(title)
            setActiveTab(tab.id)
        }
    }

    fun deleteTab(id: String) {
        viewModelScope.launch {
            deleteTabUseCase(id)
        }
    }

    fun setActiveTab(id: String) {
        viewModelScope.launch {
            setActiveTabUseCase(id)
        }
    }
}
