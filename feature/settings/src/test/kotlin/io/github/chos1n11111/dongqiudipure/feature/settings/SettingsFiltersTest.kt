package io.github.chos1n11111.dongqiudipure.feature.settings

import io.github.chos1n11111.dongqiudipure.core.model.CompetitionCatalogGroup
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef
import io.github.chos1n11111.dongqiudipure.core.model.NewsCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsFiltersTest {

    @Test
    fun `competition search matches an option name without flattening groups`() {
        val groups = listOf(
            group("欧洲", competition("3", "英超"), competition("5", "西甲")),
            group("亚洲", competition("56", "中超")),
        )

        val result = filterCompetitionGroups(groups, "英超")

        assertEquals(listOf("欧洲"), result.map { it.name })
        assertEquals(listOf("英超"), result.single().competitions.map { it.name })
    }

    @Test
    fun `competition search keeps a whole matching group`() {
        val groups = listOf(
            group("欧洲", competition("3", "英超"), competition("5", "西甲")),
            group("亚洲", competition("56", "中超")),
        )

        val result = filterCompetitionGroups(groups, "欧洲")

        assertEquals(listOf("英超", "西甲"), result.single().competitions.map { it.name })
    }

    @Test
    fun `news search trims input and matches labels`() {
        val categories = listOf(
            NewsCategory("1", "头条"),
            NewsCategory("3", "英超"),
        )

        assertEquals(
            listOf("3"),
            filterNewsCategories(categories, "  英超 ").map { it.id },
        )
    }

    private fun group(
        name: String,
        vararg competitions: CompetitionRef,
    ) = CompetitionCatalogGroup(name, competitions.toList())

    private fun competition(id: String, name: String) = CompetitionRef(
        id = CompetitionId(id),
        name = name,
        roundLabel = null,
    )
}
