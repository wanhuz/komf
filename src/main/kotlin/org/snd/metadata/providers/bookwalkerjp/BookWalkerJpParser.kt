package org.snd.metadata.providers.bookwalkerjp

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.snd.metadata.BookNameParser
import org.snd.metadata.model.BookRange
import org.snd.metadata.providers.bookwalkerjp.model.BookWalkerJpBook
import org.snd.metadata.providers.bookwalkerjp.model.BookWalkerJpBookId
import org.snd.metadata.providers.bookwalkerjp.model.BookWalkerJpBookListPage
import org.snd.metadata.providers.bookwalkerjp.model.BookWalkerJpSearchResult
import org.snd.metadata.providers.bookwalkerjp.model.BookWalkerJpSeriesBook
import org.snd.metadata.providers.bookwalkerjp.model.BookWalkerJpSeriesId
import java.net.URLDecoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class BookWalkerJpParser {
    private val baseUrl = "https://bookwalker.jp"

    fun parseSearchResults(results: String): Collection<BookWalkerJpSearchResult> {
        val document = Jsoup.parse(results, baseUrl)

        return document.getElementsByClass("m-tile-list").first()?.children()
            ?.map { parseSearchResult(it) }
            ?: emptyList()
    }

    fun parseSeriesBooks(seriesBooks: String): BookWalkerJpBookListPage {
        val document = Jsoup.parse(seriesBooks)
        val books = document.getElementsByClass("o-tile-list").first()?.children()
            ?.map { parseSeriesBook(it) }
            ?: emptyList()
        val pageElement = document.getElementsByClass("pager-area").firstOrNull()?.child(0)
        val currentPage = pageElement?.children()?.first { it.className() == "on" }
            ?.text()?.toInt() ?: 1
        val totalPages = pageElement?.children()?.mapNotNull { it.text().toIntOrNull() }?.max() ?: 1
        return BookWalkerJpBookListPage(page = currentPage, totalPages = totalPages, books = books)
    }

    fun parseBook(book: String): BookWalkerJpBook {
        val document = Jsoup.parse(book)
        val synopsis = document.getElementsByClass("synopsis-text").first()?.wholeText()?.trim()?.replace("\n\n", "\n")
        val image = document.getElementsByClass("book-img").first()?.firstElementChild()?.firstElementChild()?.attr("src")
        val name = document.getElementsByClass("detail-book-title").first()!!.child(0).textNodes().first().text()
        val productDetail = document.getElementsByClass("product-detail").first()!!.child(0)
        val seriesTitleElement = productDetail.children()
            .first { it.child(0).text() == "Series Title" }
            .child(1)
        val seriesTitle = parseSeriesName(seriesTitleElement.text())
        val seriesId = seriesTitleElement.getElementsByTag("a").first()?.attr("href")?.let { parseSeriesId(it) }
        val japaneseTitles = productDetail.children().firstOrNull { it.child(0).text() == "Japanese Title" }
            ?.child(1)?.child(0)?.child(0)
        val japaneseTitle = japaneseTitles?.textNodes()?.firstOrNull()?.text()?.removeSuffix(" (")
        val romajiTitle = japaneseTitles?.getElementsByClass("product-detail-romaji")?.first()?.text()?.removeSuffix(")")
        val authors = productDetail.children().firstOrNull { it.child(0).text() == "Author" || it.child(0).text() == "By (author)" }
            ?.child(1)?.children()?.map { it.text() } ?: emptyList()
        val artists = productDetail.children().firstOrNull { it.child(0).text() == "Artist" || it.child(0).text() == "By (artist)" }
            ?.child(1)?.children()?.map { it.text() } ?: authors
        val publisher = productDetail.children().first { it.child(0).text() == "Publisher" }
            .child(1).text()
        val genres = productDetail.children().firstOrNull { it.child(0).text() == "Genre" }
            ?.child(1)?.child(0)?.children()?.map { it.text() }
            ?: emptyList()
        val availableSince = productDetail.children().firstOrNull { it.child(0).text() == "Available since" }
            ?.child(1)?.text()?.split("/")?.first()
            ?.replace("\\(.*\\) PT ".toRegex(), "")?.trim()
            ?.let { LocalDate.parse(it, DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH)) }

        return BookWalkerJpBook(
            id = parseDocumentBookId(document),
            seriesId = seriesId,
            name = name,
            number = parseBookNumber(name),
            seriesTitle = seriesTitle,
            japaneseTitle = japaneseTitle,
            romajiTitle = romajiTitle,
            artists = artists,
            authors = authors,
            publisher = publisher,
            genres = genres,
            availableSince = availableSince,
            synopsis = synopsis,
            imageUrl = image
        )
    }

    private fun parseSeriesBook(book: Element): BookWalkerJpSeriesBook {
        val titleElement = book.getElementsByClass("a-tile-ttl").first()!!
        return BookWalkerJpSeriesBook(
            id = parseBookId(titleElement.child(0).attr("href")),
            name = titleElement.text(),
            number = parseBookNumber(titleElement.text())
        )
    }

    private fun parseSearchResult(result: Element): BookWalkerJpSearchResult {
        val imageUrl = getSearchResultThumbnail(result)
        val titleElement = result.getElementsByClass("m-book-item__title").first()!!
        val resultUrl = titleElement.child(0).attr("href")

        return BookWalkerJpSearchResult(
            seriesId = parseSeriesId(resultUrl), //Done
            bookId = parseBookId(resultUrl), //Done
            seriesName = parseSeriesName(titleElement.text()), //Done
            imageUrl = imageUrl, //Done
        )
    }

    private fun parseSeriesId(url: String): BookWalkerJpSeriesId? {
        if (url.startsWith("$baseUrl/series/").not()) return null

        return url.removePrefix("$baseUrl/series/")
            .replace("/.*/$".toRegex(), "")
            .let { BookWalkerJpSeriesId(URLDecoder.decode(it, "UTF-8")) }
    }

    private fun parseBookId(url: String): BookWalkerJpBookId {
        return url.removePrefix("$baseUrl/")
            .replace("/.*/$".toRegex(), "")
            .replace("/", "")
            .let { BookWalkerJpBookId(URLDecoder.decode(it, "UTF-8")) }
    }

    private fun parseSeriesName(name: String): String {
        return name.replace("( \\(?Manga\\)?)+$".toRegex(), "")
    }

    private fun getSearchResultThumbnail(result: Element): String? {

        return result.getElementsByClass("m-thumb__image").first()
            ?.child(0)?.attr("data-original")
    }

    private fun parseDocumentBookId(document: Document): BookWalkerJpBookId {
        return parseBookId(document.getElementsByTag("meta").first { it.attr("property") == "og:url" }
            .attr("content"))
    }

    private fun parseBookNumber(name: String): BookRange? {
        return BookNameParser.getVolumes(name)
            ?: "(?i)(?<!chapter)\\s\\d+".toRegex().findAll(name).lastOrNull()?.value?.toDoubleOrNull()
                ?.let { BookRange(it, it) }
    }
}
