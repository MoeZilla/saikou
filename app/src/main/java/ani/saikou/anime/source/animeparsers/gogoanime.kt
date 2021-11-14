package ani.saikou.anime.source.animeparsers

import ani.saikou.anime.Episode
import ani.saikou.anime.source.extractors.*
import kotlinx.coroutines.*
import org.jsoup.Jsoup

private val host = listOf(
    "http://gogoanime.pe"
)

fun httpsify(text: String): String {
    return if(text.take(2)=="//"){"https:$text"}
    else{text}
}

fun directLinkify(name: String,url: String): Episode.StreamLinks? {
    val domain = Regex("""(?<=^http[s]?:\/\/).+?(?=\/)""").find(url)!!.value
    return if ("gogo" in domain){gogoDirectLink(name,url)}
           else if("sb" in domain){ sbplayDirectLink(name,url) }
           else if ("fplayer" in domain){ fplayerdirectLinks(name,url) }
//           else if ("dood" in domain){parseDood(name,url)}
           else{return null}
}

fun parseEpisodeSources(url: String): ArrayList<Episode.StreamLinks?> = runBlocking {
    val linkForVideos = arrayListOf<Episode.StreamLinks?>()
    withContext(Dispatchers.Default) {
        Jsoup.connect(url).ignoreHttpErrors(true).get().select("div.anime_muti_link > ul > li:not(li.anime)").forEach {
            launch {
                val directLinks = directLinkify(
                    it.select("a").text().replace("Choose this server", ""),
                    httpsify(it.select("a").attr("data-video"))
                )
                if(directLinks != null){linkForVideos.add(directLinks)}
            }
        }
    }
    return@runBlocking (linkForVideos)
}

fun getGogoEpisodes(title: String,dub : Boolean = false): ArrayList<Episode> {
    val pageBody = Jsoup.connect("https://gogoanime.pe/category/$title").get().body()

    val lastEpisode = pageBody.select("ul#episode_page > li:last-child > a").attr("ep_end").toString()
    val movieId = pageBody.select("input#movie_id").attr("value").toString()

//    val responseArray = arrayListOf<Episode>()
    val responseArray = arrayListOf<String>()
    Jsoup.connect("https://ajax.gogo-load.com/ajax/load-list-episode?ep_start=0&ep_end=$lastEpisode&id=$movieId").get().body()
        .select("ul > li > a").reversed().forEach{
            responseArray.add(host[0]+it.attr("href").trim())
        }
    return arrayListOf()
}

fun gogoanimeQuery(name: String): ArrayList<String> {
    // make search and get all links

    val responseArray = arrayListOf<String>()

    Jsoup.connect("${host[0]}/search.html?keyword=${name}").get().body()
        .select(".last_episodes > ul > li div.img > a").forEach {
            responseArray.add(it.attr("href").toString().replace("/category/", ""))
        }

    return responseArray
}

fun getGogoStream(episode: Episode) : Episode{
    episode.streamLinks = parseEpisodeSources(episode.link!!)
    return episode
}


//fun testGOGO(){
//    println(gogoanimeQuery("one piece"))
//    println(getGogoEpisodes("one-piece"))
//    println(parseEpisodeSources("${host[0]}/one-piece-episode-432"))
//}