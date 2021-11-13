package ani.saikou.anime

import java.io.Serializable

data class Episode (
    val number: Int,
    var title : String?=null,
    var desc : String?=null,
    var thumb : String?=null,
    var filler : Boolean = false,
    var link : String? = null,
    var streamLinks : ArrayList<StreamLinks>?=null,
):Serializable{
    data class Quality(
        val url: String,
        val quality: String,
        val size: Int
    ):Serializable

    data class StreamLinks(
        val server: String,
        val quality: List<Quality>,
        val referer:String?
    ):Serializable
}

