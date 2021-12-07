package ani.saikou.anime.source.extractors

import ani.saikou.anime.Episode
import ani.saikou.anime.source.Extractor
import org.jsoup.Jsoup

class GogoCDN: Extractor() {
    override fun getStreamLinks(name: String, url: String): Episode.StreamLinks {
        val moiJS = Jsoup.connect(url).header("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.63 Safari/537.36").get().select(".wrapper > .videocontent > script").toString()
        return Episode.StreamLinks(name, listOf(Episode.Quality(Regex("""sources:\[\{file: '(.+m3u8)""").find(moiJS)!!.destructured.component1(), "Multi", null)), "https://goload.one/")
    }
}