package ani.saikou.manga

import ani.saikou.FuzzyDate
import ani.saikou.media.Character
import ani.saikou.media.Media
import java.io.Serializable
import java.util.Date
import kotlin.collections.ArrayList

data class Manga (
    val id: Int,
    val name: String,
    val nameRomaji: String,
    val cover: String? = null,
    val banner: String? = null,

    val userPreferredName: String,
    var userProgress: Int? = null,
    var userStatus: String? = null,
    var userScore: Int = 0,
    var userRepeat:Int = 0,
    var userUpdatedAt:Date?=null,
    var userStartedAt : FuzzyDate?=null,
    var userCompletedAt : FuzzyDate?=null,

    var isFav: Boolean = false,
    val status : String? = null,
    val meanScore: Int? = null,
    var totalChapters: Int? = null,

    var relation: String? =null,

    var startDate: FuzzyDate?=null,
    var endDate: FuzzyDate?=null,
    var source:String? = null,
    var genres:ArrayList<String>?=null,
    var description: String? = null,

    var characters:ArrayList<Character>?=null,
    var relations: ArrayList<Media>?=null,
    var recommendations: ArrayList<Media>?=null,
):Serializable