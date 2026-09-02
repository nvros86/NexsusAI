package com.nexusai.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.nexusai.app.R
import com.nexusai.domain.model.TemplateCategory
import com.nexusai.domain.model.TaskTemplate
import com.nexusai.domain.repository.TaskTemplateRepository
import com.nexusai.core.ui.theme.NexusBackground
import com.nexusai.core.ui.theme.NexusCard
import com.nexusai.core.ui.theme.NexusPurple
import com.nexusai.core.ui.theme.NexusSurface
import com.nexusai.core.ui.theme.NexusTextPrimary
import com.nexusai.core.ui.theme.NexusTextSecondary
import com.nexusai.core.ui.theme.NexusTextTertiary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TemplatesScreen(
    repository: TaskTemplateRepository,
    onTemplateClick: (TaskTemplate) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<TemplateCategory?>(null) }

    val templates = remember(searchQuery, selectedCategory) {
        when {
            searchQuery.isNotBlank() -> repository.searchTemplates(searchQuery)
            selectedCategory != null -> repository.getTemplatesByCategory(selectedCategory!!)
            else -> repository.getAllTemplates()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexusBackground)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.templates_title), color = NexusTextPrimary) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = NexusTextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = NexusBackground
            )
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text(stringResource(R.string.templates_search_hint), color = NexusTextTertiary) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = NexusTextTertiary
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NexusPurple,
                unfocusedBorderColor = NexusTextTertiary.copy(alpha = 0.3f),
                focusedContainerColor = NexusSurface,
                unfocusedContainerColor = NexusSurface
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip(
                label = stringResource(R.string.templates_all),
                selected = selectedCategory == null,
                onClick = { selectedCategory = null }
            )
            TemplateCategory.entries.forEach { category ->
                CategoryChip(
                    label = "${category.emoji} ${category.displayName}",
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = if (selectedCategory == category) null else category }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(templates, key = { it.id }) { template ->
                TemplateCard(
                    template = template,
                    onClick = { onTemplateClick(template) }
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) NexusPurple
                else NexusSurface
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (selected) NexusTextPrimary else NexusTextSecondary,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TemplateCard(
    template: TaskTemplate,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NexusCard)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = template.iconEmoji,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.title,
                    color = NexusTextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = template.category.displayName,
                    color = NexusPurple,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = template.description,
            color = NexusTextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )

        if (template.inputPlaceholders.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                template.inputPlaceholders.forEach { input ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NexusPurple.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = input.label,
                            color = NexusPurple,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
