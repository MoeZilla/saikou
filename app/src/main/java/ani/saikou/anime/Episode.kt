package ani.saikou.anime

import java.io.Serializable

data class Episode (
    val number: String,
    var title : String?=null,
    var desc : String?=null,
    var thumb : String?=null,
    var filler : Boolean = false,
    var link : String? = null,
    var selectedStream : Int = 0,
    var selectedQuality: Int = 0,
    var streamLinks : ArrayList<StreamLinks?> = arrayListOf(),
):Serializable{
    data class Quality(
        val url: String,
        val quality: String,
        val size: Int?
    ):Serializable

    data class StreamLinks(
        val server: String,
        val quality: List<Quality>,
        val referer:String?
    ):Serializable
}


