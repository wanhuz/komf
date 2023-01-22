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
        val books = document.getElementsByClass("m-tile-list").first()?.children()
            ?.map { parseSeriesBook(it) }
            ?: emptyList()
        val pageElement = document.getElementsByClass("o-pager-box").firstOrNull()?.child(0)
        val currentPage = pageElement?.children()?.first { it.className() == "o-pager-box-num__current" }
            ?.text()?.toInt() ?: 1
        val totalPages = pageElement?.children()?.mapNotNull { it.text().toIntOrNull() }?.max() ?: 1
        return BookWalkerJpBookListPage(page = currentPage, totalPages = totalPages, books = books)
    }

    fun parseBook(book: String): BookWalkerJpBook {
        val document = Jsoup.parse(book)
        val synopsis = document.getElementsByClass("p-summary__text").first()?.wholeText()?.trim()?.replace("\n\n", "\n")
        val image = document.getElementsByClass("p-main__thumb").first()?.firstElementChild()?.child(1)?.firstElementChild()?.attr("data-original")
        val name = document.getElementsByClass("p-main__title").text()
            .replace("【.*】".toRegex(), "")
        val productDetail = document.getElementsByClass("p-information__data")
        val seriesTitleElement = productDetail.select("dt:contains(シリーズ) + dd").first()!!.child(0)
        val seriesTitle = seriesTitleElement.text()
            .replace("【.*】".toRegex(), "")
            .replace("（.*）".toRegex(), "")
        val seriesId = seriesTitleElement.getElementsByTag("a").first()?.attr("href")?.let { parseSeriesId(it) }
        val japaneseTitles = null
        val japaneseTitle = seriesTitle
        val romajiTitle = null
        val authorDetail = productDetail.select("dt:contains(著者) + dd").first()!!.child(0)
        val authors = authorDetail.children().select("li:contains(著者), li:contains(原作), li:contains(著)")
            .map {it.text().replaceAfter("(", "").replace("(", "")} ?: emptyList()
        val artists = authorDetail.children().select("li:contains(作画), li:contains(イラスト)")
            .map {it.text().replaceAfter("(", "").replace("(", "")} ?: emptyList()
        val publisher = productDetail.select("dt:contains(出版社) + dd").first()?.child(0)!!.text()
        val genres = document.getElementsByClass("m-icon-tag-list").first()?.children()?.map {it.text()} ?: emptyList()
        val availableSince =  productDetail.select("dt:contains(配信開始日) + dd").first()?.text()
            .let { LocalDate.parse(it, DateTimeFormatter.ofPattern("y/M/d", Locale.ENGLISH)) }
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
        val titleElement = book.getElementsByClass("m-book-item__title").first()!!
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
            seriesId = parseSeriesId(resultUrl),
            bookId = parseBookId(resultUrl),
            seriesName = parseSeriesName(titleElement.text()),
            imageUrl = imageUrl,
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
            ?: "(?i)(?<!chapter)([０-９]+|\\s\\d+)".toRegex().findAll(name).lastOrNull()?.value?.let {parseJpnNumtoEngNum(it)}?.toDoubleOrNull()
                ?.let { BookRange(it, it) }
    }

    fun parseJpnNumtoEngNum(str: String):String {
        var result = ""
        var en = '0'
        for (ch in str) {
            en = ch
            when (ch) {
                '０' -> en = '0'
                '１' -> en = '1'
                '２' -> en = '2'
                '３' -> en = '3'
                '４' -> en = '4'
                '５' -> en = '5'
                '６' -> en = '6'
                '７' -> en = '7'
                '８' -> en = '8'
                '９' -> en = '9'
            }
            result = "${result}$en"
        }
        return result
    }
}
