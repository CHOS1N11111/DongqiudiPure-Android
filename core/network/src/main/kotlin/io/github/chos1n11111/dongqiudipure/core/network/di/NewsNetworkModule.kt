package io.github.chos1n11111.dongqiudipure.core.network.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.chos1n11111.dongqiudipure.core.network.NewsRemoteDataSource
import io.github.chos1n11111.dongqiudipure.core.network.FootballRemoteDataSource
import io.github.chos1n11111.dongqiudipure.core.network.OkHttpNewsRemoteDataSource
import io.github.chos1n11111.dongqiudipure.core.network.OkHttpFootballRemoteDataSource
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SportDataBaseUrl

@Module
@InstallIn(SingletonComponent::class)
abstract class NewsNetworkModule {

    @Binds
    @Singleton
    abstract fun bindNewsRemoteDataSource(
        implementation: OkHttpNewsRemoteDataSource,
    ): NewsRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindFootballRemoteDataSource(
        implementation: OkHttpFootballRemoteDataSource,
    ): FootballRemoteDataSource

    companion object {
        @Provides
        @Singleton
        fun provideJson(): Json = Json {
            ignoreUnknownKeys = true
            isLenient = false
            explicitNulls = false
            coerceInputValues = false
        }

        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(25, TimeUnit.SECONDS)
            .followRedirects(false)
            .followSslRedirects(false)
            .retryOnConnectionFailure(true)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("User-Agent", "DongqiudiPure-Android/0.1")
                    .build()
                chain.proceed(request)
            }
            .build()

        @Provides
        @ApiBaseUrl
        fun provideApiBaseUrl(): HttpUrl = "https://api.dongqiudi.com/".toHttpUrl()

        @Provides
        @SportDataBaseUrl
        fun provideSportDataBaseUrl(): HttpUrl =
            "https://sport-data.dongqiudi.com/".toHttpUrl()
    }
}
