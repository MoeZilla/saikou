package ani.saikou.anilist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ani.saikou.anilist
import ani.saikou.media.Media

class AnilistAnimeViewModel : ViewModel() {
    private val type = "ANIME"
    private val trending: MutableLiveData<ArrayList<Media>> = MutableLiveData<ArrayList<Media>>(null)
    fun getTrending(): LiveData<ArrayList<Media>> = trending
    fun loadTrending() = trending.postValue(anilist.query.search(type, sort="TRENDING_DESC"))
}