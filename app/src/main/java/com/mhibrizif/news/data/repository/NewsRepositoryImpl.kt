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

    override suspend fun getTopHeadlines(
        country: String,
        page: Int,
        pageSize: Int
    ): Result<List<Article>> {
        return try {
            val response = api.getTopHeadlines(country, page, pageSize)
            val dtoArticles = response.articles.orEmpty()
            val articles = dtoArticles.map { it.toDomain() }

            if (page == 1) {
                dao.deleteArticlesByCategory(CATEGORY_TOP_HEADLINES)
            }
            dao.insertArticles(dtoArticles.map { it.toEntity(CATEGORY_TOP_HEADLINES) })

            Result.success(articles)
        } catch (e: Exception) {
            if (page == 1) {
                val cached = dao.getArticlesByCategory(CATEGORY_TOP_HEADLINES)
                if (cached.isNotEmpty()) {
                    return Result.success(cached.map { it.toDomain() })
                }
            }
            Result.failure(e)
        }
    }

    override suspend fun searchNews(
        query: String,
        from: String?,
        to: String?,
        sortBy: String,
        page: Int,
        pageSize: Int
    ): Result<List<Article>> {
        return try {
            val response = api.searchNews(query, from, to, sortBy, page, pageSize)
            Result.success(response.articles.orEmpty().map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val CATEGORY_TOP_HEADLINES = "top_headlines"
    }
}
