package org.snd.komga.repository

import org.snd.komga.model.MatchedBook
import org.snd.komga.model.dto.KomgaBookId

interface MatchedBookRepository {

    fun findFor(bookId: KomgaBookId): MatchedBook?

    fun save(matchedBook: MatchedBook)

    fun delete(matchedBook: MatchedBook)
}
