package com.vrddhih.app.core.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Categorization of themes for UI organization in the Themes selection screen.
 */
enum class ThemeCategory {
    DEFAULT,
    CORE,
    PREMIUM,
    TWO_TONE,
}

/**
 * Centralized theme configuration defining brand accent colors and independent FAB colors.
 *
 * @param id Unique identifier used for persistence.
 * @param displayName Human-readable theme name.
 * @param primaryAccent Main brand accent replacing Electric Blue across all screens and components.
 * @param secondaryAccent Complementary accent replacing Cyan/Ice across accents, framing, and gradients.
 * @param onAccent Text/content color placed atop primaryAccent surfaces (near-black Void for high contrast).
 * @param fabColor Independent contrasting color strictly assigned to the floating '+' action button.
 * @param fabPressedColor Darkened shade used during active FAB pressed state.
 * @param fabGlowColor Ambient glow color for the floating action button.
 * @param isTwoTone Whether this theme preset features a two-tone outer-ring/inner-core swatch design.
 * @param innerCoreColor Inner core color displayed inside two-tone swatches.
 * @param category Section placement in the Themes screen.
 */
data class VrddhihThemeConfig(
    val id: String,
    val displayName: String,
    val primaryAccent: Color,
    val secondaryAccent: Color = primaryAccent,
    val onAccent: Color = VrddhihColors.Void,
    val fabColor: Color,
    val fabPressedColor: Color = darkenColor(fabColor),
    val fabGlowColor: Color = fabColor,
    val isTwoTone: Boolean = false,
    val innerCoreColor: Color? = null,
    val category: ThemeCategory = ThemeCategory.PREMIUM,
)

private fun darkenColor(color: Color, factor: Float = 0.88f): Color {
    return Color(
        red = (color.red * factor).coerceIn(0f, 1f),
        green = (color.green * factor).coerceIn(0f, 1f),
        blue = (color.blue * factor).coerceIn(0f, 1f),
        alpha = color.alpha,
    )
}

/**
 * Catalog of all curated themes matching the Solo Leveling Hunter System and reference design.
 */
object VrddhihThemes {
    const val DEFAULT_ID = "default_ultra_dark"

    // 1. Default Ultra Dark theme
    val Default = VrddhihThemeConfig(
        id = DEFAULT_ID,
        displayName = "Default Ultra Dark",
        primaryAccent = Color(0xFF5795FF), // Electric Blue
        secondaryAccent = Color(0xFF38E5E5), // Vivid Cyan / Ice
        fabColor = Color(0xFFFF6A00), // Intense Orange
        fabPressedColor = Color(0xFFE85A00),
        category = ThemeCategory.DEFAULT,
    )

    // 2. Core colors (Top row in reference image)
    val CoreRose = VrddhihThemeConfig(
        id = "core_rose",
        displayName = "Rose / Crimson",
        primaryAccent = Color(0xFFD82F5A),
        secondaryAccent = Color(0xFFFF6088),
        fabColor = Color(0xFFFFB000), // Golden Yellow
        category = ThemeCategory.CORE,
    )

    val CoreOrange = VrddhihThemeConfig(
        id = "core_orange",
        displayName = "Orange",
        primaryAccent = Color(0xFFE86D24),
        secondaryAccent = Color(0xFFFFA058),
        fabColor = Color(0xFF5795FF), // Electric Blue
        category = ThemeCategory.CORE,
    )

    val CoreCyan = VrddhihThemeConfig(
        id = "core_cyan",
        displayName = "Cyan / Teal",
        primaryAccent = Color(0xFF0BB0C8),
        secondaryAccent = Color(0xFF38E5E5),
        fabColor = Color(0xFFFF6A00), // Orange
        category = ThemeCategory.CORE,
    )

    val CorePurple = VrddhihThemeConfig(
        id = "core_purple",
        displayName = "Purple",
        primaryAccent = Color(0xFF9E4DE8),
        secondaryAccent = Color(0xFFC17FFF),
        fabColor = Color(0xFFFFC45C), // Golden Yellow
        category = ThemeCategory.CORE,
    )

    // 3. Premium solid accent colors (Rows 1-3 in reference grid)
    val PremiumRed = VrddhihThemeConfig(
        id = "premium_red",
        displayName = "Red",
        primaryAccent = Color(0xFFD9383A),
        secondaryAccent = Color(0xFFFF6B6D),
        fabColor = Color(0xFF38E5E5), // Cyan
    )

    val PremiumCoral = VrddhihThemeConfig(
        id = "premium_coral",
        displayName = "Coral",
        primaryAccent = Color(0xFFE6735C),
        secondaryAccent = Color(0xFFFFA28D),
        fabColor = Color(0xFF007F78), // Deep Teal
    )

