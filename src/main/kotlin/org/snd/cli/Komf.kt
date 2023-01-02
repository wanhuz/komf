package org.snd.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import org.snd.mediaserver.model.MediaServer
import org.snd.mediaserver.model.MediaServer.KOMGA
import org.snd.module.AppContext
import org.snd.module.CliContext
import java.nio.file.Path


class Komf : CliktCommand(invokeWithoutSubcommand = true) {
    private val configDir by option(envvar = "KOMF_CONFIG_DIR").convert { Path.of(it) }
    private val configFile by option().convert { Path.of(it) }
    private val configFileArgument by argument().convert { Path.of(it) }.optional()
    private val verbose by option().flag()
    private val mediaServer by option().convert { MediaServer.valueOf(it.uppercase()) }.default(KOMGA)

    override fun run() {

        if (currentContext.invokedSubcommand == null) {
            val appContext = AppContext(configDir ?: configFileArgument ?: configFile)
            appContext.verbose = verbose
            appContext.init()
        } else {
            val cliContext = CliContext(
                configDir ?: configFileArgument ?: configFile,
                mediaServer,
                verbose
            )
            currentContext.findOrSetObject { cliContext }
        }
    }

}
