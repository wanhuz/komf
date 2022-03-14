package org.snd.komga.model.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BookThumbnail(
    val id: String,
    val bookId: String,
    val type: String,
    val selected: Boolean,
)

