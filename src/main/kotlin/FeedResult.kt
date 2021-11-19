package co.appreactor.feedk

sealed class FeedResult {
    data class Success(val feed: Feed) : FeedResult()
    data class HttpConnectionFailure(val t: Throwable) : FeedResult()
    data class HttpNotOk(val responseCode: Int, val message: String) : FeedResult()
    data class ParserFailure(val t: Throwable) : FeedResult()
    object UnknownFeedType : FeedResult()
}