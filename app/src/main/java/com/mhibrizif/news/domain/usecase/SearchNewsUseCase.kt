package com.mhibrizif.news.domain.usecase

import com.mhibrizif.news.domain.model.Article
import com.mhibrizif.news.domain.repository.NewsRepository
import javax.inject.Inject

class SearchNewsUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    suspend operator fun invoke(
        query: String,
        from: String? = null,
        to: String? = null,
        sortBy: String = "publishedAt"
    ): Result<List<Article>> {
        return repository.searchNews(query, from, to, sortBy)
    }
}
