package co.appreactor.feedk

import org.w3c.dom.Document
import org.w3c.dom.Element

data class AtomFeed(
    val title: String,
    val links: List<AtomLink>,
    val entries: Result<List<AtomEntry>>,
) : Feed()

data class AtomEntry(
    val id: String,
    val feedId: String,
    val title: String,
    val published: String,
    val updated: String,
    val authorName: String,
    val content: String,
    val links: List<AtomLink>,
)

data class AtomLink(
    val href: String,
    val rel: AtomLinkRel?,
    val type: String,
    val hreflang: String,
    val title: String,
    val length: Long?,
)

sealed class AtomLinkRel {
    object Alternate : AtomLinkRel()
    object Enclosure : AtomLinkRel()
    object Related : AtomLinkRel()
    object Self : AtomLinkRel()
    object Via : AtomLinkRel()
}

fun atomFeed(document: Document): Result<AtomFeed> {
    val documentElement = document.documentElement

    val title = documentElement.getElementsByTagName("title").item(0).textContent
        ?: return Result.failure(Exception("Channel has no title"))

    val links = documentElement.childNodes
        .list()
        .filterIsInstance<Element>()
        .filter { it.tagName == "link" }
        .map { element -> element.toAtomLink().getOrElse { return Result.failure(it) } }

    return Result.success(
        AtomFeed(
            title = title,
            links = links,
            entries = atomEntries(document),
        )
    )
}

fun atomEntries(document: Document): Result<List<AtomEntry>> {
    val feedId = document.getElementsByTagName("id").item(0).textContent
        ?: return Result.failure(Exception("Feed ID is missing"))

    val entries = document.getElementsByTagName("entry")

    val parsedEntries = (0 until entries.length).mapNotNull { index ->
        val entry = entries.item(index) as Element

        // > atom:entry elements MUST contain exactly one atom:id element.
        // Source: https://tools.ietf.org/html/rfc4287
        val id = entry.getElementsByTagName("id").item(0).textContent ?: return@mapNotNull null

        // > atom:entry elements MUST contain exactly one atom:title element.
        // Source: https://tools.ietf.org/html/rfc4287
        val title =
            entry.getElementsByTagName("title").item(0).textContent ?: return@mapNotNull null

        var content = ""

        val contentElements = entry.getElementsByTagName("content")

        if (contentElements.length > 0) {
            content = contentElements.item(0).textContent ?: ""
        }

        // > atom:entry elements MUST contain exactly one atom:updated element.
        // Source: https://tools.ietf.org/html/rfc4287
        val updated =
            entry.getElementsByTagName("updated").item(0).textContent ?: return@mapNotNull null

        // > atom:entry elements MUST contain exactly one atom:updated element.
        // Source: https://tools.ietf.org/html/rfc4287
        val author = entry.getElementsByTagName("author").item(0)

        // TODO
        // atom:entry elements MUST contain one or more atom:author elements, unless the atom:entry
        // contains an atom:source element that contains an atom:author element or, in an Atom Feed
        // Document, the atom:feed element contains an atom:author element itself.
        // Source: https://tools.ietf.org/html/rfc4287
        val authorName = if (author != null && author is Element) {
            author.getElementsByTagName("name").item(0)?.textContent ?: ""
        } else {
            ""
        }

        val linkElements = entry.getElementsByTagName("link")

        val links = linkElements
            .list()
            .filterIsInstance<Element>()
            .map { element -> element.toAtomLink().getOrElse { return Result.failure(it) } }

        AtomEntry(
            id = id,
            feedId = feedId,
            title = title,
            published = updated, // TODO
            updated = updated,
            authorName = authorName,
            content = content,
            links = links,
        )
    }

    return Result.success(parsedEntries)
}

private fun Element.toAtomLink(): Result<AtomLink> {
    val rel = when (getAttribute("rel")) {
        "" -> AtomLinkRel.Alternate
        "alternate" -> AtomLinkRel.Alternate
        "enclosure" -> AtomLinkRel.Enclosure
        "related" -> AtomLinkRel.Related
        "self" -> AtomLinkRel.Self
        "via" -> AtomLinkRel.Via
        else -> return Result.failure(Exception("Unknown rel type: ${getAttribute("rel")}"))
    }

    val lengthAttrName = "length"

    val length = if (getAttribute(lengthAttrName).toLongOrNull() != null) {
        getAttribute(lengthAttrName).toLong()
    } else {
        null
    }

    return Result.success(
        AtomLink(
            href = getAttribute("href"),
            rel = rel,
            type = getAttribute("type"),
            hreflang = getAttribute("hreflang"),
            title = getAttribute("title"),
            length = length
        )
    )
}