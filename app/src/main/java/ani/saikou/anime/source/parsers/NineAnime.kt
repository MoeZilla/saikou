package ani.saikou.anime.source.parsers

import ani.saikou.anime.Episode
import ani.saikou.anime.source.Parser
import ani.saikou.anime.source.SourceAnime
import ani.saikou.loadData
import ani.saikou.logger
import ani.saikou.media.Media
import ani.saikou.media.MediaDetailsViewModel
import ani.saikou.saveData
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.jsoup.Jsoup
import java.net.URLEncoder

class NineAnime(private val model: MediaDetailsViewModel, private val dub:Boolean=false): Parser() {

    //WE DO A LIL TROLLIN
    private val host = listOf(
        "https://animekisa.in/"
        //where 9anime.to
    )

    override fun getStream(episode: Episode): Episode {
        val streams = arrayListOf<Episode.StreamLinks?>()
        Jsoup.connect(episode.link!!).get().select("#servers-list ul.nav li a").forEach { servers ->
            val embedLink = servers.attr("data-embed") // embed link of servers
            val name = servers.select("span").text()
            val token = Regex("(?<=window.skey = )'.*?'").find(
                Jsoup.connect(embedLink).header("referer", host[0]).get().html()
            )?.value?.trim('\'') //token to get the m3u8

            val m3u8Link = Json.decodeFromString<JsonObject>(Jsoup.connect("${embedLink.replace("/e/", "/info/")}&skey=$token")
                .header("referer", host[0])
                .ignoreContentType(true).get().body().text())["media"]!!.jsonObject["sources"]!!.jsonArray[0].jsonObject["file"].toString().trim('"')
            streams.add(Episode.StreamLinks(name,listOf(Episode.Quality(m3u8Link,"Multi",0)),"https://vidstream.pro/"))
        }
        episode.streamLinks = streams
        return episode
    }

    override fun getEpisodes(media: Media): MutableMap<String, Episode> {
        var slug:SourceAnime? = loadData("animekisa${if(dub) "dub" else ""}_${media.id}")
        if (slug==null) {
            val it = (media.nameMAL?:media.name)
            model.parserText.postValue("Searching for $it")
            logger("9anime : Searching for $it")
            val search = search("$!$it | &language%5B%5D=${if(dub) "d" else "s"}ubbed&year%5B%5D=${media.anime?.seasonYear}&sort=default&status=all")
//            val search = googleSearch(it)
            if (search.isNotEmpty()) {
                slug = search[0]
                model.parserText.postValue("Found : ${slug.name}")
                saveData("animekisain${if(dub) "dub" else ""}_${media.id}", slug)
            }
        }
        if (slug!=null) return getSlugEpisodes(slug.link)
        return mutableMapOf()
    }

    override fun search(string: String): ArrayList<SourceAnime> {
        var url = URLEncoder.encode(string, "utf-8")
        if(string.startsWith("$!")){
            val a = string.replace("$!","").split(" | ")
            url = URLEncoder.encode(a[0], "utf-8")+a[1]
        }
        println("${host[0]}filter?keyword=$url")
        val responseArray = arrayListOf<SourceAnime>()
        Jsoup.connect("${host[0]}filter?keyword=$url").get()
            .select("#main-wrapper .film_list-wrap > .flw-item .film-poster").forEach{
                val link = it.select("a").attr("href")
                val title = it.select("img").attr("title")
                val cover = it.select("img").attr("data-src")
                responseArray.add(SourceAnime(link,title,cover))
            }
        return responseArray
    }

    private fun getSlugEpisodes(slug:String): MutableMap<String, Episode>{
        val responseArray = mutableMapOf<String,Episode>()
        val pageBody = Jsoup.connect(slug).get().body()
        pageBody.select(".tab-pane > ul.nav").forEach{
            it.select("li>a").forEach { i ->
                val num = i.text().trim()
                responseArray[num] = Episode(number = num,link = i.attr("href").trim())
            }
        }
        println("Response Episodes : $responseArray")
        return responseArray
    }

//    private fun googleSearch(title:String):ArrayList<String>{
//        val arr = arrayListOf<String>()
//        val a = Jsoup.connect("https://google.com/search?q=site%3Aanimekisa.in${URLEncoder.encode("\"$title\"", "utf-8")}").userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36").ignoreHttpErrors(true).timeout(0).get().body()
//        println(a)
////        val b = Jsoup.connect("https://google.com/"+a.select("noscript>div>a>href").attr("href")).get().body()
//        a.select("div.g>div>div>div>a").forEach {
//            println(it)
//            arr.add(it.attr("href"))
//        }
//        return arr
//    }
}