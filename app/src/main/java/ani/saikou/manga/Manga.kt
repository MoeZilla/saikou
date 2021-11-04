package ani.saikou.manga

import ani.saikou.media.Character
import ani.saikou.media.Media
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

data class Manga (
    val name: String,
    val id: Int,
    val cover: String,
    var banner: String? = null,
    val status : String? = null,
    val meanScore: Int?,

    var totalChapters: Int? = null,

    val userPreferredName: String,
    var userProgress: Int? = null,
    var userStatus: String? = null,
    var userScore: Int = 0,

    var relation: String? =null,

    var season:String? = null,
    var seasonYear:Int? = null,
    var startDate: Calendar?=null,
    var endDate: Calendar?=null,

    var source:String? = null,

    var genres:ArrayList<String>?=null,

    var description: String? = null,

    var characters:ArrayList<Character>?=null,
    var relations: ArrayList<Media>?=null,
    var recommendations: ArrayList<Media>?=null,
):Serializable