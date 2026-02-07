package com.mhibrizif.news.presentation.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val searchNewsUseCase: SearchNewsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    init {
        loadTopHeadlines()
    }

    fun loadTopHeadlines() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getTopHeadlinesUseCase()
                .onSuccess { articles ->
                    _uiState.update { it.copy(articles = articles, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(error = e.message ?: "Unknown error", isLoading = false)
                    }
                }
        }
    }

    fun searchNews(query: String) {
        if (query.isBlank()) {
            loadTopHeadlines()
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            searchNewsUseCase(query)
                .onSuccess { articles ->
                    _uiState.update { it.copy(articles = articles, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(error = e.message ?: "Unknown error", isLoading = false)
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
