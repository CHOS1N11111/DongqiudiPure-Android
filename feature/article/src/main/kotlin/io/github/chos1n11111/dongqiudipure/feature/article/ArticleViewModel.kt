package io.github.chos1n11111.dongqiudipure.feature.article

import androidx.lifecycle.ViewModel
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import io.github.chos1n11111.dongqiudipure.core.model.ArticleDetail
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.Comment
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.sampledata.SampleFeed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 文章页状态。
 *
 * 正文与评论是**两个独立的 section**：评论接口失效时正文照常阅读
 * （PLAN.md M3：「评论失败不影响正文」）。
 */
data class ArticleUiState(
    val detail: SectionState<ArticleDetail> = SectionState.Loading,
    val comments: SectionState<List<Comment>> = SectionState.Loading,
    val commentSort: CommentSort = CommentSort.Hottest,
)

enum class CommentSort(@StringRes val labelRes: Int) {
    Hottest(R.string.article_comment_sort_hottest),
    Newest(R.string.article_comment_sort_newest),
}

/**
 * ⚠️ 当前从 :core:sampledata 读取假数据。
 * 接入时替换为 `articleRepository.loadArticle(id)` 与 `loadComments(id, sort)`。
 * 详见 docs/engineering/BACKEND-CONTRACT-TODO.md §2.2
 */
class ArticleViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleUiState())
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()

    private var articleId: ArticleId? = null

    fun load(id: ArticleId) {
        if (articleId == id) return
        articleId = id
        loadDetail()
        loadComments()
    }

    fun retryDetail() {
        _uiState.update { it.copy(detail = SectionState.Loading) }
        loadDetail()
    }

    fun retryComments() {
        _uiState.update { it.copy(comments = SectionState.Loading) }
        loadComments()
    }

    fun selectSort(sort: CommentSort) {
        if (sort == _uiState.value.commentSort) return
        _uiState.update { it.copy(commentSort = sort, comments = SectionState.Loading) }
        loadComments()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            // TODO(data): 替换为 Repository 调用。
            delay(400)
            _uiState.update { it.copy(detail = SectionState.Content(SampleFeed.articleDetail)) }
        }
    }

    private fun loadComments() {
        viewModelScope.launch {
            // TODO(data): 替换为 Repository 调用。评论比正文慢是常态，
            //  所以这里的延迟更长 —— 正文应当先出现，不等评论。
            delay(900)
            val list = when (_uiState.value.commentSort) {
                CommentSort.Hottest -> SampleFeed.comments
                CommentSort.Newest -> SampleFeed.comments.reversed()
            }
            _uiState.update {
                it.copy(
                    comments = if (list.isEmpty()) {
                        SectionState.Empty
                    } else {
                        SectionState.Content(list)
                    },
                )
            }
        }
    }
}
