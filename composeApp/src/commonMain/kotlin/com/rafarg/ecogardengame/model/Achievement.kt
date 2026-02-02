package com.rafarg.ecogardengame.model

import org.jetbrains.compose.resources.StringResource
import ecogardengame.composeapp.generated.resources.*

/**
 * Data class representing an in-game achievement.
 * @property id Unique identifier for the achievement.
 * @property nameRes Resource ID for the achievement's name.
 * @property descriptionRes Resource ID for the description of how to earn it.
 * @property emoji The emoji icon associated with the achievement.
 * @property checkEarned Lambda function that defines the logic to determine if the achievement is met.
 */
data class Achievement(
    val id: String,
    val nameRes: StringResource,
    val descriptionRes: StringResource,
    val emoji: String,
    val checkEarned: (GameItemProvider) -> Boolean
)

/**
 * Interface that provides access to the necessary game data for checking achievement progress.
 * Implemented by the GameViewModel.
 */
interface GameItemProvider {
    val items: List<GameItem>
    val globalUpgrades: List<GlobalUpgrade>
    val libraryCategories: List<LibraryCategory>
    val totalClicks: Int
    fun isArtUnlocked(artId: String): Boolean
    fun getArtCount(): Int
}

/**
 * Data repository containing all the achievements available in the game.
 */
