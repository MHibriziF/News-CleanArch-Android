package com.mhibrizif.news.presentation.news

import com.mhibrizif.news.domain.model.Article

data class NewsUiState(
    val headlineArticles: List<Article> = emptyList(),
    val indonesiaArticles: List<Article> = emptyList(),
    val searchResults: List<Article> = emptyList(),
    val isHeadlinesLoading: Boolean = false,
    val isIndonesiaLoading: Boolean = false,
    val isSearchLoading: Boolean = false,
    val isLoadingMoreHeadlines: Boolean = false,
    val isLoadingMoreIndonesia: Boolean = false,
    val headlinesError: String? = null,
    val indonesiaError: String? = null,
    val searchError: String? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val headlinePage: Int = 1,
    val hasMoreHeadlines: Boolean = true,
    val indonesiaPage: Int = 1,
    val hasMoreIndonesia: Boolean = true
)
