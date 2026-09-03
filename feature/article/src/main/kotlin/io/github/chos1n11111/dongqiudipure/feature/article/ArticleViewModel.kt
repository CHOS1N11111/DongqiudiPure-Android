package io.github.chos1n11111.dongqiudipure.feature.article

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chos1n11111.dongqiudipure.core.data.ArticleRepository
import io.github.chos1n11111.dongqiudipure.core.data.FootballCatalogRepository
import io.github.chos1n11111.dongqiudipure.core.model.ArticleBlock
import io.github.chos1n11111.dongqiudipure.core.model.ArticleDetail
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.ArticleLinkTarget
import io.github.chos1n11111.dongqiudipure.core.model.Comment
import io.github.chos1n11111.dongqiudipure.core.model.CommentOrder
import io.github.chos1n11111.dongqiudipure.core.model.DataResult
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ArticleUiState(
    val detail: SectionState<ArticleDetail> = SectionState.Loading,
    val commentSort: CommentSort = CommentSort.Hottest,
)

enum class CommentSort(
    @param:StringRes val labelRes: Int,
    val order: CommentOrder,
) {
    Hottest(R.string.article_comment_sort_hottest, CommentOrder.Recommended),
    Newest(R.string.article_comment_sort_newest, CommentOrder.Newest),
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val repository: ArticleRepository,
    private val footballCatalogRepository: FootballCatalogRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleUiState())
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()

    private val articleId = MutableStateFlow<ArticleId?>(null)
    private val commentOrder = MutableStateFlow(CommentSort.Hottest)

    val comments: Flow<PagingData<Comment>> = combine(
        articleId.filterNotNull(),
        commentOrder,
    ) { id, sort -> id to sort.order }
        .flatMapLatest { (id, order) -> repository.pagedComments(id, order) }
        .cachedIn(viewModelScope)

    fun load(id: ArticleId) {
        if (articleId.value == id) return
        articleId.value = id
        _uiState.update { it.copy(detail = SectionState.Loading) }
        loadDetail(id)
    }

    fun retryDetail() {
        val id = articleId.value ?: return
        _uiState.update { it.copy(detail = SectionState.Loading) }
        loadDetail(id)
    }

    fun selectSort(sort: CommentSort) {
        if (sort == commentOrder.value) return
        commentOrder.value = sort
        _uiState.update { it.copy(commentSort = sort) }
    }

    private fun loadDetail(id: ArticleId) {
        viewModelScope.launch {
            val result = repository.loadArticle(id).resolveCatalogLinks()
            if (articleId.value != id) return@launch
            _uiState.update { state ->
                when (result) {
                    is DataResult.Success -> state.copy(
                        detail = SectionState.Content(result.value),
                    )

                    is DataResult.Failure -> state.copy(detail = SectionState.Failed(result.error))
                }
            }
        }
    }

    private suspend fun DataResult<ArticleDetail>.resolveCatalogLinks(): DataResult<ArticleDetail> {
        if (this !is DataResult.Success) return this
        val catalogIds = value.blocks.mapNotNull { block ->
            ((block as? ArticleBlock.Link)?.target as? ArticleLinkTarget.CompetitionCatalog)?.id
        }.toSet()
        if (catalogIds.isEmpty()) return this

        val catalog = footballCatalogRepository.loadCompetitionCatalog()
        if (catalog !is DataResult.Success) return this
        val byCatalogId = catalog.value
            .flatMap { it.competitions }
            .mapNotNull { competition -> competition.catalogId?.let { it to competition.id } }
            .toMap()
        return DataResult.Success(
            value.copy(
                blocks = value.blocks.map { block ->
                    val link = block as? ArticleBlock.Link ?: return@map block
                    val unresolved = link.target as? ArticleLinkTarget.CompetitionCatalog
                        ?: return@map block
                    byCatalogId[unresolved.id]?.let { competitionId ->
                        link.copy(target = ArticleLinkTarget.Competition(competitionId))
                    } ?: block
                },
            ),
        )
    }
}
