package ani.saikou.manga.source.parsers

import ani.saikou.loadData
import ani.saikou.manga.MangaChapter
import ani.saikou.manga.source.MangaParser
import ani.saikou.media.Media
import ani.saikou.media.MediaDetailsViewModel
import ani.saikou.media.Source
import ani.saikou.saveData
import org.jsoup.Jsoup

class MangaPill(private val model: MediaDetailsViewModel):MangaParser() {
    override fun getChapter(chapter: MangaChapter): MangaChapter {
        chapter.images = arrayListOf()
        Jsoup.connect(chapter.link!!).get().select("img.lazy.js-page").forEach {
            chapter.images!!.add(it.attr("data-src"))
        }
        return chapter
    }

    private fun getLinkChapters(link:String):MutableMap<String,MangaChapter>{
        val responseArray = mutableMapOf<String, MangaChapter>()
        Jsoup.connect(link).get().select("#chapters > div > a").reversed().forEach{
            val chap = it.text().replace("Chapter ","")
            responseArray[chap] = MangaChapter(chap,link=it.attr("abs:href"))
        }
        return responseArray
    }

    override fun getChapters(media: Media): MutableMap<String, MangaChapter> {
        var source:Source? = loadData("mangapil_${media.id}")
        if (source==null) {
            model.parserText.postValue("Searching : ${media.name}")
            val search = search(media.name)
            if (search.isNotEmpty()) {
                println("MangaPill : ${search[0]}")
                source = search[0]
                model.parserText.postValue("Found : ${source.name}")
                saveData("mangapill_${media.id}", source)
            }
        }
        else{
            model.parserText.postValue("Selected : ${source.name}")
        }
        if (source!=null) return getLinkChapters(source.link)
        return mutableMapOf()
    }

    override fun search(string: String): ArrayList<Source> {
        val response = arrayListOf<Source>()
        Jsoup.connect("https://mangapill.com/quick-search?q=$string").get().select(".bg-card").forEach{
            response.add(Source(
                link = it.attr("abs:href"),
                name = it.select(".flex > div").text(),
                cover = it.select("img").attr("src")
            ))
        }
        return response
    }
}