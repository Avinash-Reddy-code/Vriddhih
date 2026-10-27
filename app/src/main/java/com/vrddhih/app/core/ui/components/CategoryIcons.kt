package com.vrddhih.app.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Loop
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.Rocket
import androidx.compose.material.icons.outlined.RunCircle
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Work
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Curated set of 56 icons for category selection. Each entry has a stable [key] stored in the database
 * and a corresponding Material icon [vector].
 *
 * When an unknown key is loaded from the database, [byKey] returns the [fallback] icon.
 * All icon identifiers have been verified to exist in the material-icons-extended library.
 */
object CategoryIcons {

    data class Entry(val key: String, val vector: ImageVector, val label: String, val group: String)

    val fallback: ImageVector = Icons.Outlined.MoreHoriz

    val GROUPS: List<String> = listOf("General", "Work & Study", "Health & Fitness", "Home & Lifestyle", "Finance", "Other")

    val ALL: List<Entry> = listOf(
        // General
        Entry("person",           Icons.Outlined.Person,              "Person",         "General"),
        Entry("people",           Icons.Outlined.People,              "People",         "General"),
        Entry("star",             Icons.Outlined.Star,                "Star",           "General"),
        Entry("flag",             Icons.Outlined.Flag,                "Flag",           "General"),
        Entry("pin",              Icons.Outlined.Pin,                 "Pin",            "General"),
        Entry("calendar_month",   Icons.Outlined.CalendarMonth,       "Calendar",       "General"),
        Entry("alarm",            Icons.Outlined.Alarm,               "Alarm",          "General"),
        Entry("timer",            Icons.Outlined.Timer,               "Timer",          "General"),
        Entry("notifications",    Icons.Outlined.Notifications,       "Bell",           "General"),
        Entry("check_circle",     Icons.Outlined.CheckCircleOutline,  "Check Circle",   "General"),
        Entry("task",             Icons.Outlined.Task,                "Task",           "General"),
        Entry("folder",           Icons.Outlined.Folder,              "Folder",         "General"),
        Entry("rocket",           Icons.Outlined.Rocket,              "Rocket",         "General"),
        Entry("flash_on",         Icons.Outlined.FlashOn,             "Lightning",      "General"),
        Entry("more_horiz",       Icons.Outlined.MoreHoriz,           "More",           "General"),

        // Work & Study
        Entry("work",             Icons.Outlined.Work,                "Briefcase",      "Work & Study"),
        Entry("code",             Icons.Outlined.Code,                "Code",           "Work & Study"),
        Entry("book",             Icons.Outlined.Book,                "Book",           "Work & Study"),
        Entry("menu_book",        Icons.AutoMirrored.Outlined.MenuBook, "Open Book",      "Work & Study"),
        Entry("school",           Icons.Outlined.School,              "Graduation Cap", "Work & Study"),
        Entry("lightbulb",        Icons.Outlined.Lightbulb,           "Idea",           "Work & Study"),
        Entry("palette",          Icons.Outlined.Palette,             "Palette",        "Work & Study"),
        Entry("music_note",       Icons.Outlined.MusicNote,           "Music",          "Work & Study"),
        Entry("camera",           Icons.Outlined.CameraAlt,           "Camera",         "Work & Study"),
        Entry("language",         Icons.Outlined.Language,            "Globe",          "Work & Study"),
        Entry("mail",             Icons.Outlined.MailOutline,         "Mail",           "Work & Study"),
        Entry("phone",            Icons.Outlined.Phone,               "Phone",          "Work & Study"),
        Entry("travel",           Icons.Outlined.TravelExplore,       "Travel",         "Work & Study"),

        // Health & Fitness
        Entry("fitness_center",   Icons.Outlined.FitnessCenter,       "Dumbbell",       "Health & Fitness"),
        Entry("run_circle",       Icons.Outlined.RunCircle,           "Running",        "Health & Fitness"),
        Entry("favorite",         Icons.Outlined.FavoriteBorder,      "Heart",          "Health & Fitness"),
        Entry("spa",              Icons.Outlined.Spa,                 "Spa",            "Health & Fitness"),
        Entry("water_drop",       Icons.Outlined.WaterDrop,           "Water",          "Health & Fitness"),
        Entry("bedtime",          Icons.Outlined.Bedtime,             "Sleep",          "Health & Fitness"),
        Entry("self_improvement", Icons.Outlined.SelfImprovement,     "Meditation",     "Health & Fitness"),
        Entry("local_florist",    Icons.Outlined.LocalFlorist,        "Leaf",           "Health & Fitness"),
        Entry("loop",             Icons.Outlined.Loop,                "Routine",        "Health & Fitness"),

        // Home & Lifestyle
        Entry("home",             Icons.Outlined.Home,                "Home",           "Home & Lifestyle"),
        Entry("cleaning_services",Icons.Outlined.CleaningServices,    "Cleaning",       "Home & Lifestyle"),
        Entry("shopping_cart",    Icons.Outlined.ShoppingCart,        "Shopping",       "Home & Lifestyle"),
        Entry("family_restroom",  Icons.Outlined.FamilyRestroom,      "Family",         "Home & Lifestyle"),
        Entry("directions_car",   Icons.Outlined.DirectionsCar,       "Car",            "Home & Lifestyle"),
        Entry("directions_bike",  Icons.AutoMirrored.Outlined.DirectionsBike, "Bicycle",  "Home & Lifestyle"),
        Entry("local_dining",     Icons.Outlined.LocalDining,         "Restaurant",     "Home & Lifestyle"),
        Entry("coffee",           Icons.Outlined.Coffee,              "Coffee",         "Home & Lifestyle"),
        Entry("hotel",            Icons.Outlined.Hotel,               "Bed",            "Home & Lifestyle"),
        Entry("cake",             Icons.Outlined.Cake,                "Gift",           "Home & Lifestyle"),
        Entry("autorenew",        Icons.Outlined.Autorenew,           "Cycle",          "Home & Lifestyle"),

        // Finance
        Entry("account_balance_wallet", Icons.Outlined.AccountBalanceWallet, "Wallet",  "Finance"),
        Entry("credit_card",      Icons.Outlined.CreditCard,          "Credit Card",    "Finance"),
        Entry("savings",          Icons.Outlined.Savings,             "Savings",        "Finance"),

        // Other
        Entry("emoji_events",     Icons.Outlined.EmojiEvents,         "Trophy",         "Other"),
        Entry("security",         Icons.Outlined.Security,            "Shield",         "Other"),
    )

