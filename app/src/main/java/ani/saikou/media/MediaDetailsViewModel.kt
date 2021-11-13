package ani.saikou.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ani.saikou.anilist
import ani.saikou.anime.Episode
import ani.saikou.anime.source.getGogoEpisodes
import ani.saikou.kitsu

class MediaDetailsViewModel:ViewModel() {
    private val media: MutableLiveData<Media> = MutableLiveData<Media>(null)
    fun getMedia(): LiveData<Media> = media
    fun loadMedia(m:Media) { media.postValue(anilist.query.mediaDetails(m)) }

    private val kitsuEpisodes: MutableLiveData<ArrayList<Episode>> = MutableLiveData<ArrayList<Episode>>(null)
    fun getKitsuEpisodes() : LiveData<ArrayList<Episode>> = kitsuEpisodes
    fun loadKitsuEpisodes(s:String){ if (kitsuEpisodes.value==null) kitsuEpisodes.postValue(kitsu.getKitsuEpisodesDetails(s))}

    private val episodes: MutableLiveData<ArrayList<Episode>> = MutableLiveData<ArrayList<Episode>>(null)
    private val loaded = mutableMapOf<Int,Boolean?>()
    fun getEpisodes() : LiveData<ArrayList<Episode>> = kitsuEpisodes
    fun loadEpisodes(media: Media,i:Int){
        if(!loaded.containsKey(i)) {
            val arr = when (i) {
                0 -> getGogoEpisodes(media.nameRomaji)
                1 -> getGogoEpisodes(media.nameRomaji,true)
                else -> getGogoEpisodes(media.nameRomaji)
            }
            loaded[i] = true
            episodes.postValue(arr)
        }
        else episodes.postValue(episodes.value)
    }
}