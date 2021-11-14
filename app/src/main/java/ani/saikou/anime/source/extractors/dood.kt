package ani.saikou.anime.source.extractors

import ani.saikou.anime.Episode
import org.jsoup.Jsoup

// works, need cloudflare bypass

fun parseDood(name:String, url: String): Episode.StreamLinks {
    println(name)
    val stockPage = Jsoup.connect(url.replace("/e/","/d/")).get()
    val size = stockPage.select("div.size").text().toInt()
    println(
    Jsoup.connect(stockPage.select(".download-content > a").attr("href")).get().select(".container a").attr("onclick").split("'")[1]
    )

    return Episode.StreamLinks("", listOf(Episode.Quality("", "", size)), "lol")
}