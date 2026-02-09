package com.mhibrizif.news.presentation.news

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mhibrizif.news.presentation.news.components.ArticleCard
import com.mhibrizif.news.presentation.news.components.HeadlineCard

@Composable
private fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    viewModel: NewsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val headlineListState = rememberLazyListState()
    val mainListState = rememberLazyListState()

    val isScrollingUp = mainListState.isScrollingUp()
    val atTop = !mainListState.canScrollBackward

    // Trigger loading more headlines when near the end
    val shouldLoadMoreHeadlines by remember {
        derivedStateOf {
            val lastVisibleIndex = headlineListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = headlineListState.layoutInfo.totalItemsCount
            lastVisibleIndex >= totalItems - 3 && totalItems > 0
        }
    }

    // Trigger loading more Indonesia news when near the end
    val shouldLoadMoreIndonesia by remember {
        derivedStateOf {
            val lastVisibleIndex = mainListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = mainListState.layoutInfo.totalItemsCount
            lastVisibleIndex >= totalItems - 3 && totalItems > 0
        }
    }

    LaunchedEffect(shouldLoadMoreHeadlines) {
        if (shouldLoadMoreHeadlines) {
            viewModel.loadMoreHeadlines()
        }
    }

    LaunchedEffect(shouldLoadMoreIndonesia) {
        if (shouldLoadMoreIndonesia) {
            viewModel.loadMoreIndonesiaNews()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("News") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                state = mainListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 72.dp)
            ) {
                if (uiState.isSearchActive) {
                    // ── Search results mode ──
                    item {
                        Text(
                            text = "Search Results",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    when {
                        uiState.isSearchLoading -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        uiState.searchError != null -> {
                            item {
                                ErrorContent(
                                    error = uiState.searchError!!,
                                    onRetry = { viewModel.searchNews(uiState.searchQuery) }
                                )
                            }
                        }

                        uiState.searchResults.isEmpty() -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No results found",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        else -> {
                            items(
                                count = uiState.searchResults.size,
                                key = { index -> "search_${index}_${uiState.searchResults[index].url}" }
                            ) { index ->
                                val article = uiState.searchResults[index]
                                ArticleCard(
                                    article = article,
                                    onClick = {
                                        if (article.url.isNotBlank()) {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                                            context.startActivity(intent)
                                        }
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                } else {
                    // ── Normal mode: Headlines + Indonesia ──

                    // Top Headlines section label
                    item {
                        Text(
                            text = "Top Headlines",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // Top Headlines horizontal list
                    item {
                        when {
                            uiState.isHeadlinesLoading && uiState.headlineArticles.isEmpty() -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }

                            uiState.headlinesError != null && uiState.headlineArticles.isEmpty() -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = uiState.headlinesError!!,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        TextButton(onClick = { viewModel.loadTopHeadlines() }) {
                                            Text("Retry")
                                        }
                                    }
                                }
                            }

                            uiState.headlineArticles.isNotEmpty() -> {
                                LazyRow(
                                    state = headlineListState,
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(
                                        count = uiState.headlineArticles.size,
                                        key = { index -> "headline_${index}_${uiState.headlineArticles[index].url}" }
                                    ) { index ->
                                        val article = uiState.headlineArticles[index]
                                        HeadlineCard(
                                            article = article,
                                            onClick = {
                                                if (article.url.isNotBlank()) {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                                                    context.startActivity(intent)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Indonesia section label
                    item {
                        Text(
                            text = "Indonesia",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // Indonesia news section
                    when {
                        uiState.isIndonesiaLoading -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        uiState.indonesiaError != null -> {
                            item {
                                ErrorContent(
                                    error = uiState.indonesiaError!!,
                                    onRetry = { viewModel.loadTopHeadlines() }
                                )
                            }
                        }

                        uiState.indonesiaArticles.isEmpty() && !uiState.isIndonesiaLoading -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No articles found",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        else -> {
                            items(
                                count = uiState.indonesiaArticles.size,
                                key = { index -> "indonesia_${index}_${uiState.indonesiaArticles[index].url}" }
                            ) { index ->
                                val article = uiState.indonesiaArticles[index]
                                ArticleCard(
                                    article = article,
                                    onClick = {
                                        if (article.url.isNotBlank()) {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                                            context.startActivity(intent)
                                        }
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Search bar overlay — visible at top or when scrolling up
            AnimatedVisibility(
                visible = isScrollingUp || atTop,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Search news...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.clearSearch() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                viewModel.searchNews(uiState.searchQuery)
                                keyboardController?.hide()
                            }
                        ),
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
