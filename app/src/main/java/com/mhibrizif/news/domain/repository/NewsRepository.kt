package com.mhibrizif.news.domain.repository

import com.mhibrizif.news.domain.model.Article

interface NewsRepository {
    suspend fun getTopHeadlines(
        country: String = "us",
        page: Int = 1,
        pageSize: Int = 20
    ): Result<List<Article>>
    suspend fun searchNews(
        query: String,
        from: String? = null,
        to: String? = null,
        sortBy: String = "publishedAt",
        page: Int = 1,
        pageSize: Int = 20
    ): Result<List<Article>>
}