    val PremiumAmber = VrddhihThemeConfig(
        id = "premium_amber",
        displayName = "Amber",
        primaryAccent = Color(0xFFE8A027),
        secondaryAccent = Color(0xFFFFC966),
        fabColor = Color(0xFF7048C8), // Royal Purple
    )

    val PremiumGreen = VrddhihThemeConfig(
        id = "premium_green",
        displayName = "Green",
        primaryAccent = Color(0xFF2BB358),
        secondaryAccent = Color(0xFF5EE086),
        fabColor = Color(0xFFFF765E), // Coral
    )

    val PremiumTeal = VrddhihThemeConfig(
        id = "premium_teal",
        displayName = "Teal",
        primaryAccent = Color(0xFF1EAA92),
        secondaryAccent = Color(0xFF4FE0C8),
        fabColor = Color(0xFFFF8A35), // Warm Orange
    )

    val PremiumViolet = VrddhihThemeConfig(
        id = "premium_violet",
        displayName = "Violet",
        primaryAccent = Color(0xFF7B4BD4),
        secondaryAccent = Color(0xFFA57CF5),
        fabColor = Color(0xFFFFC45C), // Golden Yellow
    )

    val PremiumRaspberry = VrddhihThemeConfig(
        id = "premium_raspberry",
        displayName = "Raspberry",
        primaryAccent = Color(0xFFD32F65),
        secondaryAccent = Color(0xFFFF6395),
        fabColor = Color(0xFFFFB300), // Golden Amber
    )

    val PremiumTerracotta = VrddhihThemeConfig(
        id = "premium_terracotta",
        displayName = "Terracotta",
        primaryAccent = Color(0xFFD3593B),
        secondaryAccent = Color(0xFFFF886E),
        fabColor = Color(0xFF5795FF), // Electric Blue
    )

    val PremiumGold = VrddhihThemeConfig(
        id = "premium_gold",
        displayName = "Gold",
        primaryAccent = Color(0xFFC9942E),
        secondaryAccent = Color(0xFFF2C463),
        fabColor = Color(0xFF6541A5), // Deep Violet
    )

    val PremiumEmerald = VrddhihThemeConfig(
        id = "premium_emerald",
        displayName = "Emerald",
        primaryAccent = Color(0xFF249C65),
        secondaryAccent = Color(0xFF58CC96),
        fabColor = Color(0xFFFF8A35), // Warm Orange
    )

    val PremiumSkyBlue = VrddhihThemeConfig(
        id = "premium_sky_blue",
        displayName = "Sky Blue",
        primaryAccent = Color(0xFF2E99D6),
        secondaryAccent = Color(0xFF6EC3F5),
        fabColor = Color(0xFFF07824), // Deep Orange
    )

    val PremiumLavender = VrddhihThemeConfig(
        id = "premium_lavender",
        displayName = "Lavender",
        primaryAccent = Color(0xFF786899),
        secondaryAccent = Color(0xFFA898C8),
        fabColor = Color(0xFF6541A5), // Deep Violet
    )

    val PremiumDustyRose = VrddhihThemeConfig(
        id = "premium_dusty_rose",
        displayName = "Dusty Rose",
        primaryAccent = Color(0xFFC96172),
        secondaryAccent = Color(0xFFF08E9D),
        fabColor = Color(0xFF38E5B0), // Mint
    )

    val PremiumBurgundy = VrddhihThemeConfig(
        id = "premium_burgundy",
        displayName = "Burgundy",
        primaryAccent = Color(0xFFA64868),
        secondaryAccent = Color(0xFFD47A97),
        fabColor = Color(0xFFE5B86B), // Champagne Gold
    )

    val PremiumLime = VrddhihThemeConfig(
        id = "premium_lime",
        displayName = "Lime",
        primaryAccent = Color(0xFF88AD16),
        secondaryAccent = Color(0xFFB8DC45),
        fabColor = Color(0xFF7048B8), // Deep Purple
    )

    val PremiumSage = VrddhihThemeConfig(
        id = "premium_sage",
        displayName = "Sage",
        primaryAccent = Color(0xFF5E945E),
        secondaryAccent = Color(0xFF8EC08E),
        fabColor = Color(0xFFC86F50), // Terracotta
    )

    val PremiumSlateBlue = VrddhihThemeConfig(
        id = "premium_slate_blue",
        displayName = "Slate Blue",
        primaryAccent = Color(0xFF536E8A),
        secondaryAccent = Color(0xFF839EB8),
        fabColor = Color(0xFFF2AD45), // Warm Amber
    )

