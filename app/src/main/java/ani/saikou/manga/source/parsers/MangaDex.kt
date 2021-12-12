package ani.saikou.manga.source.parsers

import ani.saikou.loadData
import ani.saikou.manga.MangaChapter
import ani.saikou.manga.source.MangaParser
import ani.saikou.media.Media
import ani.saikou.media.Source
import ani.saikou.saveData
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.jsoup.Jsoup
import kotlin.math.roundToInt

class MangaDex:MangaParser() {
    private val host = "https://api.mangadex.org"
    private val limit = 100
    override fun getLinkChapters(link: String): MutableMap<String, MangaChapter> {
        live.postValue("Getting Chapters...")
        val arr = mutableMapOf<String, MangaChapter>()
        val totalChapters = Regex("(?<=\"total\":)\\d+").find(
            Jsoup.connect("$host/manga/$link/feed?limit=0").ignoreContentType(true).get().text()
        )!!.value.toInt()
        live.postValue("Parsing Chapters...")
        (0..totalChapters step 200).reversed().forEach{ index ->
            val jsonResponse = Jsoup.connect("$host/manga/$link/feed?limit=200&order[volume]=desc&order[chapter]=desc&offset=$index").ignoreContentType(true).get().text()
            Json.decodeFromString<JsonObject>(jsonResponse)["data"]!!.jsonArray.reversed().forEach{
                if(it.jsonObject["attributes"]!!.jsonObject["translatedLanguage"].toString() == "\"en\""){
                    val chapter = it.jsonObject["attributes"]!!.jsonObject["chapter"].toString().trim('"')
                    val title = it.jsonObject["attributes"]!!.jsonObject["title"].toString().trim('"')
                    val hash = it.jsonObject["attributes"]!!.jsonObject["hash"].toString().trim('"')
                    val images = arrayListOf<String>()
                    for(page in it.jsonObject["attributes"]!!.jsonObject["data"]!!.jsonArray){
                        images.add("https://uploads.mangadex.org/data/$hash/${page.toString().trim('"')}")
                    }
                    saveData("mangadex_${chapter}_$hash",images)
//                    println(images)
                    arr[chapter] = MangaChapter(chapter,title,hash)
                }
            }
            live.postValue("Chapter Parsing : ${100-(index.toFloat()/totalChapters * 100).roundToInt()}%...")
        }
        live.postValue("Chapters Loaded!")
        return arr
    }

    override fun getChapter(chapter: MangaChapter): MangaChapter {
        chapter.images = loadData<ArrayList<String>>("mangadex_${chapter.number}_${chapter.link}")
        return chapter
    }

    override fun getChapters(media: Media): MutableMap<String, MangaChapter> {
        var source:Source? = loadData("mangadex_${media.id}")
        if (source==null) {
            live.postValue("Searching : ${media.name}")
            val search = search(media.name)
            if (search.isNotEmpty()) {
                println("MangaDex : ${search[0]}")
                source = search[0]
                live.postValue("Found : ${source.name}")
//                saveData("mangadex_${media.id}", source)
            }
        }
        else{
            live.postValue("Loaded : ${source.name}")
        }
        if (source!=null) return getLinkChapters(source.link)
        return mutableMapOf()
    }

    override fun search(string: String): ArrayList<Source> {

        val arr = arrayListOf<Source>()
        val jsonResponse = Jsoup.connect("$host/manga?limit=$limit&title=$string&order[relevance]=desc&includes[]=cover_art").ignoreContentType(true).get().text()
        Json.decodeFromString<JsonObject>(jsonResponse)["data"]!!.jsonArray.forEach{
            val id = it.jsonObject["id"].toString().trim('"') // id
            val title = it.jsonObject["attributes"]!!.jsonObject["title"]!!.jsonObject["en"].toString().trim('"') // en title
            val coverName = Regex("(?<=\"fileName\":\").+?(?=\")").find(it.jsonObject["relationships"]!!.jsonArray.toString())?.value // cover image
            val coverURL = "https://uploads.mangadex.org/covers/$id/$coverName.256.jpg"
            arr.add(Source(id,title,coverURL))
        }
        return arr
    }
}