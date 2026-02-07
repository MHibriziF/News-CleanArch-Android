package com.mhibrizif.news.data.repository

import com.mhibrizif.news.data.local.dao.ArticleDao
import com.mhibrizif.news.data.mapper.toDomain
import com.mhibrizif.news.data.mapper.toEntity
import com.mhibrizif.news.data.remote.NewsApiService
import com.mhibrizif.news.domain.model.Article
import com.mhibrizif.news.domain.repository.NewsRepository
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val api: NewsApiService,
    private val dao: ArticleDao
) : NewsRepository {

    override suspend fun getTopHeadlines(country: String): Result<List<Article>> {
        return try {
            val response = api.getTopHeadlines(country)
            val dtoArticles = response.articles.orEmpty()
            val articles = dtoArticles.map { it.toDomain() }

            // Cache results
            dao.deleteArticlesByCategory(CATEGORY_TOP_HEADLINES)
            dao.insertArticles(dtoArticles.map { it.toEntity(CATEGORY_TOP_HEADLINES) })

            Result.success(articles)
        } catch (e: Exception) {
            // Fall back to cached data
            val cached = dao.getArticlesByCategory(CATEGORY_TOP_HEADLINES)
            if (cached.isNotEmpty()) {
                Result.success(cached.map { it.toDomain() })
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun searchNews(
        query: String,
        from: String?,
        to: String?,
        sortBy: String
    ): Result<List<Article>> {
        return try {
            val response = api.searchNews(query, from, to, sortBy)
            Result.success(response.articles.orEmpty().map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val CATEGORY_TOP_HEADLINES = "top_headlines"
    }
}