    val PremiumWarmTaupe = VrddhihThemeConfig(
        id = "premium_warm_taupe",
        displayName = "Warm Taupe",
        primaryAccent = Color(0xFF856E66),
        secondaryAccent = Color(0xFFB39D95),
        fabColor = Color(0xFFFF765E), // Coral
    )

    // 4. Curated two-tone presets (Rows 4-5 in reference grid)
    val TwoToneEmeraldCoral = VrddhihThemeConfig(
        id = "twotone_emerald_coral",
        displayName = "Emerald & Coral",
        primaryAccent = Color(0xFF249C65),
        secondaryAccent = Color(0xFFFF765E),
        fabColor = Color(0xFFFF765E),
        isTwoTone = true,
        innerCoreColor = Color(0xFFFF765E),
        category = ThemeCategory.TWO_TONE,
    )

    val TwoToneTaupePeach = VrddhihThemeConfig(
        id = "twotone_taupe_peach",
        displayName = "Taupe & Peach",
        primaryAccent = Color(0xFF856E66),
        secondaryAccent = Color(0xFFE6735C),
        fabColor = Color(0xFF5795FF),
        isTwoTone = true,
        innerCoreColor = Color(0xFFE6735C),
        category = ThemeCategory.TWO_TONE,
    )

    val TwoToneAmberRose = VrddhihThemeConfig(
        id = "twotone_amber_rose",
        displayName = "Amber & Rose",
        primaryAccent = Color(0xFFE8A027),
        secondaryAccent = Color(0xFFD82F5A),
        fabColor = Color(0xFF7048C8),
        isTwoTone = true,
        innerCoreColor = Color(0xFFD82F5A),
        category = ThemeCategory.TWO_TONE,
    )

    val TwoToneTealPurple = VrddhihThemeConfig(
        id = "twotone_teal_purple",
        displayName = "Teal & Purple",
        primaryAccent = Color(0xFF1EAA92),
        secondaryAccent = Color(0xFF9E4DE8),
        fabColor = Color(0xFFFF8A35),
        isTwoTone = true,
        innerCoreColor = Color(0xFF9E4DE8),
        category = ThemeCategory.TWO_TONE,
    )

    val TwoToneSlateCoral = VrddhihThemeConfig(
        id = "twotone_slate_coral",
        displayName = "Slate Blue & Coral",
        primaryAccent = Color(0xFF536E8A),
        secondaryAccent = Color(0xFFFF765E),
        fabColor = Color(0xFFF2AD45),
        isTwoTone = true,
        innerCoreColor = Color(0xFFFF765E),
        category = ThemeCategory.TWO_TONE,
    )

    val TwoTonePurpleMagenta = VrddhihThemeConfig(
        id = "twotone_purple_magenta",
        displayName = "Purple & Magenta",
        primaryAccent = Color(0xFF7B4BD4),
        secondaryAccent = Color(0xFFD32F65),
        fabColor = Color(0xFFFFC45C),
        isTwoTone = true,
        innerCoreColor = Color(0xFFD32F65),
        category = ThemeCategory.TWO_TONE,
    )

    val TwoToneRoseCyan = VrddhihThemeConfig(
        id = "twotone_rose_cyan",
        displayName = "Rose & Cyan",
        primaryAccent = Color(0xFFD82F5A),
        secondaryAccent = Color(0xFF0BB0C8),
        fabColor = Color(0xFFFFB000),
        isTwoTone = true,
        innerCoreColor = Color(0xFF0BB0C8),
        category = ThemeCategory.TWO_TONE,
    )

    val TwoToneTaupeSage = VrddhihThemeConfig(
        id = "twotone_taupe_sage",
        displayName = "Taupe & Sage",
        primaryAccent = Color(0xFF856E66),
        secondaryAccent = Color(0xFF5E945E),
        fabColor = Color(0xFFFF765E),
        isTwoTone = true,
        innerCoreColor = Color(0xFF5E945E),
        category = ThemeCategory.TWO_TONE,
    )

    val TwoToneGoldTeal = VrddhihThemeConfig(
        id = "twotone_gold_teal",
        displayName = "Gold & Teal",
        primaryAccent = Color(0xFFC9942E),
        secondaryAccent = Color(0xFF1EAA92),
        fabColor = Color(0xFF6541A5),
        isTwoTone = true,
        innerCoreColor = Color(0xFF1EAA92),
        category = ThemeCategory.TWO_TONE,
    )

    val TwoToneSageCoral = VrddhihThemeConfig(
        id = "twotone_sage_coral",
        displayName = "Sage & Coral",
        primaryAccent = Color(0xFF5E945E),
        secondaryAccent = Color(0xFFFF765E),
        fabColor = Color(0xFFC86F50),
        isTwoTone = true,
        innerCoreColor = Color(0xFFFF765E),
        category = ThemeCategory.TWO_TONE,
    )

