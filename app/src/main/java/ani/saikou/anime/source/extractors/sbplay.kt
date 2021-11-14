package ani.saikou.anime.source.extractors

import ani.saikou.anime.Episode
import org.jsoup.Jsoup

fun sbplayDirectLink(name: String,link:String): Episode.StreamLinks {

//    println("sbstarted")

    val tempQuality = mutableListOf<Episode.Quality>()
    Jsoup.connect(link.replace("/e/","/d/")).get().select("table > tbody > tr > td > a").forEach {
        val tempArray = it.attr("onclick").split("'")

        tempQuality.add(
            Episode.Quality(
                Jsoup.connect("https://sbplay.one/dl?op=download_orig&id=${tempArray[1]}&mode=${tempArray[3]}&hash=${tempArray[5]}")
                    .get().select(".contentbox > span > a").attr("abs:href"),
                it.text(), 1
            )
        )
    }

//    println("sbFinished")

    return Episode.StreamLinks(
        name,
        tempQuality,
        "http://sbplay.one"
    )
}
