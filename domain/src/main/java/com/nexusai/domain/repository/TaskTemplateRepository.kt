package com.nexusai.domain.repository

import com.nexusai.domain.model.TaskTemplate
import com.nexusai.domain.model.TemplateCategory

interface TaskTemplateRepository {
    fun getAllTemplates(): List<TaskTemplate>
    fun getTemplatesByCategory(category: TemplateCategory): List<TaskTemplate>
    fun getTemplateById(id: String): TaskTemplate?
    fun searchTemplates(query: String): List<TaskTemplate>
}
