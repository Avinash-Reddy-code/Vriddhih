package com.vrddhih.app.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vrddhih.app.core.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    /** Inserts a category; silently ignores duplicates (idempotent seeding). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(category: CategoryEntity)

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE is_built_in = 1 AND id = :id")
    suspend fun deleteBuiltIn(id: String)

    @Query("SELECT * FROM categories ORDER BY sort_order, name")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sort_order, name")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: String): CategoryEntity?

    /** Reassigns all quests in the given category to [targetCategoryId]. Used before deletion. */
    @Query("UPDATE quests SET category_id = :targetCategoryId WHERE category_id = :fromId")
    suspend fun reassignQuests(fromId: String, targetCategoryId: String)

    @Query(
        """
        SELECT 
            c.id AS categoryId,
            (
                (SELECT COUNT(*) FROM quests q WHERE q.category_id = c.id) +
                (SELECT COUNT(*) FROM habits h WHERE h.category_id = c.id) +
                (SELECT COUNT(*) FROM goals g WHERE g.category_id = c.id)
            ) AS count
        FROM categories c
        """
    )
    fun observeEntryCounts(): Flow<List<CategoryEntryCount>>

    @Query(
        """
        UPDATE categories SET is_built_in = 0 
        WHERE is_built_in = 1 
          AND id NOT IN (:currentIds) 
          AND (
            id IN (SELECT DISTINCT category_id FROM quests WHERE category_id IS NOT NULL)
            OR id IN (SELECT DISTINCT category_id FROM habits WHERE category_id IS NOT NULL)
            OR id IN (SELECT DISTINCT category_id FROM goals WHERE category_id IS NOT NULL)
          )
        """
    )
    suspend fun preserveReferencedObsoleteBuiltIns(currentIds: List<String>)

    @Query("DELETE FROM categories WHERE is_built_in = 1 AND id NOT IN (:currentIds)")
    suspend fun deleteUnreferencedObsoleteBuiltIns(currentIds: List<String>)
}

data class CategoryEntryCount(
    val categoryId: String,
    val count: Int,
)
