package ani.saikou.kitsu

import ani.saikou.anime.Episode
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import org.jsoup.Connection.*
import org.jsoup.Jsoup

class Kitsu {
    private fun getKitsuData(query:String): String {
        return Jsoup.connect("https://kitsu.io/api/graphql")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("Connection", "keep-alive")
            .header("DNT", "1")
            .header("Origin", "https://kitsu.io")
            .ignoreContentType(true)
            .ignoreHttpErrors(true)
            .requestBody(query)
            .method(Method.POST).execute().body()
    }

    fun getKitsuEpisodesDetails(title:String): MutableMap<String,Episode>? {
        val query = """{"query":"query{searchAnimeByTitle(first:1,title:\"$title\"){nodes{id titles{localized}episodes(first:2000){nodes{number titles{canonical}description thumbnail{original{url}}}}}}}"}"""
        val result = getKitsuData(query)
        println(result)
        var arr :  MutableMap<String,Episode>? = null
        val node : JsonElement? = Json.decodeFromString<JsonObject>(result).jsonObject["data"]!!.jsonObject["searchAnimeByTitle"]!!.jsonObject["nodes"]
            if (node!=null){ if (!node.jsonArray.isEmpty()){
                val episodes : JsonElement? = node.jsonArray[0].jsonObject["episodes"]!!.jsonObject["nodes"]
                arr = mutableMapOf()
                episodes?.jsonArray?.forEach {
                    println(it.toString())
                    if (it!=JsonNull){
                        arr[it.jsonObject["number"]?.toString()?.replace("\"", "")!!] = Episode(
                            number = it.jsonObject["number"]?.toString()?.replace("\"", "")!!,
                            title = if (it.jsonObject["titles"]!!.jsonObject["canonical"]!=JsonNull) it.jsonObject["titles"]!!.jsonObject["canonical"]?.toString()?.replace("\"", "") else null,
                            desc = if (it.jsonObject["description"]!!.jsonObject["en"]!=JsonNull) it.jsonObject["description"]!!.jsonObject["en"]?.toString()?.replace("\"", "")?.replace("\\n", "\n") else null,
                            thumb = if (it.jsonObject["thumbnail"]!=JsonNull) "https://image-compression-api.herokuapp.com/?q=" + it.jsonObject["thumbnail"]!!.jsonObject["original"]!!.jsonObject["url"]?.toString()?.replace("\"", "") else null,
                        )
                    }
                }
            }
        }
        return arr
    }
}