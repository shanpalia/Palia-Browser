package com.example.data.repository

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import android.util.Xml
import java.net.HttpURLConnection
import java.net.URL


data class NewsArticle(
    val title: String,
    val source: String,
    val published: String,
    val url: String
)

class NewsRepository {
    suspend fun fetch(language: String): List<NewsArticle> = withContext(Dispatchers.IO) {
        val (hl, gl, ceid) = when (language) {
            "Hindi" -> Triple("hi", "IN", "IN:hi")
            "Bengali" -> Triple("bn", "IN", "IN:bn")
            "Telugu" -> Triple("te", "IN", "IN:te")
            "Tamil" -> Triple("ta", "IN", "IN:ta")
            "Marathi" -> Triple("mr", "IN", "IN:mr")
            "Gujarati" -> Triple("gu", "IN", "IN:gu")
            "Kannada" -> Triple("kn", "IN", "IN:kn")
            "Malayalam" -> Triple("ml", "IN", "IN:ml")
            "Punjabi" -> Triple("pa", "IN", "IN:pa")
            else -> Triple("en", "IN", "IN:en")
        }
        val url = "https://news.google.com/rss?hl=$hl&gl=$gl&ceid=${Uri.encode(ceid)}"
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10000
            readTimeout = 10000
            requestMethod = "GET"
            setRequestProperty("User-Agent", "PaliaBrowser/1.0 (Android)")
        }
        try {
            connection.inputStream.use { stream ->
                val parser = Xml.newPullParser()
                parser.setInput(stream, "UTF-8")
                val result = mutableListOf<NewsArticle>()
                var event = parser.eventType
                var insideItem = false
                var title = ""
                var link = ""
                var source = "Google News"
                var pubDate = ""
                var tag = ""
                while (event != XmlPullParser.END_DOCUMENT && result.size < 12) {
                    when (event) {
                        XmlPullParser.START_TAG -> {
                            tag = parser.name
                            if (tag == "item") {
                                insideItem = true
                                title = ""
                                link = ""
                                source = "Google News"
                                pubDate = ""
                            }
                        }
                        XmlPullParser.TEXT -> if (insideItem) {
                            when (tag) {
                                "title" -> title = parser.text.trim()
                                "link" -> link = parser.text.trim()
                                "pubDate" -> pubDate = parser.text.trim()
                                "source" -> source = parser.text.trim()
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name == "item" && insideItem) {
                                if (title.isNotBlank() && link.isNotBlank()) {
                                    result += NewsArticle(title, source, formatAge(pubDate), link)
                                }
                                insideItem = false
                            }
                            tag = ""
                        }
                    }
                    event = parser.next()
                }
                result
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun formatAge(raw: String): String {
        if (raw.isBlank()) return "Latest"
        return try {
            val cleaned = raw.substringBefore(" GMT").substringBefore(" UTC")
            val formatter = java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", java.util.Locale.US)
            formatter.timeZone = java.util.TimeZone.getTimeZone("GMT")
            val date = formatter.parse(cleaned) ?: return "Latest"
            val minutes = ((System.currentTimeMillis() - date.time) / 60000L).coerceAtLeast(0)
            when {
                minutes < 60 -> "${minutes.coerceAtLeast(1)}m ago"
                minutes < 1440 -> "${minutes / 60}h ago"
                else -> "${minutes / 1440}d ago"
            }
        } catch (_: Exception) { "Latest" }
    }
}
