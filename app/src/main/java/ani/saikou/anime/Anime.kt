package ani.saikou.anime

import ani.saikou.media.Character
import ani.saikou.media.Media
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

data class Anime(
    val name: String,
    val id: Int,
    val cover: String,
    val banner: String? = null,
    val status : String? = null,
    val meanScore: Int?,

    val userPreferredName: String,
    var userProgress: Int? = null,
    var userStatus: String? = null,
    var userScore: Int = 0,

    var relation: String? =null,

    var episodeDuration: Int? = null,
    var totalEpisodes: Int? = null,
    var nextAiringEpisode: Int? = null,
    var nextAiringEpisodeTime: Date? = null,

    var season:String? = null,
    var seasonYear:Int? = null,
    var startDate: Calendar?=null,
    var endDate: Calendar?=null,

    var source:String? = null,
    var mainStudioID: Int? = null,
    var mainStudioName: String? =null,

    var genres:ArrayList<String>?=null,

    var description: String? = null,

    var characters:ArrayList<Character>?=null,
    var relations: ArrayList<Media>?=null,
    var recommendations: ArrayList<Media>?=null,

    var youtube: String?=null,

):Serializable