package ani.saikou.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ani.saikou.anilist.Anilist
import ani.saikou.anime.Episode
import ani.saikou.anime.source.AnimeParser
import ani.saikou.anime.source.parsers.*
import ani.saikou.kitsu.Kitsu
import ani.saikou.logger
import ani.saikou.manga.MangaChapter
import ani.saikou.manga.source.MangaParser
import ani.saikou.manga.source.parsers.MangaPill

class MediaDetailsViewModel:ViewModel() {
    val parserText = MutableLiveData("")
    private val animeParsers:MutableMap<Int,AnimeParser> = mutableMapOf()
    private val mangaParsers:MutableMap<Int,MangaParser> = mutableMapOf()

    private val media: MutableLiveData<Media> = MutableLiveData<Media>(null)
    fun getMedia(): LiveData<Media> = media
    fun loadMedia(m:Media) { if (media.value==null) media.postValue(Anilist.query.mediaDetails(m)) }

    val userScore = MutableLiveData<Double?>(null)
    val userProgress = MutableLiveData<Int?>(null)
    val userStatus = MutableLiveData<String?>(null)

    private val kitsuEpisodes: MutableLiveData<MutableMap<String,Episode>> = MutableLiveData<MutableMap<String,Episode>>(null)
    fun getKitsuEpisodes() : LiveData<MutableMap<String,Episode>> = kitsuEpisodes
    fun loadKitsuEpisodes(s:String){ if (kitsuEpisodes.value==null) kitsuEpisodes.postValue(Kitsu.getKitsuEpisodesDetails(s))}

    private val episodes: MutableLiveData<MutableMap<Int,MutableMap<String,Episode>>> = MutableLiveData<MutableMap<Int,MutableMap<String,Episode>>>(null)
    private val epsLoaded = mutableMapOf<Int,MutableMap<String,Episode>>()
    fun getEpisodes() : LiveData<MutableMap<Int,MutableMap<String,Episode>>> = episodes
    fun loadEpisodes(media: Media,i:Int,model:MediaDetailsViewModel){
        logger("Loading Episodes : $epsLoaded")
        if(!epsLoaded.containsKey(i)) {
            epsLoaded[i] = when (i) {
                0 -> animeParsers.getOrPut(i, { Gogo(model) })
                1 -> animeParsers.getOrPut(i, { Gogo(model,true) })
                2 -> animeParsers.getOrPut(i, { NineAnime(model) })
                3 -> animeParsers.getOrPut(i, { NineAnime(model,true) })
                else -> animeParsers.getOrPut(i, { Gogo(model) })
            }.getEpisodes(media)
        }
        episodes.postValue(epsLoaded)
    }
    private var streams: MutableLiveData<Episode> = MutableLiveData<Episode>(null)
    fun getStreams() : LiveData<Episode> = streams
    fun loadStreams(episode: Episode,i:Int){
        streams.postValue(animeParsers[i]?.getStream(episode)?:episode)
        streams = MutableLiveData<Episode>(null)
    }

    private val mangaChapters: MutableLiveData<MutableMap<Int,MutableMap<String,MangaChapter>>> = MutableLiveData<MutableMap<Int,MutableMap<String,MangaChapter>>>(null)
    private val mangaLoaded = mutableMapOf<Int,MutableMap<String,MangaChapter>>()
    fun getMangaChapters() : LiveData<MutableMap<Int,MutableMap<String,MangaChapter>>> = mangaChapters
    fun loadMangaChapters(media:Media,i:Int,model: MediaDetailsViewModel){
        logger("Loading Manga Chapters : $mangaLoaded")
        if(!mangaLoaded.containsKey(i)){
            mangaLoaded[i] = when(i){
                0->mangaParsers.getOrPut(i, { MangaPill(model) })
                else -> mangaParsers.getOrPut(i, { MangaPill(model) })
            }.getChapters(media)
        }
        mangaChapters.postValue(mangaLoaded)
    }

    private var mangaChaps: MutableLiveData<MangaChapter> = MutableLiveData<MangaChapter>(null)
    fun getMangaChap() : LiveData<MangaChapter> = mangaChaps
    fun loadMangaChap(chap: MangaChapter,i:Int){
        mangaChaps.postValue(mangaParsers[i]?.getChapter(chap)?:chap)
        mangaChaps = MutableLiveData<MangaChapter>(null)
    }
}