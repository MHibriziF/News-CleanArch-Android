package com.mhibrizif.news.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mhibrizif.news.data.local.entity.ArticleEntity

@Dao
interface ArticleDao {

    @Query("SELECT * FROM articles WHERE category = :category ORDER BY publishedAt DESC")
    suspend fun getArticlesByCategory(category: String): List<ArticleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

    @Query("DELETE FROM articles WHERE category = :category")
    suspend fun deleteArticlesByCategory(category: String)
}
