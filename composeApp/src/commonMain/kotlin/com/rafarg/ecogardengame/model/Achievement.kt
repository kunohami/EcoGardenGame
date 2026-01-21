package com.rafarg.ecogardengame.model

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val checkEarned: (GameItemProvider) -> Boolean
)

interface GameItemProvider {
    val items: List<GameItem>
    val globalUpgrades: List<GlobalUpgrade>
    val libraryCategories: List<LibraryCategory>
}

object AchievementRepository {
    val achievements = listOf(
        // --- FRUIT UNLOCKS ---
        Achievement(
            id = "unlock_all_fruits",
            name = "Master Gardener",
            description = "Unlock all fruits and vegetables in the garden.",
            emoji = "🚜",
            checkEarned = { provider -> provider.items.all { it.unlocked } }
        ),
        
        // --- MODIFIERS ---
        Achievement(
            id = "unlock_all_modifiers",
            name = "Geneticist",
            description = "Unlock all specific modifiers for every plant.",
            emoji = "🧬",
            checkEarned = { provider -> 
                provider.items.all { item -> 
                    item.modifiers.all { it.isUnlocked } 
                } 
            }
        ),

        // --- UPGRADES ---
        Achievement(
            id = "unlock_all_upgrades",
            name = "Shopaholic",
            description = "Max out all global upgrades in the shop.",
            emoji = "💎",
            checkEarned = { provider -> provider.globalUpgrades.all { it.isMaxLevel } }
        ),

        // --- LIBRARY CATEGORIES ---
        Achievement(
            id = "lib_tomato_pro",
            name = "Tomato Expert",
            description = "Read all facts about Tomatoes.",
            emoji = "📕",
            checkEarned = { provider -> provider.libraryCategories.find { it.id == "tomato" }?.entries?.all { it.isUnlocked } ?: false }
        ),
        Achievement(
            id = "lib_broccoli_pro",
            name = "Broccoli Expert",
            description = "Read all facts about Broccoli.",
            emoji = "📗",
            checkEarned = { provider -> provider.libraryCategories.find { it.id == "broccoli" }?.entries?.all { it.isUnlocked } ?: false }
        ),
        Achievement(
            id = "lib_bell_pepper_pro",
            name = "Pepper Expert",
            description = "Read all facts about Bell Peppers.",
            emoji = "📙",
            checkEarned = { provider -> provider.libraryCategories.find { it.id == "bell_pepper" }?.entries?.all { it.isUnlocked } ?: false }
        ),
        Achievement(
            id = "lib_garlic_pro",
            name = "Garlic Expert",
            description = "Read all facts about Garlic.",
            emoji = "📔",
            checkEarned = { provider -> provider.libraryCategories.find { it.id == "garlic" }?.entries?.all { it.isUnlocked } ?: false }
        ),
        Achievement(
            id = "lib_purple_onion_pro",
            name = "Onion Expert",
            description = "Read all facts about Purple Onions.",
            emoji = "📓",
            checkEarned = { provider -> provider.libraryCategories.find { it.id == "purple_onion" }?.entries?.all { it.isUnlocked } ?: false }
        ),
        Achievement(
            id = "lib_squash_pro",
            name = "Squash Expert",
            description = "Read all facts about Squash.",
            emoji = "📒",
            checkEarned = { provider -> provider.libraryCategories.find { it.id == "squash" }?.entries?.all { it.isUnlocked } ?: false }
        ),
        Achievement(
            id = "lib_apple_pro",
            name = "Apple Expert",
            description = "Read all facts about Apples.",
            emoji = "📖",
            checkEarned = { provider -> provider.libraryCategories.find { it.id == "apple" }?.entries?.all { it.isUnlocked } ?: false }
        ),
        Achievement(
            id = "lib_plagues_pro",
            name = "Pest Control",
            description = "Unlock all information about Plagues.",
            emoji = "🔍",
            checkEarned = { provider -> provider.libraryCategories.find { it.id == "plagues" }?.entries?.all { it.isUnlocked } ?: false }
        ),
        Achievement(
            id = "lib_farmers_pro",
            name = "Social Studies",
            description = "Unlock all information about Farmers.",
            emoji = "🤝",
            checkEarned = { provider -> provider.libraryCategories.find { it.id == "farmers" }?.entries?.all { it.isUnlocked } ?: false }
        ),
        Achievement(
            id = "lib_pesticides_pro",
            name = "Chemical Engineer",
            description = "Unlock all information about Pesticides.",
            emoji = "🧪",
            checkEarned = { provider -> provider.libraryCategories.find { it.id == "pesticides" }?.entries?.all { it.isUnlocked } ?: false }
        ),
        Achievement(
            id = "lib_genetic_pro",
            name = "Bio-Technician",
            description = "Unlock all information about Genetic Modification.",
            emoji = "🧬",
            checkEarned = { provider -> provider.libraryCategories.find { it.id == "genetic" }?.entries?.all { it.isUnlocked } ?: false }
        )
    )
}
