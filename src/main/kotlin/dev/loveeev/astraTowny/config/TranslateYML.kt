package dev.loveeev.astratowny.config

import dev.loveeev.astratowny.AstraTowny
import dev.loveeev.astratowny.manager.TownManager
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.*

object TranslateYML {
    private val translations = mutableMapOf<String, YamlConfiguration>()

    init {
        loadTranslations()
    }

    fun getTranslations(): List<String> = translations.keys.toList()

    private fun loadTranslations() {
        val folder = File("${AstraTowny.instance.dataFolder}/translate")
        if (!folder.exists()) folder.mkdirs()

        folder.listFiles { _, name -> name.endsWith(".yml") }?.forEach { file ->
            val langCode = file.nameWithoutExtension.lowercase(Locale.ROOT)
            translations[langCode] = YamlConfiguration.loadConfiguration(file)
        }
    }

    private fun getPlayerLanguage(player: Player): String {
        return TownManager.getResident(player)!!.language
    }

    fun getTranslation(player: Player, key: String): String {
        val lang = getPlayerLanguage(player)
        return translations[lang]?.getString(key) ?: translations["en"]?.getString(key) ?: key
    }
    fun getTranslation( key: String): String {
        return translations["en"]?.getString(key) ?: key
    }

    fun getTranslationList(player: Player, key: String): List<String> {
        val lang = getPlayerLanguage(player)
        return translations[lang]?.getStringList(key) ?: translations["en"]?.getStringList(key) ?: listOf(key)
    }

    fun reload() {
        translations.clear()
        loadTranslations()
    }

}
