package com.stark.podtrail.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun provideFeedParser(client: OkHttpClient): com.stark.podtrail.network.FeedParser {
        return com.stark.podtrail.network.FeedParser(client)
    }

    @Provides
    @Singleton
    fun provideItunesPodcastSearcher(
        client: OkHttpClient,
        gson: Gson
    ): com.stark.podtrail.network.ItunesPodcastSearcher {
        return com.stark.podtrail.network.ItunesPodcastSearcher(client, gson)
    }
}
