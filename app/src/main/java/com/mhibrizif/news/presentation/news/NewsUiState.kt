package com.mhibrizif.news.presentation.news

import com.mhibrizif.news.domain.model.Article

data class NewsUiState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)
