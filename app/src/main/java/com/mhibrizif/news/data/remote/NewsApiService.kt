package com.mhibrizif.news.data.remote

import com.mhibrizif.news.data.remote.dto.NewsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): NewsResponseDto

    @GET("everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): NewsResponseDto
}