object AchievementRepository {
    val achievements = listOf(
        // --- PROGRESSION ACHIEVEMENTS ---
        Achievement(
            id = "unlock_all_fruits",
            nameRes = Res.string.ach_master_gardener_name,
            descriptionRes = Res.string.ach_master_gardener_desc,
            emoji = "🚜",
            checkEarned = { provider -> provider.items.all { it.unlocked } }
        ),
        
        Achievement(
            id = "unlock_all_modifiers",
            nameRes = Res.string.ach_geneticist_name,
            descriptionRes = Res.string.ach_geneticist_desc,
            emoji = "🧬",
            checkEarned = { provider -> 
                provider.items.all { item -> 
                    item.modifiers.all { it.isUnlocked } 
                } 
            }
        ),

        Achievement(
            id = "unlock_all_upgrades",
            nameRes = Res.string.ach_shopaholic_name,
            descriptionRes = Res.string.ach_shopaholic_desc,
            emoji = "💎",
            checkEarned = { provider -> provider.globalUpgrades.all { it.isMaxLevel } }
        ),

        // --- SKILL ACHIEVEMENTS ---
        Achievement(
            id = "squash_sonic_speed",
            nameRes = Res.string.ach_sonic_squash_name,
            descriptionRes = Res.string.ach_sonic_squash_desc,
            emoji = "⚡",
            checkEarned = { provider -> 
                provider.items.filterIsInstance<Squash>().any { it.maxStreak >= 10 }
            }
        ),

        Achievement(
            id = "tomato_critical_master",
            nameRes = Res.string.ach_tomato_sniper_name,
            descriptionRes = Res.string.ach_tomato_sniper_desc,
            emoji = "🎯",
            checkEarned = { provider -> 
                provider.items.filterIsInstance<Tomato>().any { it.criticalHits >= 50 }
            }
        ),

        // --- CLICK MILESTONES ---
        Achievement(
            id = "clicks_5000",
            nameRes = Res.string.ach_hard_worker_name,
            descriptionRes = Res.string.ach_hard_worker_desc,
            emoji = "🖐️",
            checkEarned = { it.totalClicks >= 5000 }
        ),
        Achievement(
            id = "clicks_10000",
            nameRes = Res.string.ach_dedicated_farmer_name,
            descriptionRes = Res.string.ach_dedicated_farmer_desc,
            emoji = "💪",
            checkEarned = { it.totalClicks >= 10000 }
        ),
        Achievement(
            id = "clicks_15000",
            nameRes = Res.string.ach_clicking_machine_name,
            descriptionRes = Res.string.ach_clicking_machine_desc,
            emoji = "🤖",
            checkEarned = { it.totalClicks >= 15000 }
        ),
        Achievement(
            id = "clicks_20000",
            nameRes = Res.string.ach_legendary_tapper_name,
            descriptionRes = Res.string.ach_legendary_tapper_desc,
            emoji = "👑",
            checkEarned = { it.totalClicks >= 20000 }
        ),

        // --- KNOWLEDGE (LIBRARY) ACHIEVEMENTS ---
        Achievement(
            id = "lib_tomato_pro",
            nameRes = Res.string.ach_tomato_expert_name,
            descriptionRes = Res.string.ach_tomato_expert_desc,
            emoji = "📕",
            checkEarned = { provider -> provider.libraryCategories.find { it.id == "tomato" }?.entries?.all { it.isUnlocked } ?: false }
        ),
        Achievement(
            id = "lib_broccoli_pro",
            nameRes = Res.string.ach_broccoli_expert_name,
            descriptionRes = Res.string.ach_broccoli_expert_desc,
            emoji = "📗",
            checkEarned = { provider -> provider.libraryCategories.find { it.id == "broccoli" }?.entries?.all { it.isUnlocked } ?: false }
        ),
        Achievement(
            id = "lib_bell_pepper_pro",
            nameRes = Res.string.ach_pepper_expert_name,
            descriptionRes = Res.string.ach_pepper_expert_desc,
            emoji = "📙",
            checkEarned = { provider -> provider.libraryCategories.find { it.id == "bell_pepper" }?.entries?.all { it.isUnlocked } ?: false }
        ),
        Achievement(
            id = "lib_garlic_pro",
            nameRes = Res.string.ach_garlic_expert_name,
            descriptionRes = Res.string.ach_garlic_expert_desc,
            emoji = "📔",
            checkEarned = { provider -> provider.libraryCategories.find { it.id == "garlic" }?.entries?.all { it.isUnlocked } ?: false }
        ),
        Achievement(
            id = "lib_purple_onion_pro",
            nameRes = Res.string.ach_onion_expert_name,
            descriptionRes = Res.string.ach_onion_expert_desc,
            emoji = "📓",
            checkEarned = { provider -> provider.libraryCategories.find { it.id == "purple_onion" }?.entries?.all { it.isUnlocked } ?: false }
        ),
        Achievement(
            id = "lib_squash_pro",
            nameRes = Res.string.ach_squash_expert_name,
            descriptionRes = Res.string.ach_squash_expert_desc,
            emoji = "📒",
            checkEarned = { provider -> provider.libraryCategories.find { it.id == "squash" }?.entries?.all { it.isUnlocked } ?: false }
        ),
        Achievement(
            id = "lib_apple_pro",
            nameRes = Res.string.ach_apple_expert_name,
            descriptionRes = Res.string.ach_apple_expert_desc,
            emoji = "📖",
            checkEarned = { provider -> provider.libraryCategories.find { it.id == "apple" }?.entries?.all { it.isUnlocked } ?: false }
        ),
        Achievement(
            id = "lib_plagues_pro",
            nameRes = Res.string.ach_pest_control_name,
            descriptionRes = Res.string.ach_pest_control_desc,
            emoji = "🔍",
            checkEarned = { provider -> provider.libraryCategories.find { it.id == "plagues" }?.entries?.all { it.isUnlocked } ?: false }
        ),
        Achievement(
            id = "lib_farmers_pro",
            nameRes = Res.string.ach_social_studies_name,
            descriptionRes = Res.string.ach_social_studies_desc,
            emoji = "🤝",
            checkEarned = { provider -> provider.libraryCategories.find { it.id == "farmers" }?.entries?.all { it.isUnlocked } ?: false }
        ),
        Achievement(
            id = "lib_pesticides_pro",
            nameRes = Res.string.ach_chemical_engineer_name,
            descriptionRes = Res.string.ach_chemical_engineer_desc,
            emoji = "🧪",
            checkEarned = { provider -> provider.libraryCategories.find { it.id == "pesticides" }?.entries?.all { it.isUnlocked } ?: false }
        ),
        Achievement(
            id = "lib_genetic_pro",
            nameRes = Res.string.ach_bio_technician_name,
            descriptionRes = Res.string.ach_bio_technician_desc,
            emoji = "🧬",
            checkEarned = { provider -> provider.libraryCategories.find { it.id == "genetic" }?.entries?.all { it.isUnlocked } ?: false }
        ),

        // --- ART GALLERY ACHIEVEMENT ---
        Achievement(
            id = "art_collector",
            nameRes = Res.string.ach_art_collector_name,
            descriptionRes = Res.string.ach_art_collector_desc,
            emoji = "🎨",
            checkEarned = { false } // Logic override is implemented in GameViewModel.checkAchievements
        )
    )
}
