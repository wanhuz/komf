package org.snd.metadata.providers.bookwalkerjp.model

import org.snd.metadata.model.BookRange
import java.time.LocalDate

data class BookWalkerJpBook(
    val id: BookWalkerJpBookId,
    val seriesId: BookWalkerJpSeriesId?,
    val name: String,
    val number: BookRange?,

    val seriesTitle: String,
    val japaneseTitle: String?,
    val romajiTitle: String?,
    val artists: Collection<String>,
    val authors: Collection<String>,
    val publisher: String,
    val genres: Collection<String>,
    val availableSince: LocalDate?,

    val synopsis: String?,
    val imageUrl: String?,
)

