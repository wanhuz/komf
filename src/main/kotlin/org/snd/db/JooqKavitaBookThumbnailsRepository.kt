package org.snd.db

import org.jooq.DSLContext
import org.snd.jooq.Tables.BOOK_THUMBNAILS
import org.snd.jooq.tables.records.BookThumbnailsRecord
import org.snd.mediaserver.model.MediaServer.KAVITA
import org.snd.mediaserver.model.MediaServerBookId
import org.snd.mediaserver.model.MediaServerSeriesId
import org.snd.mediaserver.model.MediaServerThumbnailId
import org.snd.mediaserver.repository.BookThumbnail
import org.snd.mediaserver.repository.BookThumbnailsRepository

class JooqKavitaBookThumbnailsRepository(
    private val dsl: DSLContext,
) : BookThumbnailsRepository {

    override fun findFor(bookId: MediaServerBookId): BookThumbnail? {
        return dsl.selectFrom(BOOK_THUMBNAILS)
            .where(BOOK_THUMBNAILS.BOOK_ID.eq(bookId.id))
            .and(BOOK_THUMBNAILS.SERVER_TYPE.eq(KAVITA.name))
            .fetchOne()
            ?.toModel()
    }

    override fun save(bookThumbnail: BookThumbnail) {
        val record = bookThumbnail.toRecord()
        dsl.insertInto(BOOK_THUMBNAILS, *BOOK_THUMBNAILS.fields())
            .values(record)
            .onConflict()
            .doUpdate()
            .set(record)
            .execute()
    }

    override fun delete(bookId: MediaServerBookId) {
        dsl.delete(BOOK_THUMBNAILS)
            .where(BOOK_THUMBNAILS.BOOK_ID.eq(bookId.id))
            .and(BOOK_THUMBNAILS.SERVER_TYPE.eq(KAVITA.name))
            .execute()
    }

    private fun BookThumbnailsRecord.toModel(): BookThumbnail = BookThumbnail(
        bookId = MediaServerBookId(bookId),
        seriesId = MediaServerSeriesId(seriesId),
        thumbnailId = thumbnailId?.let { MediaServerThumbnailId(it) },
    )

    private fun BookThumbnail.toRecord() = BookThumbnailsRecord(
        bookId.id,
        KAVITA.name,
        seriesId.id,
        thumbnailId?.id,
    )
}
