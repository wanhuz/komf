package org.snd.mediaserver.kavita

import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import mu.KotlinLogging
import org.snd.mediaserver.NotificationService
import org.snd.mediaserver.kavita.model.KavitaChapter
import org.snd.mediaserver.kavita.model.KavitaVolume
import org.snd.mediaserver.kavita.model.KavitaVolumeId
import org.snd.mediaserver.kavita.model.events.CoverUpdateEvent
import org.snd.mediaserver.kavita.model.events.NotificationProgressEvent
import org.snd.mediaserver.kavita.model.events.SeriesRemovedEvent
import org.snd.mediaserver.model.MediaServerBookId
import org.snd.mediaserver.model.MediaServerSeriesId
import org.snd.mediaserver.repository.SeriesMatchRepository
import org.snd.module.MediaServerModule.MetadataServiceProvider
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit.SECONDS
import java.util.function.Predicate

private val logger = KotlinLogging.logger {}

//ugly
class KavitaEventListener(
    private val baseUrl: String,
    private val metadataServiceProvider: MetadataServiceProvider,
    private val kavitaClient: KavitaClient,
    private val tokenProvider: KavitaTokenProvider,
    private val libraryFilter: Predicate<String>,
    private val notificationService: NotificationService,
    private val seriesMatchRepository: SeriesMatchRepository,
    private val executor: ExecutorService,
    private val clock: Clock
) {
    private var hubConnection: HubConnection? = null
    private val volumesChanged: MutableList<Int> = ArrayList()

    @Volatile
    private var lastScan: Instant = clock.instant()

    @Volatile
    private var isActive: Boolean = false

    fun start() {
        isActive = true
        val hubConnection: HubConnection = HubConnectionBuilder
            .create("$baseUrl/hubs/messages")
            .withAccessTokenProvider(Single.defer { Single.just(tokenProvider.getToken()) })
            .build()
        hubConnection.on("NotificationProgress", ::processNotification, NotificationProgressEvent::class.java)
        hubConnection.on("CoverUpdate", ::processCoverUpdate, CoverUpdateEvent::class.java)
        hubConnection.on("SeriesRemoved", ::seriesRemoved, SeriesRemovedEvent::class.java)

        hubConnection.onClosed { reconnect(hubConnection) }
        registerInvocations(hubConnection)

        Completable.defer {
            hubConnection.start()
                .delaySubscription(10, SECONDS, Schedulers.trampoline())
                .doOnError { logger.error(it) { } }
        }
            .retry().subscribeOn(Schedulers.io()).subscribe()
        this.hubConnection = hubConnection
    }

    private fun reconnect(hubConnection: HubConnection) {
        if (isActive) {
            Completable.defer {
                hubConnection.start().delaySubscription(10, SECONDS, Schedulers.trampoline())
                    .doOnError { logger.error(it) { "Failed to reconnect to Kavita" } }
            }
                .retry { _ -> isActive }
                .subscribeOn(Schedulers.io()).subscribe()
        }
    }

    @Synchronized
    fun stop() {
        isActive = false
        hubConnection?.close()
    }

    @Synchronized
    private fun processNotification(notification: NotificationProgressEvent) {
        if (notification.name == "ScanProgress" && notification.eventType == "ended") {
            val volumes = volumesChanged.toList()
            executor.execute { processEvents(volumes) }
            volumesChanged.clear()
        }
    }

    @Synchronized
    private fun processCoverUpdate(event: CoverUpdateEvent) {
        if (event.body?.get("entityType") == "volume") {
            volumesChanged.add((event.body["id"] as Double).toInt())
        }
    }

    private fun seriesRemoved(event: SeriesRemovedEvent) {
        seriesMatchRepository.delete(MediaServerSeriesId(event.body.seriesId.toString()))
    }

    private fun processEvents(volumeIds: Collection<Int>) {
        val now = clock.instant()

        val volumes: List<KavitaVolume> = volumeIds.map { kavitaClient.getVolume(KavitaVolumeId(it)) }
        val newVolumes: Map<KavitaVolume, Collection<KavitaChapter>> = getNew(volumes)
        val seriesToChaptersMap = newVolumes.keys
            .groupBy { it.seriesId() }
            .mapKeys { (s, _) -> kavitaClient.getSeries(s) }
            .filter { (s, _) -> libraryFilter.test(s.libraryId.toString()) }
            .mapValues { (_, v) -> v.flatMap { newVolumes[it]!! } }

        val libraryToSeriesMap = seriesToChaptersMap.entries
            .groupBy { (series, _) -> series.libraryId }
        libraryToSeriesMap.forEach { (libraryId, series) ->
            val metadataService = metadataServiceProvider.serviceFor(libraryId.toString())
            series.forEach { (series, _) ->
                metadataService.matchSeriesMetadata(MediaServerSeriesId(series.id.toString()))
            }
        }

        notificationService.executeFor(
            seriesToChaptersMap
                .map { (series, chapters) ->
                    MediaServerSeriesId(series.id.toString()) to chapters.map { chapter -> MediaServerBookId(chapter.id.toString()) }
                }.toMap()
        )

        lastScan = now
    }

    private fun getNew(volumes: Collection<KavitaVolume>): Map<KavitaVolume, Collection<KavitaChapter>> {
        return volumes.mapNotNull { volume ->
            val newChapters = volume.chapters
                .filter { it.created.toInstant(OffsetDateTime.now().offset).isAfter(lastScan) }
            if (newChapters.isEmpty()) null
            else volume to newChapters
        }.toMap()
    }

    private fun registerInvocations(hubConnection: HubConnection) {
        // need to add all invocation targets in order to avoid errors in logs
        // register noop handlers
        hubConnection.on("BackupDatabaseProgress", { }, Object::class.java)
        hubConnection.on("BookThemeProgress", { }, Object::class.java)
        hubConnection.on("ConvertBookmarksProgress", { }, Object::class.java)
        hubConnection.on("CleanupProgress", { }, Object::class.java)
        hubConnection.on("CoverUpdateProgress", { }, Object::class.java)
        hubConnection.on("DownloadProgress", { }, Object::class.java)
        hubConnection.on("Error", { }, Object::class.java)
        hubConnection.on("FileScanProgress", { }, Object::class.java)
        hubConnection.on("Info", { }, Object::class.java)
        hubConnection.on("LibraryModified", { }, Object::class.java)
        hubConnection.on("OnlineUsers", { }, Object::class.java)
        hubConnection.on("ScanSeries", { }, Object::class.java)
        hubConnection.on("ScanProgress", { }, Object::class.java)
        hubConnection.on("SendingToDevice", { }, Object::class.java)
        hubConnection.on("SeriesAdded", { }, Object::class.java)
        hubConnection.on("SeriesAddedToCollection", { }, Object::class.java)
        hubConnection.on("SiteThemeProgress", { }, Object::class.java)
        hubConnection.on("UpdateAvailable", { }, Object::class.java)
        hubConnection.on("UserUpdate", { }, Object::class.java)
        hubConnection.on("UserProgressUpdate", { }, Object::class.java)
        hubConnection.on("WordCountAnalyzerProgress", { }, Object::class.java)
    }
}
