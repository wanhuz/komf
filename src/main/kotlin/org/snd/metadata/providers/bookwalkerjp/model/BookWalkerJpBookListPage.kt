package org.snd.metadata.providers.bookwalkerjp.model

data class BookWalkerJpBookListPage(
    val page: Int,
    val totalPages: Int,
    val books: Collection<BookWalkerJpSeriesBook>
)

