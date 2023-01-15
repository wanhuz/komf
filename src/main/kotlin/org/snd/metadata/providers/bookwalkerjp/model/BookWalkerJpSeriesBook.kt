package org.snd.metadata.providers.bookwalkerjp.model

import org.snd.metadata.model.BookRange

data class BookWalkerJpSeriesBook(
    val id: BookWalkerJpBookId,
    val number: BookRange?,
    val name: String
)
