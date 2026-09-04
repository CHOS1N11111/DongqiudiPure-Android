package io.github.chos1n11111.dongqiudipure.feature.settings

import io.github.chos1n11111.dongqiudipure.core.model.FollowedEntity
import io.github.chos1n11111.dongqiudipure.core.model.FollowedEntityPreferences
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerRef
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.TeamRef
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FollowedEntityPreferencesTest {

    @Test
    fun `missing storage starts with no team or followed entity`() {
        val decoded = decodeFollowedEntities(null)

        assertEquals(null, decoded.mainTeamId)
        assertTrue(decoded.entities.isEmpty())
    }

    @Test
    fun `main team and followed player survive local storage round trip`() {
        val teamId = TeamId("50000513")
        val preferences = FollowedEntityPreferences(
            mainTeamId = teamId,
            entities = listOf(
                FollowedEntity.Team(
                    TeamRef(
                        id = teamId,
                        name = "阿森纳",
                        crestUrl = "https://sd.qunliao.info/arsenal.png",
                    ),
                    secondaryLabel = "英格兰",
                ),
                FollowedEntity.Player(
                    PlayerRef(
                        id = PlayerId("50000116"),
                        name = "梅西",
                        avatarUrl = "https://sd.qunliao.info/messi.png",
                    ),
                    secondaryLabel = "前锋 · 迈阿密国际",
                ),
            ),
        )

        val decoded = decodeFollowedEntities(encodeFollowedEntities(preferences))

        assertEquals(preferences, decoded)
        assertEquals("阿森纳", decoded.mainTeam?.name)
    }
}
