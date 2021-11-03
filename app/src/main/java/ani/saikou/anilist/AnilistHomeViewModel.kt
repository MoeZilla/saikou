package ani.saikou.anilist

import ani.saikou.media.Media
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AnilistHomeViewModel : ViewModel() {
    private val listImages : MutableLiveData<ArrayList<String?>> = MutableLiveData<ArrayList<String?>>(arrayListOf())
    fun getListImages(): LiveData<ArrayList<String?>> = listImages
    fun setListImages() = listImages.postValue(anilist.query.getBannerImages())

    private val animeContinue: MutableLiveData<ArrayList<Media>> = MutableLiveData<ArrayList<Media>>(null)
    fun getAnimeContinue(): LiveData<ArrayList<Media>> = animeContinue
    fun setAnimeContinue() = animeContinue.postValue(anilist.query.continueMedia("ANIME"))

    private val mangaContinue: MutableLiveData<ArrayList<Media>> = MutableLiveData<ArrayList<Media>>(null)
    fun getMangaContinue(): LiveData<ArrayList<Media>> = mangaContinue
    fun setMangaContinue() = mangaContinue.postValue(anilist.query.continueMedia("MANGA"))

    private val recommendation: MutableLiveData<ArrayList<Media>> = MutableLiveData<ArrayList<Media>>(null)
    fun getRecommendation(): LiveData<ArrayList<Media>> = recommendation
    fun setRecommendation() = recommendation.postValue(anilist.query.recommendations())
}