package com.mhibrizif.news.data.remote

import com.mhibrizif.news.data.remote.dto.NewsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us"
    ): NewsResponseDto

    @GET("everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("sortBy") sortBy: String = "publishedAt"
    ): NewsResponseDto
}
