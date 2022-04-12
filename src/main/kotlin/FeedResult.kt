package co.appreactor.feedk

sealed class FeedResult {
    data class Success(val feed: Feed) : FeedResult()
    data class UnsupportedMediaType(val mediaType: String) : FeedResult()
    object UnsupportedFeedType : FeedResult()
    data class IOError(val t: Throwable) : FeedResult()
    data class ParserError(val t: Throwable) : FeedResult()
}