package io.github.chos1n11111.dongqiudipure.core.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.chos1n11111.dongqiudipure.core.model.AppError
import io.github.chos1n11111.dongqiudipure.core.model.AppErrorException
import io.github.chos1n11111.dongqiudipure.core.model.EndpointId
import io.github.chos1n11111.dongqiudipure.core.model.TeamCirclePost
import io.github.chos1n11111.dongqiudipure.core.network.ApiResult
import io.github.chos1n11111.dongqiudipure.core.network.FootballRemoteDataSource

internal class TeamCirclePagingSource(
    private val remote: FootballRemoteDataSource,
    private val groupId: String,
) : PagingSource<Int, TeamCirclePost>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TeamCirclePost> {
        val page = params.key ?: 1
        return when (val result = remote.loadTeamCircle(groupId, page)) {
            is ApiResult.Failure -> LoadResult.Error(AppErrorException(result.error))
            is ApiResult.Success -> try {
                if (result.value.code.scalarFootball() != "200") throw ContractViolation()
                val payload = result.value.data ?: throw ContractViolation()
                val currentPage = payload.currentPage.optionalFootballInt() ?: throw ContractViolation()
                val lastPage = payload.lastPage.optionalFootballInt() ?: throw ContractViolation()
                val posts = payload.data ?: throw ContractViolation()
                if (currentPage != page || lastPage < currentPage) {
                    throw ContractViolation()
                }
                LoadResult.Page(
                    data = posts.mapNotNull { post ->
                        runCatching { post.toDomain() }.getOrNull()
                    }.distinctBy { it.id },
                    prevKey = null,
                    nextKey = (page + 1).takeIf { page < lastPage },
                )
            } catch (_: ContractViolation) {
                LoadResult.Error(
                    AppErrorException(AppError.UnsupportedContract(TEAM_CIRCLE_ENDPOINT)),
                )
            }
        }
    }

    override fun getRefreshKey(state: PagingState<Int, TeamCirclePost>): Int? = null
}

private val TEAM_CIRCLE_ENDPOINT = EndpointId("football.team-circle")
