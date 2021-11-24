package co.appreactor.feedk

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class Test {

    @Test
    fun `parse feeds`() {
        val feedDirs = listOf(
            File("src/test/resources/atom"),
            File("src/test/resources/rss"),
        )

        val feedFiles = feedDirs.map { it.listFiles()!!.toList() }.flatten()
        println("There are ${feedFiles.size} feed files")

        val feeds = feedFiles.map {
            when (val res = feed(it.toURI().toURL())) {
                is FeedResult.Success -> res.feed
                else -> throw Exception()
            }
        }

        feeds.forEach {
            when (it) {
                is AtomFeed -> assert(it.entries.getOrThrow().isNotEmpty())
                is RssFeed -> assert(it.channel.items.getOrThrow().isNotEmpty())
            }
        }
    }

    @Test
    fun `parse rfc 822 dates`() {
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

    @Test
    fun `HTTP not OK`() {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setResponseCode(404))
            start()
        }

        val path = "/feed.xml"

        val feedResult = feed(server.url(path).toUrl())
        assert(feedResult is FeedResult.HttpNotOk)

        val request = server.takeRequest()
        assertEquals(path, request.path)

        server.shutdown()
    }
}