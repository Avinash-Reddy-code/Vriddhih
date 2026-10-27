package com.vrddhih.app.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A task category. Built-in categories have [isBuiltIn] = true and cannot be renamed or deleted.
 * Custom categories may be mutated freely. [sortOrder] controls display order in pickers.
 *
 * [iconKey] is a stable string identifier that maps to a Material icon in [com.vrddhih.app.core.ui.components.CategoryIcons].
 * [colorArgb] is stored as an ARGB integer and always represents the full-opacity accent color
 * regardless of the current device display mode.
 */
@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)],
)
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "icon_key") val iconKey: String,
    @ColumnInfo(name = "color_argb") val colorArgb: Int,
    @ColumnInfo(name = "is_built_in", defaultValue = "0") val isBuiltIn: Boolean = false,
    @ColumnInfo(name = "sort_order", defaultValue = "0") val sortOrder: Int = 0,
)
