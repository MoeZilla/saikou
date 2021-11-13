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

    fun getKitsuEpisodesDetails(title:String): ArrayList<Episode>? {
        val query = """{"query":"query{searchAnimeByTitle(first:1,title:\"$title\"){nodes{id titles{localized}episodes(first:2000){nodes{number titles{canonical}description thumbnail{original{url}}}}}}}"}"""
        val result = getKitsuData(query)
        var arr : ArrayList<Episode>? = null
        println(result)
        val node : JsonElement? = Json.decodeFromString<JsonObject>(result).jsonObject["data"]!!.jsonObject["searchAnimeByTitle"]!!.jsonObject["nodes"]
            if (node!=null){ if (!node.jsonArray.isEmpty()){
                val episodes : JsonElement? = node.jsonArray[0].jsonObject["episodes"]!!.jsonObject["nodes"]
                arr = arrayListOf()
                episodes?.jsonArray?.forEach {
                    arr.add(
                        Episode(
                            number = it.jsonObject["number"]?.toString()?.replace("\"","")?.toInt()!!,
                            title = it.jsonObject["titles"]!!.jsonObject["canonical"]?.toString()?.replace("\"",""),
                            desc = it.jsonObject["description"]!!.jsonObject["en"]?.toString()?.replace("\"",""),
                            thumb =  if  (it.jsonObject["thumbnail"].toString()!="null") it.jsonObject["thumbnail"]!!.jsonObject["original"]!!.jsonObject["url"]?.toString()?.replace("\"","") else null,
                        )
                    )
                }
            }
        }
        return arr
    }
}