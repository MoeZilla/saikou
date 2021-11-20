package ani.saikou.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ani.saikou.anilist
import ani.saikou.anime.Episode
import ani.saikou.anime.source.parsers.getGogoEpisodes
import ani.saikou.anime.source.parsers.getGogoStream
import ani.saikou.anime.source.parsers.getTwistEpisodes
import ani.saikou.anime.source.parsers.getTwistStream
import ani.saikou.kitsu
import ani.saikou.logger

class MediaDetailsViewModel:ViewModel() {
    private val media: MutableLiveData<Media> = MutableLiveData<Media>(null)
    fun getMedia(): LiveData<Media> = media
    fun loadMedia(m:Media) { media.postValue(anilist.query.mediaDetails(m)) }

    val userScore = MutableLiveData<Double?>(null)
    val userProgress = MutableLiveData<Int?>(null)
    val userStatus = MutableLiveData<String?>(null)

    private val kitsuEpisodes: MutableLiveData<MutableMap<String,Episode>> = MutableLiveData<MutableMap<String,Episode>>(null)
    fun getKitsuEpisodes() : LiveData<MutableMap<String,Episode>> = kitsuEpisodes
    fun loadKitsuEpisodes(s:String){ if (kitsuEpisodes.value==null) kitsuEpisodes.postValue(kitsu.getKitsuEpisodesDetails(s))}

    private val episodes: MutableLiveData<MutableMap<Int,MutableMap<String,Episode>>> = MutableLiveData<MutableMap<Int,MutableMap<String,Episode>>>(null)
    private val loaded = mutableMapOf<Int,MutableMap<String,Episode>>()
    fun getEpisodes() : LiveData<MutableMap<Int,MutableMap<String,Episode>>> = episodes
    fun loadEpisodes(media: Media,i:Int){
        logger("Loading Episodes : $loaded")
        if(!loaded.containsKey(i)) {
            loaded[i] = when (i) {
                0 -> getGogoEpisodes(media)
                1 -> getGogoEpisodes(media,true)
                2 -> getTwistEpisodes(media)
                else -> getGogoEpisodes(media)
            }
        }
        episodes.postValue(loaded)
    }
    private var streams: MutableLiveData<Episode> = MutableLiveData<Episode>(null)
    fun getStreams() : LiveData<Episode> = streams
    fun loadStreams(episode: Episode,i:Int){
        streams.postValue(when (i) {
            0 -> getGogoStream(episode)
            1 -> getGogoStream(episode)
            2 -> getTwistStream(episode)
            else -> episode
        })
        streams = MutableLiveData<Episode>(null)
    }
}