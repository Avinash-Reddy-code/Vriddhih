package com.vrddhih.app.core.data.repository

import androidx.room.withTransaction
import com.vrddhih.app.core.data.category.BuiltInCategories
import com.vrddhih.app.core.data.local.VrddhihDatabase
import com.vrddhih.app.core.data.local.entity.CATEGORY_OTHER_ID
import com.vrddhih.app.core.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Manages task categories. Built-in categories are seeded idempotently via [seedBuiltIns].
 *
 * Deletion policy: before removing a custom category, all quests in that category are transactionally
 * reassigned to the "Other" built-in. This keeps every quest valid without deleting any history.
 */
class CategoryRepository(private val db: VrddhihDatabase) {

    private val dao = db.categoryDao()

    fun observeAll(): Flow<List<CategoryEntity>> = dao.observeAll()

    suspend fun getAll(): List<CategoryEntity> = dao.getAll()

    suspend fun getById(id: String): CategoryEntity? = dao.getById(id)

    /**
     * Seeds built-in categories idempotently. Reconciles legacy built-in category IDs from earlier builds,
     * reassigns any existing quests referencing them, upserts the current 15 categories, and cleans up obsolete built-ins.
     */
    suspend fun seedBuiltIns() {
        db.withTransaction {
            val legacyRemappings = mapOf(
                "builtin_personal" to "builtin_body",
                "builtin_health" to "builtin_fitness",
                "builtin_habits" to "builtin_mind",
                "builtin_finance" to "builtin_money",
                "builtin_reading" to "builtin_study",
                "builtin_appointments" to "builtin_errands",
            )
            legacyRemappings.forEach { (oldId, newId) ->
                dao.reassignQuests(fromId = oldId, targetCategoryId = newId)
                dao.deleteBuiltIn(oldId)
            }
            BuiltInCategories.ALL.forEach { dao.upsert(it) }
        }
    }

    /**
     * Creates a new custom category. Validates that the name is non-blank and not already taken
     * (case-insensitive). Returns the saved entity or null if the name is a duplicate.
     */
    suspend fun createCustom(name: String, iconKey: String, colorArgb: Int): CategoryEntity? {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return null
        val existing = dao.getAll()
        if (existing.any { it.name.equals(trimmed, ignoreCase = true) }) return null
        val id = "custom_${System.currentTimeMillis()}"
        val maxOrder = existing.maxOfOrNull { it.sortOrder } ?: 0
        val entity = CategoryEntity(
            id = id,
            name = trimmed,
            iconKey = iconKey,
            colorArgb = colorArgb,
            isBuiltIn = false,
            sortOrder = maxOrder + 1,
        )
        dao.insert(entity)
        return entity
    }

    /**
     * Renames or re-icons a custom category. Built-in categories are immutable; call is a no-op if
     * [category.isBuiltIn] is true. Returns false if the new name is taken or the category is built-in.
     */
    suspend fun updateCustom(category: CategoryEntity, newName: String, newIconKey: String, newColorArgb: Int): Boolean {
        if (category.isBuiltIn) return false
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return false
        val existing = dao.getAll()
        val nameTaken = existing.any { it.id != category.id && it.name.equals(trimmed, ignoreCase = true) }
        if (nameTaken) return false
        dao.update(category.copy(name = trimmed, iconKey = newIconKey, colorArgb = newColorArgb))
        return true
    }

    /**
     * Deletes a custom category. All quests in [category] are transactionally reassigned to "Other".
     * Built-in categories cannot be deleted; returns false in that case.
     */
    suspend fun deleteCustom(category: CategoryEntity): Boolean {
        if (category.isBuiltIn) return false
        db.withTransaction {
            dao.reassignQuests(fromId = category.id, targetCategoryId = CATEGORY_OTHER_ID)
            dao.delete(category)
        }
        return true
    }
}
