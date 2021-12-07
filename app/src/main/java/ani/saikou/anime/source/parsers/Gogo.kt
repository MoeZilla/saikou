package ani.saikou.anime.source.parsers

import android.annotation.SuppressLint
import ani.saikou.anime.Episode
import ani.saikou.anime.source.Extractor
import ani.saikou.anime.source.Parser
import ani.saikou.anime.source.SourceAnime
import ani.saikou.anime.source.extractors.*
import ani.saikou.loadData
import ani.saikou.logger
import ani.saikou.media.Media
import ani.saikou.media.MediaDetailsViewModel
import ani.saikou.saveData
import kotlinx.coroutines.*
import org.jsoup.Jsoup

@SuppressLint("SetTextI18n")
class Gogo(private val model:MediaDetailsViewModel,private val dub:Boolean=false): Parser(){
    private val host = listOf(
        "http://gogoanime.cm"
    )

    private fun httpsIfy(text: String): String {
        return if(text.take(2)=="//"){"https:$text"}
        else{text}
    }

    private fun directLinkify(name: String,url: String): Episode.StreamLinks? {
        val domain = Regex("""(?<=^http[s]?://).+?(?=/)""").find(url)!!.value
        val extractor : Extractor?=when {
            "gogo" in domain -> GogoCDN()
            "sb" in domain ->  SBPlay()
            "fplayer" in domain -> FPlayer()
            "fembed" in domain -> FPlayer()
//            "dood" in domain -> Doodla()
            else -> null
        }
        return extractor?.getStreamLinks(name,url)
    }

    override fun getStream(episode: Episode): Episode {
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

    override fun getEpisodes(media: Media): MutableMap<String, Episode> {
        var slug:SourceAnime? = loadData("gogo${if(dub) "dub" else ""}_${media.id}")
        if (slug==null) {
            val it = (media.nameMAL ?: media.nameRomaji) + if (dub) " (Dub)" else ""
            model.parserText.postValue("Searching for $it")
            logger("Gogo : Searching for $it")
            val search = search(it)
            if (search.isNotEmpty()) {
                slug = search[0]
                model.parserText.postValue("Found : ${slug.name}")
                saveData("gogo${if(dub) "dub" else ""}_${media.id}", slug)
            }
        }
        else{
            model.parserText.postValue("Selected : ${slug.name}")
        }
        if (slug!=null) return getSlugEpisodes(slug.link)
        return mutableMapOf()
    }

    override fun search(string: String): ArrayList<SourceAnime> {
        // make search and get all links
        println("Searching for : $string")
        val responseArray = arrayListOf<SourceAnime>()
        Jsoup.connect("${host[0]}/search.html?keyword=$string").get().body()
            .select(".last_episodes > ul > li div.img > a").forEach {
                val link = it.attr("href").toString().replace("/category/", "")
                val title = it.attr("title")
                val cover = it.select("img").attr("src")
                responseArray.add(SourceAnime(link,title,cover))
            }
        return responseArray
    }

    private fun getSlugEpisodes(slug: String): MutableMap<String, Episode> {
        val pageBody = Jsoup.connect("${host[0]}/category/$slug").get().body()
        val lastEpisode = pageBody.select("ul#episode_page > li:last-child > a").attr("ep_end").toString()
        val animeId = pageBody.select("input#movie_id").attr("value").toString()

        val responseArray = mutableMapOf<String,Episode>()
        val a = Jsoup.connect("https://ajax.gogo-load.com/ajax/load-list-episode?ep_start=0&ep_end=$lastEpisode&id=$animeId").get().body().select("ul > li > a").reversed()
        a.forEach{
            val num = it.select(".name").text().replace("EP","").trim()
            responseArray[num] = Episode(number = num,link = host[0]+it.attr("href").trim())
        }
        println("Response Episodes : $responseArray")
        return responseArray
    }
}
