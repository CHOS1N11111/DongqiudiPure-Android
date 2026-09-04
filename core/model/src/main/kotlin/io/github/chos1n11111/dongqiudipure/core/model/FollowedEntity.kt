package io.github.chos1n11111.dongqiudipure.core.model

/**
 * An entity kept in the local "main team" strip.
 *
 * Keeping this model separate from the account follow state is intentional: the anonymous
 * experience is local-only and never claims that a remote follow operation has succeeded.
 */
sealed interface FollowedEntity {
    val stableKey: String
    val name: String
    val imageUrl: String?
    val secondaryLabel: String?

    data class Team(
        val team: TeamRef,
        override val secondaryLabel: String? = null,
    ) : FollowedEntity {
        override val stableKey: String = "team:${team.id.raw}"
        override val name: String = team.name
        override val imageUrl: String? = team.crestUrl
    }

    data class Player(
        val player: PlayerRef,
        override val secondaryLabel: String? = null,
    ) : FollowedEntity {
        override val stableKey: String = "player:${player.id.raw}"
        override val name: String = player.name
        override val imageUrl: String? = player.avatarUrl
    }
}

data class FollowedEntityPreferences(
    val mainTeamId: TeamId? = null,
    val entities: List<FollowedEntity> = emptyList(),
) {
    val mainTeam: FollowedEntity.Team?
        get() = entities.filterIsInstance<FollowedEntity.Team>()
            .firstOrNull { it.team.id == mainTeamId }
}

data class EntitySearchResults(
    val teams: List<FollowedEntity.Team>,
    val players: List<FollowedEntity.Player>,
)

data class TeamCirclePost(
    val id: ArticleId,
    val content: String,
    val authorName: String,
    val authorAvatarUrl: String?,
    val createdAtLabel: String?,
    val thumbnailUrls: List<String>,
    val replyCount: Int?,
    val likeCount: Int?,
)
