package org.snd.mediaserver.komga.model.dto

import com.squareup.moshi.JsonClass
import org.snd.mediaserver.model.MediaServerSeriesId
import org.snd.mediaserver.model.MediaServerSeriesThumbnail
import org.snd.mediaserver.model.MediaServerThumbnailId

@JsonClass(generateAdapter = true)
data class KomgaSeriesThumbnail(
    val id: String,
    val seriesId: String,
    val type: String,
    val selected: Boolean,
)

fun KomgaSeriesThumbnail.mediaServerSeriesThumbnail() = MediaServerSeriesThumbnail(
    id = MediaServerThumbnailId(id),
    seriesId = MediaServerSeriesId(seriesId),
    type = type,
    selected = selected
)