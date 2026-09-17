package com.example.model

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class PlayerData(
    val level: Int = 1,
    val xp: Int = 0,
    val coins: Int = 200,
    val lives: Int = 3,
    val maxLives: Int = 3,
    val lastLifeTimestamp: Long = System.currentTimeMillis(),
    val hints: Int = 3,
    val undos: Int = 5,
    val stars: Int = 0,
    val streak: Int = 1,
    val lastDailyDate: String = "",
    val activeThemeId: String = "neon",
    val unlockedThemeIds: Set<String> = setOf("neon"),
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val completedClassic: Map<Int, Int> = emptyMap(), // level -> stars
    val completedMaze: Map<Int, Int> = emptyMap(),
    val completed3D: Map<Int, Int> = emptyMap(),
    val unlockedAchievements: Set<String> = emptySet(),
    val isPremium: Boolean = false
)

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("arrowflux_save_v1", Context.MODE_PRIVATE)

    private val _playerData = MutableStateFlow(loadData())
    val playerData: StateFlow<PlayerData> = _playerData.asStateFlow()

    private fun loadData(): PlayerData {
        val version = prefs.getInt("save_version", 1)
        val coins = prefs.getInt("coins", 200)
        val lives = prefs.getInt("lives", 3)
        val hints = prefs.getInt("hints", 3)
        val undos = prefs.getInt("undos", 5)
        val stars = prefs.getInt("stars", 0)
        val level = prefs.getInt("level", 1)
        val xp = prefs.getInt("xp", 0)
        val streak = prefs.getInt("streak", 1)
        val lastDailyDate = prefs.getString("last_daily_date", "") ?: ""
        val activeThemeId = prefs.getString("active_theme", "neon") ?: "neon"
        val sound = prefs.getBoolean("sound", true)
        val music = prefs.getBoolean("music", true)
        val vibration = prefs.getBoolean("vibration", true)
        val reducedMotion = prefs.getBoolean("reduced_motion", false)
        val isPremium = prefs.getBoolean("is_premium", false)

        val unlockedThemes = prefs.getStringSet("unlocked_themes", setOf("neon")) ?: setOf("neon")
        val unlockedAchievements = prefs.getStringSet("unlocked_achievements", emptySet()) ?: emptySet()

        val classicMap = parseLevelMap(prefs.getString("completed_classic", "{}") ?: "{}")
        val mazeMap = parseLevelMap(prefs.getString("completed_maze", "{}") ?: "{}")
        val threeDMap = parseLevelMap(prefs.getString("completed_3d", "{}") ?: "{}")

        return PlayerData(
            level = level,
            xp = xp,
            coins = coins,
            lives = lives,
            hints = hints,
            undos = undos,
            stars = stars,
            streak = streak,
            lastDailyDate = lastDailyDate,
            activeThemeId = activeThemeId,
            unlockedThemeIds = unlockedThemes,
            soundEnabled = sound,
            musicEnabled = music,
            vibrationEnabled = vibration,
            reducedMotion = reducedMotion,
            completedClassic = classicMap,
            completedMaze = mazeMap,
            completed3D = threeDMap,
            unlockedAchievements = unlockedAchievements,
            isPremium = isPremium
        )
    }

    private fun parseLevelMap(jsonStr: String): Map<Int, Int> {
        val map = mutableMapOf<Int, Int>()
        try {
            val json = JSONObject(jsonStr)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key.toInt()] = json.getInt(key)
            }
        } catch (_: Exception) {}
        return map
    }

    private fun saveLevelMap(map: Map<Int, Int>): String {
        val json = JSONObject()
        map.forEach { (lvl, stars) -> json.put(lvl.toString(), stars) }
        return json.toString()
    }

    fun save(data: PlayerData) {
        _playerData.value = data
        prefs.edit().apply {
            putInt("save_version", 1)
            putInt("level", data.level)
            putInt("xp", data.xp)
            putInt("coins", data.coins)
            putInt("lives", data.lives)
            putInt("hints", data.hints)
            putInt("undos", data.undos)
            putInt("stars", data.stars)
            putInt("streak", data.streak)
            putString("last_daily_date", data.lastDailyDate)
            putString("active_theme", data.activeThemeId)
            putBoolean("sound", data.soundEnabled)
            putBoolean("music", data.musicEnabled)
            putBoolean("vibration", data.vibrationEnabled)
            putBoolean("reduced_motion", data.reducedMotion)
            putBoolean("is_premium", data.isPremium)
            putStringSet("unlocked_themes", data.unlockedThemeIds)
            putStringSet("unlocked_achievements", data.unlockedAchievements)
            putString("completed_classic", saveLevelMap(data.completedClassic))
            putString("completed_maze", saveLevelMap(data.completedMaze))
            putString("completed_3d", saveLevelMap(data.completed3D))
            apply()
        }
    }

    fun completeLevel(mode: GameMode, level: Int, starsEarned: Int, coinsEarned: Int) {
        val current = _playerData.value
        val currentMap = when (mode) {
            GameMode.CLASSIC -> current.completedClassic.toMutableMap()
            GameMode.MAZE -> current.completedMaze.toMutableMap()
            GameMode.THREE_D -> current.completed3D.toMutableMap()
        }
        val prevStars = currentMap[level] ?: 0
        val starsDiff = if (starsEarned > prevStars) starsEarned - prevStars else 0
        if (starsEarned > prevStars) {
            currentMap[level] = starsEarned
        }

        val newAchievements = current.unlockedAchievements.toMutableSet()
        if (newAchievements.isEmpty()) newAchievements.add("FIRST_ESCAPE")
        val totalLevelsDone = (current.completedClassic.size + current.completedMaze.size + current.completed3D.size) + (if (prevStars == 0) 1 else 0)
        if (totalLevelsDone >= 5) newAchievements.add("ARROW_BEGINNER")
        if (totalLevelsDone >= 25) newAchievements.add("ARROW_MASTER")
        if (current.stars + starsDiff >= 50) newAchievements.add("STAR_COLLECTOR")
        if (mode == GameMode.THREE_D) newAchievements.add("3D_EXPLORER")
        if (mode == GameMode.MAZE) newAchievements.add("MAZE_MASTER")

        val newXp = current.xp + 40 + (starsEarned * 10)
        val newLevel = 1 + (newXp / 150)

        val updated = current.copy(
            coins = current.coins + coinsEarned,
            stars = current.stars + starsDiff,
            xp = newXp,
            level = newLevel,
            completedClassic = if (mode == GameMode.CLASSIC) currentMap else current.completedClassic,
            completedMaze = if (mode == GameMode.MAZE) currentMap else current.completedMaze,
            completed3D = if (mode == GameMode.THREE_D) currentMap else current.completed3D,
            unlockedAchievements = newAchievements
        )
        save(updated)
    }

    fun consumeLife(): Boolean {
        val current = _playerData.value
        if (current.lives <= 0) return false
        val newLives = current.lives - 1
        save(current.copy(lives = newLives))
        return true
    }

    fun refillLives() {
        val current = _playerData.value
        save(current.copy(lives = current.maxLives))
    }

    fun consumeHint(): Boolean {
        val current = _playerData.value
        if (current.hints > 0) {
            save(current.copy(hints = current.hints - 1))
            return true
        }
        if (current.coins >= 30) {
            save(current.copy(coins = current.coins - 30))
            return true
        }
        return false
    }

    fun consumeUndo(): Boolean {
        val current = _playerData.value
        if (current.undos > 0) {
            save(current.copy(undos = current.undos - 1))
            return true
        }
        if (current.coins >= 15) {
            save(current.copy(coins = current.coins - 15))
            return true
        }
        return false
    }

    fun addCoins(amount: Int) {
        val current = _playerData.value
        save(current.copy(coins = current.coins + amount))
    }

    fun purchaseTheme(themeId: String, cost: Int): Boolean {
        val current = _playerData.value
        if (current.unlockedThemeIds.contains(themeId)) {
            save(current.copy(activeThemeId = themeId))
            return true
        }
        if (current.coins >= cost) {
            val unlocked = current.unlockedThemeIds + themeId
            save(current.copy(coins = current.coins - cost, unlockedThemeIds = unlocked, activeThemeId = themeId))
            return true
        }
        return false
    }

    fun setTheme(themeId: String) {
        val current = _playerData.value
        if (current.unlockedThemeIds.contains(themeId)) {
            save(current.copy(activeThemeId = themeId))
        }
    }

    fun resetProgress() {
        prefs.edit().clear().apply()
        _playerData.value = PlayerData()
    }
}
