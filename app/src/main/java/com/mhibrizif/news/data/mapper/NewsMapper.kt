package com.mhibrizif.news.data.mapper

import com.mhibrizif.news.data.local.entity.ArticleEntity
import com.mhibrizif.news.data.remote.dto.ArticleDto
import com.mhibrizif.news.domain.model.Article

fun ArticleDto.toDomain(): Article {
    return Article(
        sourceId = source?.id ?: "",
        sourceName = source?.name ?: "",
        author = author ?: "",
        title = title ?: "",
        description = description ?: "",
        url = url ?: "",
        urlToImage = urlToImage ?: "",
        publishedAt = publishedAt ?: "",
        content = content ?: ""
    )
}

fun ArticleDto.toEntity(category: String): ArticleEntity {
    return ArticleEntity(
        sourceId = source?.id ?: "",
        sourceName = source?.name ?: "",
        author = author ?: "",
        title = title ?: "",
        description = description ?: "",
        url = url ?: "",
        urlToImage = urlToImage ?: "",
        publishedAt = publishedAt ?: "",
        content = content ?: "",
        category = category
    )
}

fun ArticleEntity.toDomain(): Article {
    return Article(
        sourceId = sourceId,
        sourceName = sourceName,
        author = author,
        title = title,
        description = description,
        url = url,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        content = content
    )
}
