package ani.saikou.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ani.saikou.anilist.Anilist
import ani.saikou.anime.Episode
import ani.saikou.anime.source.Parser
import ani.saikou.anime.source.parsers.*
import ani.saikou.kitsu.Kitsu
import ani.saikou.logger

class MediaDetailsViewModel:ViewModel() {
    private val parsers:MutableMap<Int,Parser> = mutableMapOf()

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
    private val loaded = mutableMapOf<Int,MutableMap<String,Episode>>()
    fun getEpisodes() : LiveData<MutableMap<Int,MutableMap<String,Episode>>> = episodes
    fun loadEpisodes(media: Media,i:Int,model:MediaDetailsViewModel){
        logger("Loading Episodes : $loaded")
        if(!loaded.containsKey(i)) {
            loaded[i] = when (i) {
                0 -> parsers.getOrPut(i, { Gogo(model) })
                1 -> parsers.getOrPut(i, { Gogo(model,true) })
                2 -> parsers.getOrPut(i, { Twist() })
                else -> parsers.getOrPut(i, { Gogo(model) })
            }.getEpisodes(media)
        }
        episodes.postValue(loaded)
    }
    private var streams: MutableLiveData<Episode> = MutableLiveData<Episode>(null)
    fun getStreams() : LiveData<Episode> = streams
    fun loadStreams(episode: Episode,i:Int){
        streams.postValue(parsers[i]?.getStream(episode)?:episode)
        streams = MutableLiveData<Episode>(null)
    }

    val parserText = MutableLiveData("")
}