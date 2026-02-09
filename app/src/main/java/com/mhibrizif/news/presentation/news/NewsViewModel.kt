package com.mhibrizif.news.presentation.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhibrizif.news.domain.usecase.GetIndonesiaNewsUseCase
import com.mhibrizif.news.domain.usecase.GetTopHeadlinesUseCase
import com.mhibrizif.news.domain.usecase.SearchNewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val getTopHeadlinesUseCase: GetTopHeadlinesUseCase,
    private val getIndonesiaNewsUseCase: GetIndonesiaNewsUseCase,
    private val searchNewsUseCase: SearchNewsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    private var baseHeadlineArticles: List<com.mhibrizif.news.domain.model.Article> = emptyList()
    private var baseIndonesiaArticles: List<com.mhibrizif.news.domain.model.Article> = emptyList()

    init {
        loadTopHeadlines()
        loadIndonesiaNews()
    }

    fun loadTopHeadlines() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isHeadlinesLoading = true,
                    headlinesError = null,
                    headlinePage = 1,
                    hasMoreHeadlines = true
                )
            }
            getTopHeadlinesUseCase(page = 1)
                .onSuccess { articles ->
                    baseHeadlineArticles = articles
                    _uiState.update {
                        it.copy(
                            headlineArticles = articles,
                            isHeadlinesLoading = false,
                            headlinePage = 1,
                            hasMoreHeadlines = articles.size >= 20
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            headlinesError = e.message ?: "Unknown error",
                            isHeadlinesLoading = false
                        )
                    }
                }
        }
    }

    fun loadMoreHeadlines() {
        val currentState = _uiState.value
        if (currentState.isLoadingMoreHeadlines || currentState.headlineArticles.isEmpty()) return

        if (!currentState.hasMoreHeadlines) {
            // API exhausted — recycle existing articles for infinite scroll
            _uiState.update {
                it.copy(headlineArticles = it.headlineArticles + baseHeadlineArticles)
            }
            return
        }

        val nextPage = currentState.headlinePage + 1
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMoreHeadlines = true) }
            getTopHeadlinesUseCase(page = nextPage)
                .onSuccess { articles ->
                    _uiState.update {
                        it.copy(
                            headlineArticles = it.headlineArticles + articles,
                            isLoadingMoreHeadlines = false,
                            headlinePage = nextPage,
                            hasMoreHeadlines = articles.size >= 20
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoadingMoreHeadlines = false) }
                }
        }
    }

    private fun loadIndonesiaNews() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isIndonesiaLoading = true,
                    indonesiaError = null,
                    indonesiaPage = 1,
                    hasMoreIndonesia = true
                )
            }
            getIndonesiaNewsUseCase(page = 1)
                .onSuccess { articles ->
                    baseIndonesiaArticles = articles
                    _uiState.update {
                        it.copy(
                            indonesiaArticles = articles,
                            isIndonesiaLoading = false,
                            indonesiaPage = 1,
                            hasMoreIndonesia = articles.size >= 20
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            indonesiaError = e.message ?: "Unknown error",
                            isIndonesiaLoading = false
                        )
                    }
                }
        }
    }

    fun loadMoreIndonesiaNews() {
        val currentState = _uiState.value
        if (currentState.isLoadingMoreIndonesia || currentState.indonesiaArticles.isEmpty()) return

        if (!currentState.hasMoreIndonesia) {
            // API exhausted — recycle existing articles for infinite scroll
            _uiState.update {
                it.copy(indonesiaArticles = it.indonesiaArticles + baseIndonesiaArticles)
            }
            return
        }

        val nextPage = currentState.indonesiaPage + 1
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMoreIndonesia = true) }
            getIndonesiaNewsUseCase(page = nextPage)
                .onSuccess { articles ->
                    _uiState.update {
                        it.copy(
                            indonesiaArticles = it.indonesiaArticles + articles,
                            isLoadingMoreIndonesia = false,
                            indonesiaPage = nextPage,
                            hasMoreIndonesia = articles.size >= 20
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoadingMoreIndonesia = false) }
                }
        }
    }

    fun searchNews(query: String) {
        if (query.isBlank()) {
            clearSearch()
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSearchActive = true, isSearchLoading = true, searchError = null) }
            searchNewsUseCase(query)
                .onSuccess { articles ->
                    _uiState.update {
                        it.copy(searchResults = articles, isSearchLoading = false)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            searchError = e.message ?: "Unknown error",
                            isSearchLoading = false
                        )
                    }
                }
        }
    }

    fun clearSearch() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                isSearchActive = false,
                searchResults = emptyList(),
                searchError = null
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
