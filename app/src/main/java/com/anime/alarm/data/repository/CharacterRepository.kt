package com.anime.alarm.data.repository

import com.anime.alarm.R
import com.anime.alarm.data.model.Character
import com.anime.alarm.data.model.CharacterAssets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CharacterRepository {

    // Mock data for now. In the future, this might come from a DB or Remote Config
    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> = _characters.asStateFlow()

    private val _selectedCharacterId = MutableStateFlow("waguri_default")
    val selectedCharacterId: StateFlow<String> = _selectedCharacterId.asStateFlow()

    init {
        loadCharacters()
    }

    private fun loadCharacters() {
        // Placeholder resource IDs. 
        // Note: Assuming R.drawable.ic_launcher_foreground exists as a fallback for now.
        // We will need real assets later.
        val defaultCharacter = Character(
            id = "waguri_default",
            name = "Waguri",
            description = "Your energetic morning companion!",
            isPremium = false,
            isUnlocked = true,
            assets = CharacterAssets(
                normalImage = R.drawable.ic_launcher_foreground, // Placeholder
                sleepyImage = R.drawable.ic_launcher_foreground, // Placeholder
                happyImage = R.drawable.ic_launcher_foreground   // Placeholder
            )
        )
        
        val premiumCharacter = Character(
            id = "waguri_maid",
            name = "Maid Waguri",
            description = "Ready to serve, Master!",
            isPremium = true,
            isUnlocked = false,
            assets = CharacterAssets(
                normalImage = R.drawable.ic_launcher_foreground,
                sleepyImage = R.drawable.ic_launcher_foreground,
                happyImage = R.drawable.ic_launcher_foreground
            )
        )

        _characters.value = listOf(defaultCharacter, premiumCharacter)
    }

    fun selectCharacter(id: String) {
        // Logic to check if unlocked before selecting
        val char = _characters.value.find { it.id == id }
        if (char != null && char.isUnlocked) {
            _selectedCharacterId.value = id
        }
    }
    
    fun getSelectedCharacter(): Character {
        return _characters.value.find { it.id == _selectedCharacterId.value } 
            ?: _characters.value.first()
    }
}
