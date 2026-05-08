package com.antigravity.aura.di

import com.antigravity.aura.network.SpotifyApi
import com.antigravity.aura.network.SpotifyAuthApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSpotifyAuthApi(): SpotifyAuthApi {
        return Retrofit.Builder()
            .baseUrl("https://accounts.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpotifyAuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSpotifyApi(): SpotifyApi {
        return Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpotifyApi::class.java)
    }

    @Provides
    @Singleton
    fun provideYouTubeDataApi(): com.antigravity.aura.network.YouTubeDataApi {
        return Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/youtube/v3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(com.antigravity.aura.network.YouTubeDataApi::class.java)
    }
}
