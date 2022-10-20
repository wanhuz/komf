package org.snd.metadata.providers.bookwalker

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import org.snd.infra.HttpClient
import org.snd.infra.HttpException
import org.snd.metadata.model.Image
import org.snd.metadata.providers.bookwalker.model.BookWalkerBook
import org.snd.metadata.providers.bookwalker.model.BookWalkerBookId
import org.snd.metadata.providers.bookwalker.model.BookWalkerBookListPage
import org.snd.metadata.providers.bookwalker.model.BookWalkerSearchResult
import org.snd.metadata.providers.bookwalker.model.BookWalkerSeriesId

class BookWalkerClient(
    private val client: HttpClient
) {
    private val baseUrl: HttpUrl = "https://global.bookwalker.jp/".toHttpUrl()
    private val parser = BookWalkerParser()

    fun searchSeries(name: String): Collection<BookWalkerSearchResult> {
        val request = Request.Builder().url(
            baseUrl.newBuilder().addPathSegments("search/")
                .addQueryParameter("word", name)
                .addQueryParameter("qcat", "2")
                .addQueryParameter("np", "0")
                .build()
        ).build()

        return try {
            parser.parseSearchResults(client.execute(request))
        } catch (e: HttpException) {
            if (e.code == 404) emptyList()
            else throw e
        }
    }

    fun getSeriesBooks(id: BookWalkerSeriesId, page: Int): BookWalkerBookListPage {
        val request = Request.Builder().url(
            baseUrl.newBuilder().addPathSegments("series/${id.id}")
                .addQueryParameter("page", page.toString())
                .build()
        ).build()

        return parser.parseSeriesBooks(client.execute(request))
    }

    fun getBook(id: BookWalkerBookId): BookWalkerBook {
        val request = Request.Builder().url(
            baseUrl.newBuilder().addPathSegments(id.id)
                .build()
        ).build()

        return parser.parseBook(client.execute(request))
    }

    fun getThumbnail(url: HttpUrl?): Image? {
        return url?.let {
            val request = Request.Builder().url(url).build()
            val bytes = client.executeWithByteResponse(request)
            Image(bytes)
        }
    }
}
