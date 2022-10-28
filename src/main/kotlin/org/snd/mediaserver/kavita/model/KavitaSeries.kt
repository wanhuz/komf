package org.snd.mediaserver.kavita.model

import com.squareup.moshi.JsonClass
import org.snd.mediaserver.model.MediaServerLibraryId
import org.snd.mediaserver.model.MediaServerSeries
import org.snd.mediaserver.model.MediaServerSeriesId
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
class KavitaSeries(
    val id: Int,
    val name: String,
    val libraryId: Int,
    val libraryName: String,
    val originalName: String,
    val localizedName: String?,
    val sortName: String,
    val summary: String?,
    val pages: Int,
    val pagesRead: Int,
    val latestReadDate: LocalDateTime,
    val lastChapterAdded: LocalDateTime,
    val userRating: Int,
    val userReview: String?,
    val format: Int,
    val created: LocalDateTime,
    val wordCount: Int,
    val minHoursToRead: Int,
    val maxHoursToRead: Int,
    val avgHoursToRead: Int,
    val folderPath: String,
    val lastFolderScanned: LocalDateTime,
    val coverImageLocked: Boolean,
    val localizedNameLocked: Boolean,
    val nameLocked: Boolean,
    val sortNameLocked: Boolean,
) {
    fun seriesId() = KavitaSeriesId(id)
    fun libraryId() = KavitaLibraryId(libraryId)
}

fun KavitaSeries.mediaServerSeries(metadata: KavitaSeriesMetadata): MediaServerSeries {
    return MediaServerSeries(
        id = MediaServerSeriesId(id.toString()),
        libraryId = MediaServerLibraryId(libraryId.toString()),
        name = originalName,
        booksCount = null,
        metadata = metadata.mediaServerSeriesMetadata(this),
        url = folderPath,
        deleted = false,
    )
}
