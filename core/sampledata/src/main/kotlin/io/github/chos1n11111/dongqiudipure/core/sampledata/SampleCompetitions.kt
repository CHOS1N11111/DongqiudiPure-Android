package io.github.chos1n11111.dongqiudipure.core.sampledata

import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef

/**
 * 「数据」tab 的赛事列表示例数据。
 *
 * ⚠️ 真实实现**不得写死这份名单**。
 * FEATURES.md 要求「主要/热门」集合由 M2 从官方匿名默认入口归档，
 * 「M6/M7 不在代码中写死永久名单」。这里只是让切换器在开发期有内容。
 *
 * 默认选中项同理：应取服务端返回的默认/首个赛事，不是客户端偏好。
 */
object SampleCompetitions {

    val all: List<CompetitionRef> = listOf(
        CompetitionRef(CompetitionId("c-epl"), "英超", null),
        CompetitionRef(CompetitionId("c-laliga"), "西甲", null),
        CompetitionRef(CompetitionId("c-seriea"), "意甲", null),
        CompetitionRef(CompetitionId("c-bundesliga"), "德甲", null),
        CompetitionRef(CompetitionId("c-ligue1"), "法甲", null),
        CompetitionRef(CompetitionId("c-ucl"), "欧冠", null),
        CompetitionRef(CompetitionId("c-csl"), "中超", null),
    )

    /** 开发期的默认选中项。真实实现由服务端决定，不在客户端硬编码。 */
    val default: CompetitionRef = all.first()
}
