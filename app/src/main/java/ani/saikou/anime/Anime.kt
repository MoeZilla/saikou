package ani.saikou.anime

import java.io.Serializable
import java.util.Date

data class Anime(
    var totalEpisodes: Int? = null,

    var episodeDuration: Int? = null,
    var season:String? = null,
    var seasonYear:Int? = null,

    var mainStudioID: Int? = null,
    var mainStudioName: String? =null,

    var youtube: String?=null,
    var nextAiringEpisode: Int? = null,
    var nextAiringEpisodeTime: Date? = null,

    var episodes: ArrayList<Episode>? = null,
    var slug:String?=null,

    var kitsuEpisodes : ArrayList<Episode>?=null
):Serializable