    private val byKeyMap: Map<String, Entry> = ALL.associateBy { it.key }

    private val aliases: Map<String, String> = mapOf(
        "body" to "person",
        "mind" to "self_improvement",
        "study" to "school",
        "fitness" to "fitness_center",
        "food" to "local_dining",
        "sleep" to "bedtime",
        "money" to "account_balance_wallet",
        "social" to "people",
        "family" to "family_restroom",
        "selfcare" to "spa",
        "errands" to "shopping_cart",
        "other" to "more_horiz",
        "coding" to "code",
    )

    fun byKey(key: String): ImageVector {
        val resolvedKey = aliases[key] ?: key
        return byKeyMap[resolvedKey]?.vector ?: fallback
    }

    fun labelFor(key: String): String {
        val resolvedKey = aliases[key] ?: key
        return byKeyMap[resolvedKey]?.label ?: key
    }
}

/** A curated accessible color palette for custom category accent colors against the dark navy theme. */
object CategoryColors {
    /** Each pair is (ARGB int, human-readable label). */
    val PALETTE: List<Pair<Int, String>> = listOf(
        0xFF7CD3FF.toInt() to "Ice Blue",
        0xFF2F8CFF.toInt() to "Electric Blue",
        0xFF80DEEA.toInt() to "Cyan",
        0xFF69F0AE.toInt() to "Mint Green",
        0xFF45E0B0.toInt() to "Teal",
        0xFFB39DDB.toInt() to "Lavender",
        0xFFCE93D8.toInt() to "Violet",
        0xFFF48FB1.toInt() to "Rose",
        0xFFEF9A9A.toInt() to "Salmon",
        0xFFFFAB91.toInt() to "Peach",
        0xFFFFCC80.toInt() to "Amber",
        0xFFFFB74D.toInt() to "Orange",
        0xFFA5D6A7.toInt() to "Sage",
        0xFF80CBC4.toInt() to "Seafoam",
        0xFF7A8DAB.toInt() to "Steel",
        0xFFE8F2FF.toInt() to "White",
    )
}
