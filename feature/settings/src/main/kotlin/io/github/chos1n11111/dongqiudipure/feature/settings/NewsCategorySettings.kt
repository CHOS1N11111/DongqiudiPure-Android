package io.github.chos1n11111.dongqiudipure.feature.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chos1n11111.dongqiudipure.core.data.NewsRepository
import io.github.chos1n11111.dongqiudipure.core.designsystem.R as DesignR
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.model.NewsCategory
import javax.inject.Inject

@HiltViewModel
class NewsCategorySettingsViewModel @Inject constructor(
    repository: NewsRepository,
) : ViewModel() {
    val categories: List<NewsCategory> = repository.categories
}

@Composable
fun NewsCategorySettingsRoute(
    preferences: NewsPreferences,
    onCategoryToggle: (String, Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewsCategorySettingsViewModel = hiltViewModel(),
) {
    NewsCategorySettingsScreen(
        categories = viewModel.categories,
        preferences = preferences,
        onCategoryToggle = onCategoryToggle,
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewsCategorySettingsScreen(
    categories: List<NewsCategory>,
    preferences: NewsPreferences,
    onCategoryToggle: (String, Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filteredCategories = filterNewsCategories(categories, query)
    val selectedVisibleCount = categories.count { it.id in preferences.categoryIds }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_news_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(DqdIcons.ArrowBack),
                            contentDescription = stringResource(DesignR.string.ds_action_back),
                            modifier = Modifier.size(DqdSize.iconMedium),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SettingsSearchField(
                query = query,
                onQueryChange = { query = it },
                placeholder = stringResource(R.string.settings_news_search_hint),
            )
            if (filteredCategories.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                    SettingsSearchEmptyState(query)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredCategories, key = { it.id }) { category ->
                        val selected = category.id in preferences.categoryIds
                        SelectionSettingsRow(
                            label = category.label,
                            selected = selected,
                            enabled = !selected || selectedVisibleCount > 1,
                            onClick = {
                                onCategoryToggle(category.id, !selected)
                            },
                        )
                    }
                }
            }
        }
    }
}

internal fun filterNewsCategories(
    categories: List<NewsCategory>,
    query: String,
): List<NewsCategory> {
    val term = query.trim()
    return if (term.isEmpty()) categories else categories.filter {
        it.label.contains(term, ignoreCase = true)
    }
}
