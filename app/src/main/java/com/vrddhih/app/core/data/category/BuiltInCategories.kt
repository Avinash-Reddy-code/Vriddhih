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
        cat("builtin_body",           "Body",             "person",                 0xFFFF6B8B),
        cat("builtin_mind",           "Mind",             "self_improvement",       0xFFBA68C8),
        cat("builtin_work",           "Work",             "work",                   0xFF3D82F6),
        cat("builtin_study",          "Study",            "school",                 0xFF7986CB),
        cat("builtin_coding",         "Coding",           "code",                   0xFF00E5FF),
        cat("builtin_home",           "Home",             "home",                   0xFFFFA726),
        cat("builtin_fitness",        "Fitness",          "fitness_center",         0xFF00E676),
        cat("builtin_food",           "Food",             "local_dining",           0xFFFF7043),
        cat("builtin_sleep",          "Sleep",            "bedtime",                0xFF9575CD),
        cat("builtin_money",          "Money",            "account_balance_wallet", 0xFFFFD700),
        cat("builtin_social",         "Social",           "people",                 0xFFFF4081),
        cat("builtin_family",         "Family",           "family_restroom",        0xFFFF8A80),
        cat("builtin_selfcare",       "Self-care",        "spa",                    0xFF26A69A),
        cat("builtin_errands",        "Errands",          "shopping_cart",          0xFFCE93D8),
        cat("builtin_other",          "Other",            "more_horiz",             0xFF90A4AE),
    ).mapIndexed { index, entity -> entity.copy(sortOrder = index) }

    private fun cat(id: String, name: String, iconKey: String, colorValue: Long) = CategoryEntity(
        id = id,
        name = name,
        iconKey = iconKey,
        colorArgb = Color(colorValue).toArgb(),
        isBuiltIn = true,
    )
}
