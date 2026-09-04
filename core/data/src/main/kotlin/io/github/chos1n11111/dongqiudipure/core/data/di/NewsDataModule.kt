package io.github.chos1n11111.dongqiudipure.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.chos1n11111.dongqiudipure.core.data.ArticleRepository
import io.github.chos1n11111.dongqiudipure.core.data.DefaultNewsRepository
import io.github.chos1n11111.dongqiudipure.core.data.DefaultSessionRepository
import io.github.chos1n11111.dongqiudipure.core.data.DeviceIdStore
import io.github.chos1n11111.dongqiudipure.core.data.DefaultFootballRepository
import io.github.chos1n11111.dongqiudipure.core.data.FootballCatalogRepository
import io.github.chos1n11111.dongqiudipure.core.data.FootballEntityRepository
import io.github.chos1n11111.dongqiudipure.core.data.MatchRepository
import io.github.chos1n11111.dongqiudipure.core.data.NewsRepository
import io.github.chos1n11111.dongqiudipure.core.data.KeystoreSessionStore
import io.github.chos1n11111.dongqiudipure.core.data.SessionRepository
import io.github.chos1n11111.dongqiudipure.core.data.SessionStore
import io.github.chos1n11111.dongqiudipure.core.data.SharedPreferencesDeviceIdStore
import io.github.chos1n11111.dongqiudipure.core.data.StandingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NewsDataModule {

    @Binds
    @Singleton
    abstract fun bindNewsRepository(
        implementation: DefaultNewsRepository,
    ): NewsRepository

    @Binds
    @Singleton
    abstract fun bindArticleRepository(
        implementation: DefaultNewsRepository,
    ): ArticleRepository

    @Binds
    @Singleton
    abstract fun bindMatchRepository(
        implementation: DefaultFootballRepository,
    ): MatchRepository

    @Binds
    @Singleton
    abstract fun bindStandingsRepository(
        implementation: DefaultFootballRepository,
    ): StandingsRepository

    @Binds
    @Singleton
    abstract fun bindFootballCatalogRepository(
        implementation: DefaultFootballRepository,
    ): FootballCatalogRepository

    @Binds
    @Singleton
    abstract fun bindFootballEntityRepository(
        implementation: DefaultFootballRepository,
    ): FootballEntityRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        implementation: DefaultSessionRepository,
    ): SessionRepository

    @Binds
    @Singleton
    internal abstract fun bindSessionStore(
        implementation: KeystoreSessionStore,
    ): SessionStore

    @Binds
    @Singleton
    internal abstract fun bindDeviceIdStore(
        implementation: SharedPreferencesDeviceIdStore,
    ): DeviceIdStore
}
