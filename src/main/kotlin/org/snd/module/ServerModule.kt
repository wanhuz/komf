package org.snd.module

import com.charleskorn.kaml.Yaml
import io.javalin.Javalin
import io.javalin.util.ConcurrencyUtil
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import org.eclipse.jetty.server.LowResourceMonitor
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.StatisticsHandler
import org.snd.api.ConfigController
import org.snd.api.ConfigUpdateMapper
import org.snd.api.MetadataController
import org.snd.config.ConfigWriter
import org.snd.config.ServerConfig
import org.snd.mediaserver.model.MediaServer.KAVITA
import org.snd.mediaserver.model.MediaServer.KOMGA
import java.util.concurrent.Executors

class ServerModule(
    config: ServerConfig,
    appContext: AppContext,
    mediaServerModule: MediaServerModule,
    jsonModule: JsonModule,
) : AutoCloseable {
    private val executor = Executors.newSingleThreadExecutor(
        BasicThreadFactory.Builder()
            .namingPattern("komf-api-task-handler-%d").build()
    )

    private val configWriter = ConfigWriter(Yaml.default)
    private val configMapper = ConfigUpdateMapper()

    private val server = Javalin.create { config ->
        config.plugins.enableCors { cors -> cors.add { it.anyHost() } }
        config.showJavalinBanner = false
        config.jetty.server {
            Server(ConcurrencyUtil.jettyThreadPool("JettyServerThreadPool")).apply {
                addBean(LowResourceMonitor(this))
                insertHandler(StatisticsHandler())
                setAttribute("is-default-server", true)
                stopTimeout = 5000L
            }
        }
    }.routes {
        MetadataController(
            metadataService = mediaServerModule.komgaMetadataService,
            metadataUpdateService = mediaServerModule.komgaMetadataUpdateService,
            taskHandler = executor,
            moshi = jsonModule.moshi,
            serverType = KOMGA
        ).register()
        MetadataController(
            metadataService = mediaServerModule.kavitaMetadataService,
            metadataUpdateService = mediaServerModule.kavitaMetadataUpdateService,
            taskHandler = executor,
            moshi = jsonModule.moshi,
            serverType = KAVITA
        ).register()

        ConfigController(
            appContext = appContext,
            configWriter = configWriter,
            moshi = jsonModule.moshi,
            configMapper = configMapper
        ).register()
    }

    init {
        server.start(config.port)
    }

    override fun close() {
        server.close()
        executor.shutdown()
    }
}
