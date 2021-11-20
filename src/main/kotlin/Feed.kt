package co.appreactor.feedk

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import javax.xml.parsers.DocumentBuilderFactory

sealed class Feed

fun feed(url: URL, openedConnection: URLConnection? = null): FeedResult {
    val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()

    val connection = openedConnection
        ?: runCatching {
            url.openConnection()
        }.getOrElse {
            return FeedResult.HttpConnectionFailure(it)
        }

    runCatching {
        if (connection is HttpURLConnection) {
            connection.connect()
        }
    }.onFailure {
        return FeedResult.HttpConnectionFailure(it)
    }

    if (connection is HttpURLConnection) {
        val httpResponseCode = connection.responseCode

        if (httpResponseCode != 200) {
            return FeedResult.HttpNotOk(
                responseCode = httpResponseCode,
                message = connection.errorStream.reader().readText()
            )
        }
    }

    val document = runCatching {
        documentBuilder.parse(connection.inputStream)
    }.getOrElse {
        return FeedResult.ParserFailure(it)
    }

    return when (feedType(document)) {
        FeedType.ATOM -> {
            val result = atomFeed(document, url)

            if (result.isSuccess) {
                FeedResult.Success(result.getOrNull()!!)
            } else {
                FeedResult.ParserFailure(result.exceptionOrNull()!!)
            }
        }

        FeedType.RSS -> {
            val result = rssFeed(document)

            if (result.isSuccess) {
                FeedResult.Success(result.getOrNull()!!)
            } else {
                FeedResult.ParserFailure(result.exceptionOrNull()!!)
            }
        }

        FeedType.UNKNOWN -> FeedResult.UnknownFeedType
    }
}