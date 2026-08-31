package com.nexusai.domain.repository

import com.nexusai.domain.model.AutomationChain
import com.nexusai.domain.model.ChainRunResult
import kotlinx.coroutines.flow.Flow

interface ChainRepository {
    fun getAllChains(): Flow<List<AutomationChain>>
    suspend fun getChainById(id: String): AutomationChain?
    suspend fun saveChain(chain: AutomationChain)
    suspend fun deleteChain(id: String)
    suspend fun runChain(chain: AutomationChain): ChainRunResult
}
