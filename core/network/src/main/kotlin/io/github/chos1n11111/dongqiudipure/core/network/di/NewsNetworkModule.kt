package io.github.chos1n11111.dongqiudipure.core.network.di

import android.content.Context
import android.os.Build
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.chos1n11111.dongqiudipure.core.network.NewsRemoteDataSource
import io.github.chos1n11111.dongqiudipure.core.network.FootballRemoteDataSource
import io.github.chos1n11111.dongqiudipure.core.network.AuthRemoteDataSource
import io.github.chos1n11111.dongqiudipure.core.network.DqdClientProfile
import io.github.chos1n11111.dongqiudipure.core.network.OkHttpNewsRemoteDataSource
import io.github.chos1n11111.dongqiudipure.core.network.OkHttpFootballRemoteDataSource
import io.github.chos1n11111.dongqiudipure.core.network.OkHttpAuthRemoteDataSource
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

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthClient

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

    @Binds
    @Singleton
    abstract fun bindAuthRemoteDataSource(
        implementation: OkHttpAuthRemoteDataSource,
    ): AuthRemoteDataSource

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
        fun provideOkHttpClient(clientProfile: DqdClientProfile): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(25, TimeUnit.SECONDS)
            .followRedirects(false)
            .followSslRedirects(false)
            .retryOnConnectionFailure(true)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("User-Agent", clientProfile.userAgent)
                    .build()
                chain.proceed(request)
            }
            .build()

        /**
         * Login and authenticated reads use a dedicated client so credentials can never be
         * attached to the anonymous news and football request pipeline.
         */
        @Provides
        @Singleton
        @AuthClient
        fun provideAuthOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(25, TimeUnit.SECONDS)
            .followRedirects(false)
            .followSslRedirects(false)
            .retryOnConnectionFailure(false)
            .build()

        @Provides
        @Singleton
        @Suppress("DEPRECATION")
        fun provideDqdClientProfile(
            @ApplicationContext context: Context,
        ): DqdClientProfile {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }
            val versionName = packageInfo.versionName?.takeIf(String::isNotBlank) ?: "unknown"

            return DqdClientProfile(
                userAgent = buildString {
                    append("DongqiudiPure-Android/")
                    append(versionName)
                    append(" Android/")
                    append(Build.VERSION.RELEASE)
                    append(" SDK/")
                    append(Build.VERSION.SDK_INT)
                    append(" VersionCode/")
                    append(versionCode)
                },
            )
        }

        @Provides
        @ApiBaseUrl
        fun provideApiBaseUrl(): HttpUrl = "https://api.dongqiudi.com/".toHttpUrl()

        @Provides
        @SportDataBaseUrl
        fun provideSportDataBaseUrl(): HttpUrl =
            "https://sport-data.dongqiudi.com/".toHttpUrl()
    }
}
