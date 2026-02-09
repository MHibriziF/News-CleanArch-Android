package com.mhibrizif.news.domain.usecase

import com.mhibrizif.news.domain.model.Article
import com.mhibrizif.news.domain.repository.NewsRepository
import javax.inject.Inject

class GetIndonesiaNewsUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    suspend operator fun invoke(
        page: Int = 1,
        pageSize: Int = 20
    ): Result<List<Article>> {
        return repository.searchNews(query = "Indonesia", page = page, pageSize = pageSize)
    }
}
