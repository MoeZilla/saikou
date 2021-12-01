package ani.saikou.anime.source.parsers

import ani.saikou.anime.Episode
import ani.saikou.anime.source.extractors.*
import ani.saikou.getMalTitle
import ani.saikou.media.Media
import kotlinx.coroutines.*
import org.jsoup.Jsoup

private val host = listOf(
    "http://gogoanime.pe"
)

private fun httpsIfy(text: String): String {
    return if(text.take(2)=="//"){"https:$text"}
    else{text}
}

private fun directLinkify(name: String,url: String): Episode.StreamLinks? {
    val domain = Regex("""(?<=^http[s]?://).+?(?=/)""").find(url)!!.value
    return when {
        "gogo" in domain -> gogoDirectLink(name,url)
        "sb" in domain ->  sbplayDirectLink(name,url)
        "fplayer" in domain -> fplayerdirectLinks(name,url)
//        "dood" in domain -> parseDood(name,url)
        else -> null
    }
}

fun getGogoSlugEpisodes(slug: String): MutableMap<String, Episode> {
    val pageBody = Jsoup.connect("${host[0]}/category/$slug").get().body()
    val lastEpisode = pageBody.select("ul#episode_page > li:last-child > a").attr("ep_end").toString()
    val movieId = pageBody.select("input#movie_id").attr("value").toString()

    val responseArray = mutableMapOf<String,Episode>()
    val a = Jsoup.connect("https://ajax.gogo-load.com/ajax/load-list-episode?ep_start=0&ep_end=$lastEpisode&id=$movieId").get().body()
        .select("ul > li > a").reversed()
//    println("Slug Episodes : $a")
        a.forEach{
            val num = it.select(".name").text().replace("EP","").trim()
            responseArray[num] = Episode(number = num,link = host[0]+it.attr("href").trim())
        }
    println("Response Episodes : $responseArray")
    return responseArray
}

fun searchGogo(name: String): ArrayList<String> {
    // make search and get all links
    val search= Regex("[^A-Za-z0-9 ]").replace(name," ")
    println("Searching for : $search")
    val responseArray = arrayListOf<String>()
    Jsoup.connect("${host[0]}/search.html?keyword=$search").get().body()
        .select(".last_episodes > ul > li div.img > a").forEach {
            responseArray.add(it.attr("href").toString().replace("/category/", ""))
        }
    return responseArray
}

fun getGogoStream(episode: Episode) : Episode{
    episode.streamLinks = runBlocking {
        val linkForVideos = arrayListOf<Episode.StreamLinks?>()
        withContext(Dispatchers.Default) {
            Jsoup.connect(episode.link!!).ignoreHttpErrors(true).get().select("div.anime_muti_link > ul > li:not(li.anime)").forEach {
                launch {
                    val directLinks = directLinkify(
                        it.select("a").text().replace("Choose this server", ""),
                        httpsIfy(it.select("a").attr("data-video"))
                    )
                    if(directLinks != null){linkForVideos.add(directLinks)}
                }
            }
        }
        return@runBlocking (linkForVideos)
    }
    return episode
}

fun getGogoEpisodes(media: Media,dub:Boolean=false):MutableMap<String,Episode>{
    val it = media.nameMAL?:media.nameRomaji
    val search = searchGogo(it + if (dub) " (Dub)" else "")
    println("Search : $search")
    if (search.isNotEmpty()) {
        println("Slugged : ${search[0]}")
        return getGogoSlugEpisodes(search[0])
    }
    return mutableMapOf()
}
