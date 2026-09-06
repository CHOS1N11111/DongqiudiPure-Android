package io.github.chos1n11111.dongqiudipure.feature.home

import androidx.lifecycle.ViewModelStore
import androidx.paging.PagingData
import io.github.chos1n11111.dongqiudipure.core.data.NewsRepository
import io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary
import io.github.chos1n11111.dongqiudipure.core.model.NewsCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val headline = NewsCategory("1", "Headlines")
    private val sports = NewsCategory("248", "Sports")
    private val enabledIds = setOf(headline.id, sports.id)
    private val viewModelStore = ViewModelStore()
    private lateinit var repository: RecordingNewsRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        repository = RecordingNewsRepository(listOf(headline, sports))
        viewModel = HomeViewModel(repository)
        viewModelStore.put("home", viewModel)
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun `switching categories preserves the disabled football filter`() = runTest {
        viewModel.setPreferences(enabledIds, footballOnly = false)
        viewModel.feed.launchIn(backgroundScope)
        runCurrent()

        viewModel.selectCategory(sports)
        runCurrent()
        viewModel.selectCategory(headline)
        runCurrent()

        assertEquals(
            listOf(
                FeedRequest(headline, footballOnly = false),
                FeedRequest(sports, footballOnly = false),
                FeedRequest(headline, footballOnly = false),
            ),
            repository.requests,
        )
    }

    @Test
    fun `switching after refresh preserves filtering without forcing another refresh`() = runTest {
        viewModel.setPreferences(enabledIds, footballOnly = false)
        viewModel.feed.launchIn(backgroundScope)
        runCurrent()

        viewModel.refresh()
        runCurrent()
        assertEquals(
            FeedRequest(headline, footballOnly = false, fresh = true),
            repository.requests.last(),
        )

        viewModel.selectCategory(sports)
        runCurrent()

        assertEquals(FeedRequest(sports, footballOnly = false), repository.requests.last())
    }

    @Test
    fun `switching categories keeps a reenabled football filter`() = runTest {
        viewModel.setPreferences(enabledIds, footballOnly = false)
        viewModel.feed.launchIn(backgroundScope)
        runCurrent()

        viewModel.setPreferences(enabledIds, footballOnly = true)
        runCurrent()
        viewModel.selectCategory(sports)
        runCurrent()

        assertEquals(FeedRequest(sports, footballOnly = true), repository.requests.last())
    }

    private data class FeedRequest(
        val category: NewsCategory,
        val footballOnly: Boolean,
        val fresh: Boolean = false,
    )

    private class RecordingNewsRepository(
        override val categories: List<NewsCategory>,
    ) : NewsRepository {
        val requests = mutableListOf<FeedRequest>()

        override fun pagedFeed(
            category: NewsCategory,
            fresh: Boolean,
            footballOnly: Boolean,
        ): Flow<PagingData<ArticleSummary>> {
            requests += FeedRequest(category, footballOnly, fresh)
            return flowOf(PagingData.empty())
        }
    }
}
