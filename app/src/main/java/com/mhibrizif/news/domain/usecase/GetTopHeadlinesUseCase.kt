package com.mhibrizif.news.domain.usecase

import com.mhibrizif.news.domain.model.Article
import com.mhibrizif.news.domain.repository.NewsRepository
import javax.inject.Inject

class GetTopHeadlinesUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    suspend operator fun invoke(country: String = "us"): Result<List<Article>> {
        return repository.getTopHeadlines(country)
    }
}
