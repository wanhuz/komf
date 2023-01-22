package org.snd.config

import com.charleskorn.kaml.Yaml
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isWritable
import kotlin.io.path.writeText
import kotlin.text.Charsets.UTF_8

class ConfigWriter(
    private val yaml: Yaml
) {

    @Synchronized
    fun writeConfig(config: AppConfig, path: Path) {
        checkWriteAccess(path)
        if (path.isDirectory()) {
            path.resolve("application.yml")
                .writeText(yaml.encodeToString(AppConfig.serializer(), removeDeprecatedOptions(config)), UTF_8)
        } else {
            path.writeText(yaml.encodeToString(AppConfig.serializer(), removeDeprecatedOptions(config)), UTF_8)
        }
    }

    @Synchronized
    fun writeConfigToDefaultPath(config: AppConfig) {
        val filePath = Path.of(".").toAbsolutePath().normalize().resolve("application.yml")
        checkWriteAccess(filePath)
        filePath.writeText(yaml.encodeToString(AppConfig.serializer(), removeDeprecatedOptions(config)), UTF_8)
    }

    private fun checkWriteAccess(path: Path) {
        if (path.isWritable().not()) throw AccessDeniedException(file = path.toFile(), reason = "No write access to config file")
    }

    private fun removeDeprecatedOptions(config: AppConfig): AppConfig {
        return config.copy(
            komga = config.komga.copy(
                metadataUpdate = config.komga.metadataUpdate.copy(
                    bookThumbnails = null,
                    seriesThumbnails = null,
                    seriesTitle = null,
                    titleType = null,
                    readingDirectionValue = null,
                    languageValue = null,
                    orderBooks = null,
                    modes = null
                ),
                aggregateMetadata = null
            ),
            kavita = config.kavita.copy(
                metadataUpdate = config.kavita.metadataUpdate.copy(
                    bookThumbnails = null,
                    seriesThumbnails = null,
                    seriesTitle = null,
                    titleType = null,
                    readingDirectionValue = null,
                    languageValue = null,
                    orderBooks = null,
                    modes = null
                ),
                aggregateMetadata = null
            ),
            metadataProviders = config.metadataProviders.copy(
                mangaUpdates = null,
                mal = null,
                nautiljon = null,
                aniList = null,
                yenPress = null,
                kodansha = null,
                viz = null,
                bookWalker = null
            )
        )

    }
}