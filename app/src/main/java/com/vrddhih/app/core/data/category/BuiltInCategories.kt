package com.vrddhih.app.core.data.category

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.vrddhih.app.core.data.local.entity.CategoryEntity

/**
 * Stable built-in category definitions. These are seeded once on first DB open and never duplicated.
 * IDs are prefixed "builtin_" and must never change — they are stored as foreign keys in quest rows.
 *
 * Colors are chosen to be readable against the dark navy (0xFF080E1C) background of the app.
 * Icon keys map to [com.vrddhih.app.core.ui.components.CategoryIcons.byKey].
 */
object BuiltInCategories {

    val ALL: List<CategoryEntity> = listOf(
        cat("builtin_body",    "Body",    "person",           0xFFFF72C6),
        cat("builtin_mind",    "Mind",    "self_improvement", 0xFFB388FF),
        cat("builtin_work",    "Work",    "work",             0xFF5795FF),
        cat("builtin_fitness", "Fitness", "fitness_center",   0xFF59F581),
        cat("builtin_sleep",   "Sleep",   "bedtime",          0xFF38E5E5),
        cat("builtin_health",  "Health",  "favorite",         0xFFFF7777),
    ).mapIndexed { index, entity -> entity.copy(sortOrder = index) }

    private fun cat(id: String, name: String, iconKey: String, colorValue: Long) = CategoryEntity(
        id = id,
        name = name,
        iconKey = iconKey,
        colorArgb = Color(colorValue).toArgb(),
        isBuiltIn = true,
    )
}
