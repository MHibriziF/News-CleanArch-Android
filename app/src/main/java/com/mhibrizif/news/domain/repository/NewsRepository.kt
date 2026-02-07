package com.mhibrizif.news.domain.repository

import com.mhibrizif.news.domain.model.Article

interface NewsRepository {
    suspend fun getTopHeadlines(country: String = "us"): Result<List<Article>>
    suspend fun searchNews(
        query: String,
        from: String? = null,
        to: String? = null,
        sortBy: String = "publishedAt"
    ): Result<List<Article>>
}