    val TwoToneSlateMint = VrddhihThemeConfig(
        id = "twotone_slate_mint",
        displayName = "Slate & Mint",
        primaryAccent = Color(0xFF536E8A),
        secondaryAccent = Color(0xFF2BB358),
        fabColor = Color(0xFFFF8A35),
        isTwoTone = true,
        innerCoreColor = Color(0xFF2BB358),
        category = ThemeCategory.TWO_TONE,
    )

    val TwoToneVioletDustyRose = VrddhihThemeConfig(
        id = "twotone_violet_dusty_rose",
        displayName = "Violet & Dusty Rose",
        primaryAccent = Color(0xFF786899),
        secondaryAccent = Color(0xFFC96172),
        fabColor = Color(0xFFFFC45C),
        isTwoTone = true,
        innerCoreColor = Color(0xFFC96172),
        category = ThemeCategory.TWO_TONE,
    )

    val all: List<VrddhihThemeConfig> = listOf(
        Default,
        CoreRose,
        CoreOrange,
        CoreCyan,
        CorePurple,
        PremiumRed,
        PremiumCoral,
        PremiumAmber,
        PremiumGreen,
        PremiumTeal,
        PremiumViolet,
        PremiumRaspberry,
        PremiumTerracotta,
        PremiumGold,
        PremiumEmerald,
        PremiumSkyBlue,
        PremiumLavender,
        PremiumDustyRose,
        PremiumBurgundy,
        PremiumLime,
        PremiumSage,
        PremiumSlateBlue,
        PremiumWarmTaupe,
        TwoToneEmeraldCoral,
        TwoToneTaupePeach,
        TwoToneAmberRose,
        TwoToneTealPurple,
        TwoToneSlateCoral,
        TwoTonePurpleMagenta,
        TwoToneRoseCyan,
        TwoToneTaupeSage,
        TwoToneGoldTeal,
        TwoToneSageCoral,
        TwoToneSlateMint,
        TwoToneVioletDustyRose,
    )

    val coreThemes: List<VrddhihThemeConfig> = listOf(
        CoreRose,
        CoreOrange,
        CoreCyan,
        CorePurple,
    )

    val premiumAccentThemes: List<VrddhihThemeConfig> = listOf(
        PremiumRed,
        PremiumCoral,
        PremiumAmber,
        PremiumGreen,
        PremiumTeal,
        PremiumViolet,
        PremiumRaspberry,
        PremiumTerracotta,
        PremiumGold,
        PremiumEmerald,
        PremiumSkyBlue,
        PremiumLavender,
        PremiumDustyRose,
        PremiumBurgundy,
        PremiumLime,
        PremiumSage,
        PremiumSlateBlue,
        PremiumWarmTaupe,
    )

    val twoTonePresets: List<VrddhihThemeConfig> = listOf(
        TwoToneEmeraldCoral,
        TwoToneTaupePeach,
        TwoToneAmberRose,
        TwoToneTealPurple,
        TwoToneSlateCoral,
        TwoTonePurpleMagenta,
        TwoToneRoseCyan,
        TwoToneTaupeSage,
        TwoToneGoldTeal,
        TwoToneSageCoral,
        TwoToneSlateMint,
        TwoToneVioletDustyRose,
    )

    fun findById(id: String): VrddhihThemeConfig? {
        return all.firstOrNull { it.id == id }
    }
}

/**
 * Holds reactive theme state using Compose snapshot state.
 * Any composable reading properties backed by this state will automatically recompose.
 */
object VrddhihThemeState {
    var currentTheme: VrddhihThemeConfig by mutableStateOf(VrddhihThemes.Default)
}

val LocalVrddhihTheme = staticCompositionLocalOf { VrddhihThemes.Default }

/**
 * Local offline persistence manager for the active theme.
 * Loads synchronously upon Application/Activity creation to prevent default theme flash.
 */
object VrddhihThemeManager {
    private const val PREFS_NAME = "vrddhih_theme_preferences"
    private const val KEY_THEME_ID = "selected_theme_id"

    private var sharedPreferences: SharedPreferences? = null

    fun init(context: Context) {
        if (sharedPreferences == null) {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sharedPreferences = prefs
            val savedId = prefs.getString(KEY_THEME_ID, VrddhihThemes.DEFAULT_ID) ?: VrddhihThemes.DEFAULT_ID
            val theme = VrddhihThemes.findById(savedId) ?: VrddhihThemes.Default
            VrddhihThemeState.currentTheme = theme
        }
    }

    fun selectTheme(theme: VrddhihThemeConfig) {
        VrddhihThemeState.currentTheme = theme
        sharedPreferences?.edit()?.putString(KEY_THEME_ID, theme.id)?.apply()
    }
}
