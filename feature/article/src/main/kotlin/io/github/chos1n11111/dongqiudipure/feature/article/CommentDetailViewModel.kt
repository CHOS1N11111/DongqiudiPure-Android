package io.github.chos1n11111.dongqiudipure.feature.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chos1n11111.dongqiudipure.core.data.ArticleRepository
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.Comment
import io.github.chos1n11111.dongqiudipure.core.model.DataResult
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CommentDetailUiState(
    val parent: SectionState<Comment> = SectionState.Loading,
)

private data class CommentThreadKey(
    val articleId: ArticleId,
    val commentId: String,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CommentDetailViewModel @Inject constructor(
    private val repository: ArticleRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommentDetailUiState())
    val uiState: StateFlow<CommentDetailUiState> = _uiState.asStateFlow()
    private val threadKey = MutableStateFlow<CommentThreadKey?>(null)

    val replies: Flow<PagingData<Comment>> = threadKey
        .filterNotNull()
        .flatMapLatest { repository.pagedReplies(it.articleId, it.commentId) }
        .cachedIn(viewModelScope)

    fun load(articleId: ArticleId, commentId: String) {
        val key = CommentThreadKey(articleId, commentId)
        if (threadKey.value == key) return
        threadKey.value = key
        _uiState.value = CommentDetailUiState()
        loadParent(key)
    }

    fun retryParent() {
        val key = threadKey.value ?: return
        _uiState.update { it.copy(parent = SectionState.Loading) }
        loadParent(key)
    }

    private fun loadParent(key: CommentThreadKey) {
        viewModelScope.launch {
            when (
                val result = repository.loadCommentThread(key.articleId, key.commentId)
            ) {
                is DataResult.Failure -> _uiState.update {
                    if (threadKey.value == key) it.copy(parent = SectionState.Failed(result.error)) else it
                }
                is DataResult.Success -> _uiState.update {
                    if (threadKey.value == key) it.copy(parent = SectionState.Content(result.value)) else it
                }
            }
        }
    }
}
