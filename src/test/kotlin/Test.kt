package co.appreactor.feedk

import java.io.File
import kotlin.test.Test

class Test {

    @Test
    fun `Parse Atom feeds`() {
        val feedFiles = File("src/test/resources/atom").listFiles()!!.toList()
        println("There are ${feedFiles.size} feed files")

        val feeds = feedFiles.map {
            when (val res = feed(it.inputStream(), "text/xml")) {
                is FeedResult.Success -> res.feed
                is FeedResult.UnsupportedMediaType -> throw Exception("Unsupported media type")
                is FeedResult.UnsupportedFeedType -> throw Exception("Unsupported feed type")
                is FeedResult.IOError -> throw Exception(res.cause)
                is FeedResult.ParserError -> throw Exception(res.cause)
            }
        }

        feeds.forEach {
            when (it) {
                is AtomFeed -> assert(it.entries.getOrThrow().isNotEmpty())
                else -> throw Exception("Not an Atom feed")
            }
        }
    }

    @Test
    fun `Parse RSS feeds`() {
        val feedFiles = File("src/test/resources/rss").listFiles()!!.toList()
        println("There are ${feedFiles.size} feed files")

        val feeds = feedFiles.map {
            when (val res = feed(it.inputStream(), "text/xml")) {
                is FeedResult.Success -> res.feed
                is FeedResult.UnsupportedMediaType -> throw Exception("Unsupported media type")
                is FeedResult.UnsupportedFeedType -> throw Exception("Unsupported feed type")
                is FeedResult.IOError -> throw Exception(res.cause)
                is FeedResult.ParserError -> throw Exception(res.cause)
            }
        }

        feeds.forEach {
            when (it) {
                is RssFeed -> assert(it.channel.items.getOrThrow().isNotEmpty())
                else -> throw Exception("Not an Atom feed")
            }
        }
    }

    @Test
    fun `Parse RFC 822 dates`() {
        val dates = listOf(
            "Mon, 21 Jan 2019 16:06:12 GMT",
            "Mon, 27 Jan 2020 17:55:00 EST",
            "Sat, 13 Mar 2021 08:47:51 -0500",
        )

        dates.forEach { date ->
            println("Testing date: $date")
            val parsedDate = RFC_822.parse(date).toInstant()
            println("Parsed date: $parsedDate")
        }
    }
}