package ani.saikou.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ani.saikou.anilist.anilist

class MediaDetailsViewModel:ViewModel() {
    private val media: MutableLiveData<Media> = MutableLiveData<Media>(null)
    fun getMedia(): LiveData<Media> = media
    fun loadMedia(m:Media) = media.postValue(anilist.query.mediaDetails(m))
}