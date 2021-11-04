package ani.saikou.anilist

import android.util.Log
import ani.saikou.media.Media
import ani.saikou.anime.Anime
import ani.saikou.logger
import ani.saikou.manga.Manga
import ani.saikou.media.Character
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.jsoup.Jsoup
import java.util.*
import kotlin.collections.ArrayList

class AnilistQueries{

    private fun getQuery(query:String): String = runBlocking{
        return@runBlocking withContext(Dispatchers.Default) {
            Jsoup.connect("https://graphql.anilist.co/")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer ${anilist.token}")
                .requestBody("""{"query":"$query"}""")
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .post().body().text()
        }
    }

    fun getUserData():Boolean{
        return try{
            val response = Json.decodeFromString<JsonObject>(
                getQuery("""{Viewer {name avatar{medium}id statistics{anime{episodesWatched}manga{chaptersRead}}}}""")
            )["data"]!!.jsonObject["Viewer"]!!

            anilist.userid = response.jsonObject["id"].toString().toInt()
            anilist.username = response.jsonObject["name"].toString().trim('"')
            anilist.avatar = response.jsonObject["avatar"]!!.jsonObject["medium"].toString().trim('"')
            anilist.episodesWatched = response.jsonObject["statistics"]!!.jsonObject["anime"]!!.jsonObject["episodesWatched"].toString().toInt()
            anilist.chapterRead = response.jsonObject["statistics"]!!.jsonObject["manga"]!!.jsonObject["chaptersRead"].toString().toInt()
            true
        } catch (e: Exception){
            logger(e)
            false
        }
    }

    fun mediaDetails(media:Media):Media{
        val id  = if (media.anime!=null) media.anime.id else media.manga!!.id
        val response = getQuery("""{Media(id:$id){nextAiringEpisode{episode airingAt}source duration season seasonYear startDate{year month day}endDate{year month day}genres studios(isMain:true){nodes{id name siteUrl}}description characters(sort:FAVOURITES_DESC,perPage:25,page:1){edges{role node{id image{medium}name{userPreferred}}}}relations{edges{relationType(version:2)node{id chapters episodes episodes chapters nextAiringEpisode{episode}meanScore title{romaji userPreferred}type status(version:2)bannerImage coverImage{large}}}}recommendations{nodes{mediaRecommendation{id chapters episodes chapters nextAiringEpisode{episode}meanScore title{romaji userPreferred}type status(version:2)bannerImage coverImage{large}}}}mediaListEntry{id status score advancedScores progress progressVolumes repeat updatedAt startedAt{year month day}completedAt{year month day}user{id name}}isFavourite streamingEpisodes{title thumbnail}externalLinks{url}}}""")
        Log.e("JSON",response)
        val json = Json.decodeFromString<JsonObject>(response)["data"]!!
        if (json.toString()!="null") {
            val it = json.jsonObject["Media"]!!
            if (media.anime != null) {

                media.anime.source = it.jsonObject["source"]!!.toString().trim('"')
                media.anime.episodeDuration = it.jsonObject["duration"]!!.toString().toInt()
                media.anime.season = it.jsonObject["season"]!!.toString().trim('"')
                media.anime.seasonYear = it.jsonObject["seasonYear"]!!.toString().toInt()
                val startDate = Calendar.getInstance()
                if(it.jsonObject["startDate"]!!.jsonObject["year"].toString()!="null") startDate.set(Calendar.YEAR,it.jsonObject["startDate"]!!.jsonObject["year"].toString().toInt())
                if(it.jsonObject["startDate"]!!.jsonObject["month"].toString()!="null") startDate.set(Calendar.MONTH,it.jsonObject["startDate"]!!.jsonObject["month"].toString().toInt())
                if(it.jsonObject["startDate"]!!.jsonObject["day"].toString()!="null") startDate.set(Calendar.DAY_OF_MONTH,it.jsonObject["startDate"]!!.jsonObject["day"].toString().toInt())
                media.anime.startDate = startDate
                val endDate = Calendar.getInstance()
                if(it.jsonObject["endDate"]!!.jsonObject["year"].toString()!="null") endDate.set(Calendar.YEAR,it.jsonObject["endDate"]!!.jsonObject["year"].toString().toInt())
                if(it.jsonObject["endDate"]!!.jsonObject["month"].toString()!="null") endDate.set(Calendar.MONTH,it.jsonObject["endDate"]!!.jsonObject["month"].toString().toInt())
                if(it.jsonObject["endDate"]!!.jsonObject["day"].toString()!="null") endDate.set(Calendar.DAY_OF_MONTH,it.jsonObject["endDate"]!!.jsonObject["day"].toString().toInt())
                media.anime.endDate = endDate
                if (it.jsonObject["genres"]!!.jsonArray.isNotEmpty()){
                    media.anime.genres = arrayListOf()
                    it.jsonObject["genres"]!!.jsonArray.forEach { i ->
                        media.anime.genres!!.add(i.toString().trim('"'))
                    }
                }
                media.anime.mainStudioID = it.jsonObject["studios"]!!.jsonObject["nodes"]!!.jsonArray[0].jsonObject["id"].toString().toInt()
                media.anime.mainStudioName = it.jsonObject["studios"]!!.jsonObject["nodes"]!!.jsonArray[0].jsonObject["name"].toString().trim('"')

                media.anime.description = it.jsonObject["description"]!!.toString().trim('"')

                if(it.jsonObject["characters"]!!.jsonObject["edges"]!!.jsonArray.isNotEmpty()){
                    media.anime.characters = arrayListOf()
                    it.jsonObject["characters"]!!.jsonObject["edges"]!!.jsonArray.forEach { i->
                        media.anime.characters!!.add(Character(
                            id = i.jsonObject["node"]!!.jsonObject["id"]!!.toString().toInt(),
                            name = i.jsonObject["node"]!!.jsonObject["name"]!!.toString().trim('"'),
                            image = i.jsonObject["node"]!!.jsonObject["image"]!!.toString().trim('"'),
                            role = i.jsonObject["role"]!!.toString().trim('"')
                        ))
                    }
                }
                if(it.jsonObject["relations"]!!.jsonObject["edges"]!!.jsonArray.isNotEmpty()){
                    media.anime.relations = arrayListOf()
                    it.jsonObject["relations"]!!.jsonObject["edges"]!!.jsonArray.forEach { i->
                        if (i.jsonObject["node"]!!.jsonObject["id"].toString().trim('"')=="ANIME")
                        media.anime.relations!!.add(
                            Media(anime=Anime(
                                id = i.jsonObject["node"]!!.jsonObject["id"].toString().toInt(),
                                name = i.jsonObject["node"]!!.jsonObject["title"]!!.jsonObject["romaji"].toString().trim('"'),
                                userPreferredName = i.jsonObject["node"]!!.jsonObject["title"]!!.jsonObject["userPreferred"].toString().trim('"'),
                                cover = i.jsonObject["node"]!!.jsonObject["coverImage"]!!.jsonObject["large"].toString().trim('"'),
                                banner = i.jsonObject["node"]!!.jsonObject["bannerImage"].toString().trim('"'),
                                status = i.jsonObject["node"]!!.jsonObject["status"].toString().trim('"'),
                                meanScore = i.jsonObject["node"]!!.jsonObject["meanScore"].toString().toInt(),
                                relation = i.jsonObject["relationType"].toString().trim('"'),
                                totalEpisodes = if (i.jsonObject["node"]!!.jsonObject["episodes"].toString() != "null") i.jsonObject["node"]!!.jsonObject["episodes"].toString().toInt() else null,
                                nextAiringEpisode = if(i.jsonObject["node"]!!.jsonObject["nextAiringEpisode"].toString() != "null") i.jsonObject["node"]!!.jsonObject["nextAiringEpisode"]!!.jsonObject["episode"].toString().toInt()-1 else null,
                            ))
                        )
                        else media.anime.relations!!.add(
                            Media(manga= Manga(
                                id = i.jsonObject["node"]!!.jsonObject["id"].toString().toInt(),
                                name = i.jsonObject["node"]!!.jsonObject["title"]!!.jsonObject["romaji"].toString().trim('"'),
                                userPreferredName = i.jsonObject["node"]!!.jsonObject["title"]!!.jsonObject["userPreferred"].toString().trim('"'),
                                cover = i.jsonObject["node"]!!.jsonObject["coverImage"]!!.jsonObject["large"].toString().trim('"'),
                                banner = i.jsonObject["node"]!!.jsonObject["bannerImage"].toString().trim('"'),
                                status = i.jsonObject["node"]!!.jsonObject["status"].toString().trim('"'),
                                meanScore = i.jsonObject["node"]!!.jsonObject["meanScore"].toString().toInt(),
                                relation = i.jsonObject["relationType"].toString().trim('"'),
                                totalChapters = if (i.jsonObject["node"]!!.jsonObject["chapters"]!!.toString() != "null") i.jsonObject["node"]!!.jsonObject["chapters"].toString().toInt() else null,
                            )
                            )
                        )
                    }
                }
                if(it.jsonObject["recommendations"]!!.jsonObject["nodes"]!!.jsonArray.isNotEmpty()) {
                    media.anime.recommendations = arrayListOf()
                    it.jsonObject["recommendations"]!!.jsonObject["nodes"]!!.jsonArray.forEach { i ->
                        if (i.jsonObject["mediaRecommendation"]!!.jsonObject["type"].toString().trim('"') == "ANIME") {
                            media.anime.recommendations!!.add(
                                Media(
                                    anime = Anime(
                                        id = i.jsonObject["mediaRecommendation"]!!.jsonObject["id"].toString().toInt(),
                                        name = i.jsonObject["mediaRecommendation"]!!.jsonObject["title"]!!.jsonObject["romaji"].toString().trim('"'),
                                        userPreferredName = i.jsonObject["mediaRecommendation"]!!.jsonObject["title"]!!.jsonObject["userPreferred"].toString().trim('"'),
                                        status = i.jsonObject["mediaRecommendation"]!!.jsonObject["status"].toString().trim('"'),
                                        cover = i.jsonObject["mediaRecommendation"]!!.jsonObject["coverImage"]!!.jsonObject["large"].toString().trim('"'),
                                        banner = i.jsonObject["mediaRecommendation"]!!.jsonObject["bannerImage"].toString().trim('"'),
                                        meanScore = i.jsonObject["mediaRecommendation"]!!.jsonObject["meanScore"].toString().toInt(),

                                        totalEpisodes = if (i.jsonObject["mediaRecommendation"]!!.jsonObject["episodes"]!!.toString() != "null") i.jsonObject["mediaRecommendation"]!!.jsonObject["episodes"].toString().toInt() else null,
                                        nextAiringEpisode = if (i.jsonObject["mediaRecommendation"]!!.jsonObject["nextAiringEpisode"].toString() != "null") i.jsonObject["mediaRecommendation"]!!.jsonObject["nextAiringEpisode"]!!.jsonObject["episode"].toString().toInt() - 1 else null,
                                    )
                                )
                            )
                        } else {
                            media.anime.recommendations!!.add(
                                Media(
                                    manga = Manga(
                                        id = i.jsonObject["mediaRecommendation"]!!.jsonObject["id"].toString().toInt(),
                                        name = i.jsonObject["mediaRecommendation"]!!.jsonObject["title"]!!.jsonObject["romaji"].toString().trim('"'),
                                        userPreferredName = i.jsonObject["mediaRecommendation"]!!.jsonObject["title"]!!.jsonObject["userPreferred"].toString().trim('"'),
                                        status = i.jsonObject["mediaRecommendation"]!!.jsonObject["status"].toString().trim('"'),
                                        banner = i.jsonObject["mediaRecommendation"]!!.jsonObject["bannerImage"].toString().trim('"'),
                                        cover = i.jsonObject["mediaRecommendation"]!!.jsonObject["coverImage"]!!.jsonObject["large"].toString().trim('"'),
                                        meanScore = i.jsonObject["mediaRecommendation"]!!.jsonObject["meanScore"].toString().toInt(),

                                        totalChapters = if (i.jsonObject["mediaRecommendation"]!!.jsonObject["chapters"].toString() != "null") i.jsonObject["mediaRecommendation"]!!.jsonObject["chapters"].toString().toInt() else null,
                                    )
                                )
                            )
                        }
                    }
                }

                it.jsonObject["externalLinks"]!!.jsonArray.forEach{ i->
                    val url = i.jsonObject["url"].toString().trim('"')
                    if(url.startsWith("https://www.youtube.com") ){
                        media.anime.youtube = url
                    }
                }

                media.anime.nextAiringEpisodeTime = if (it.jsonObject["nextAiringEpisode"]!=null) Date(it.jsonObject["nextAiringEpisode"]!!.jsonObject["airingAt"].toString().toLong()) else null
                //TODO("Media List Entry")
            } else if (media.manga != null) {
                media.manga.source = it.jsonObject["source"]!!.toString().trim('"')
                media.manga.season = it.jsonObject["season"]!!.toString().trim('"')
                media.manga.seasonYear = it.jsonObject["seasonYear"]!!.toString().toInt()
                val startDate = Calendar.getInstance()
                if(it.jsonObject["startDate"]!!.jsonObject["year"].toString()!="null") startDate.set(Calendar.YEAR,it.jsonObject["startDate"]!!.jsonObject["year"].toString().toInt())
                if(it.jsonObject["startDate"]!!.jsonObject["month"].toString()!="null") startDate.set(Calendar.MONTH,it.jsonObject["startDate"]!!.jsonObject["month"].toString().toInt())
                if(it.jsonObject["startDate"]!!.jsonObject["day"].toString()!="null") startDate.set(Calendar.DAY_OF_MONTH,it.jsonObject["startDate"]!!.jsonObject["day"].toString().toInt())
                media.manga.startDate = startDate
                val endDate = Calendar.getInstance()
                if(it.jsonObject["endDate"]!!.jsonObject["year"].toString()!="null") endDate.set(Calendar.YEAR,it.jsonObject["endDate"]!!.jsonObject["year"].toString().toInt())
                if(it.jsonObject["endDate"]!!.jsonObject["month"].toString()!="null") endDate.set(Calendar.MONTH,it.jsonObject["endDate"]!!.jsonObject["month"].toString().toInt())
                if(it.jsonObject["endDate"]!!.jsonObject["day"].toString()!="null") endDate.set(Calendar.DAY_OF_MONTH,it.jsonObject["endDate"]!!.jsonObject["day"].toString().toInt())
                media.manga.endDate = endDate
                if (it.jsonObject["genres"]!!.jsonArray.isNotEmpty()){
                    media.manga.genres = arrayListOf()
                    it.jsonObject["genres"]!!.jsonArray.forEach { i ->
                        media.manga.genres!!.add(i.toString().trim('"'))
                    }
                }

                media.manga.description = it.jsonObject["description"]!!.toString().trim('"')

                if(it.jsonObject["characters"]!!.jsonObject["edges"]!!.jsonArray.isNotEmpty()){
                    media.manga.characters = arrayListOf()
                    it.jsonObject["characters"]!!.jsonObject["edges"]!!.jsonArray.forEach { i->
                        media.manga.characters!!.add(Character(
                            id = i.jsonObject["node"]!!.jsonObject["id"]!!.toString().toInt(),
                            name = i.jsonObject["node"]!!.jsonObject["name"]!!.toString().trim('"'),
                            image = i.jsonObject["node"]!!.jsonObject["image"]!!.toString().trim('"'),
                            role = i.jsonObject["role"]!!.toString().trim('"')
                        ))
                    }
                }
                if(it.jsonObject["relations"]!!.jsonObject["edges"]!!.jsonArray.isNotEmpty()){
                    media.manga.relations = arrayListOf()
                    it.jsonObject["relations"]!!.jsonObject["edges"]!!.jsonArray.forEach { i->
                        if (i.jsonObject["node"]!!.jsonObject["id"].toString().trim('"')=="ANIME")
                            media.manga.relations!!.add(
                                Media(anime=Anime(
                                    id = i.jsonObject["node"]!!.jsonObject["id"].toString().toInt(),
                                    name = i.jsonObject["node"]!!.jsonObject["title"]!!.jsonObject["romaji"].toString().trim('"'),
                                    userPreferredName = i.jsonObject["node"]!!.jsonObject["title"]!!.jsonObject["userPreferred"].toString().trim('"'),
                                    cover = i.jsonObject["node"]!!.jsonObject["coverImage"]!!.jsonObject["large"].toString().trim('"'),
                                    banner = i.jsonObject["node"]!!.jsonObject["bannerImage"].toString().trim('"'),
                                    status = i.jsonObject["node"]!!.jsonObject["status"].toString().trim('"'),
                                    meanScore = i.jsonObject["node"]!!.jsonObject["meanScore"].toString().toInt(),
                                    relation = i.jsonObject["relationType"].toString().trim('"'),
                                    totalEpisodes = if (i.jsonObject["node"]!!.jsonObject["episodes"].toString() != "null") i.jsonObject["node"]!!.jsonObject["episodes"].toString().toInt() else null,
                                    nextAiringEpisode = if(i.jsonObject["node"]!!.jsonObject["nextAiringEpisode"].toString() != "null") i.jsonObject["node"]!!.jsonObject["nextAiringEpisode"]!!.jsonObject["episode"].toString().toInt()-1 else null,
                                ))
                            )
                        else media.manga.relations!!.add(
                            Media(manga= Manga(
                                id = i.jsonObject["node"]!!.jsonObject["id"].toString().toInt(),
                                name = i.jsonObject["node"]!!.jsonObject["title"]!!.jsonObject["romaji"].toString().trim('"'),
                                userPreferredName = i.jsonObject["node"]!!.jsonObject["title"]!!.jsonObject["userPreferred"].toString().trim('"'),
                                cover = i.jsonObject["node"]!!.jsonObject["coverImage"]!!.jsonObject["large"].toString().trim('"'),
                                banner = i.jsonObject["node"]!!.jsonObject["bannerImage"].toString().trim('"'),
                                status = i.jsonObject["node"]!!.jsonObject["status"].toString().trim('"'),
                                meanScore = i.jsonObject["node"]!!.jsonObject["meanScore"].toString().toInt(),
                                relation = i.jsonObject["relationType"].toString().trim('"'),
                                totalChapters = if (it.jsonObject["node"]!!.jsonObject["chapters"]!!.toString() != "null") it.jsonObject["media"]!!.jsonObject["chapters"].toString().toInt() else null,
                            )
                            )
                        )
                    }
                }
                if(it.jsonObject["recommendations"]!!.jsonObject["nodes"]!!.jsonArray.isNotEmpty()) {
                    media.manga.recommendations = arrayListOf()
                    it.jsonObject["recommendations"]!!.jsonObject["nodes"]!!.jsonArray.forEach { i ->
                        if (i.jsonObject["mediaRecommendation"]!!.jsonObject["type"].toString().trim('"') == "ANIME") {
                            media.manga.recommendations!!.add(
                                Media(
                                    anime = Anime(
                                        id = i.jsonObject["mediaRecommendation"]!!.jsonObject["id"].toString().toInt(),
                                        name = i.jsonObject["mediaRecommendation"]!!.jsonObject["title"]!!.jsonObject["romaji"].toString().trim('"'),
                                        userPreferredName = i.jsonObject["mediaRecommendation"]!!.jsonObject["title"]!!.jsonObject["userPreferred"].toString().trim('"'),
                                        status = i.jsonObject["mediaRecommendation"]!!.jsonObject["status"].toString().trim('"'),
                                        cover = i.jsonObject["mediaRecommendation"]!!.jsonObject["coverImage"]!!.jsonObject["large"].toString().trim('"'),
                                        banner = i.jsonObject["mediaRecommendation"]!!.jsonObject["bannerImage"].toString().trim('"'),
                                        meanScore = i.jsonObject["mediaRecommendation"]!!.jsonObject["meanScore"].toString().toInt(),

                                        totalEpisodes = if (i.jsonObject["mediaRecommendation"]!!.jsonObject["episodes"]!!.toString() != "null") i.jsonObject["mediaRecommendation"]!!.jsonObject["episodes"].toString().toInt() else null,
                                        nextAiringEpisode = if (i.jsonObject["mediaRecommendation"]!!.jsonObject["nextAiringEpisode"].toString() != "null") i.jsonObject["mediaRecommendation"]!!.jsonObject["nextAiringEpisode"]!!.jsonObject["episode"].toString().toInt() - 1 else null,
                                    )
                                )
                            )
                        } else {
                            media.manga.recommendations!!.add(
                                Media(
                                    manga = Manga(
                                        id = i.jsonObject["mediaRecommendation"]!!.jsonObject["id"].toString().toInt(),
                                        name = i.jsonObject["mediaRecommendation"]!!.jsonObject["title"]!!.jsonObject["romaji"].toString().trim('"'),
                                        userPreferredName = i.jsonObject["mediaRecommendation"]!!.jsonObject["title"]!!.jsonObject["userPreferred"].toString().trim('"'),
                                        status = i.jsonObject["mediaRecommendation"]!!.jsonObject["status"].toString().trim('"'),
                                        banner = i.jsonObject["mediaRecommendation"]!!.jsonObject["bannerImage"].toString().trim('"'),
                                        cover = i.jsonObject["mediaRecommendation"]!!.jsonObject["coverImage"]!!.jsonObject["large"].toString().trim('"'),
                                        meanScore = i.jsonObject["mediaRecommendation"]!!.jsonObject["meanScore"].toString().toInt(),

                                        totalChapters = if (i.jsonObject["mediaRecommendation"]!!.jsonObject["chapters"].toString() != "null") i.jsonObject["mediaRecommendation"]!!.jsonObject["chapters"].toString().toInt() else null,
                                    )
                                )
                            )
                        }
                    }
                }
                //TODO("Media List Entry")
            }
        }
        return media
    }


    fun continueMedia(type:String): ArrayList<Media> {
        val response = getQuery(""" { MediaListCollection(userId: ${anilist.userid}, type: $type, status: CURRENT) { lists { entries { progress score(format:POINT_100) status media { id status chapters episodes nextAiringEpisode {episode} meanScore bannerImage coverImage{large} title { romaji userPreferred } } } } } } """)
        val returnArray = arrayListOf<Media>()
        val list = Json.decodeFromString<JsonObject>(response)["data"]!!.jsonObject["MediaListCollection"]!!.jsonObject["lists"]!!.jsonArray
        if (list.isNotEmpty()){
            list[0].jsonObject["entries"]!!.jsonArray.reversed().forEach {
                if (type == "ANIME"){
                    returnArray.add(
                        Media(anime=Anime(
                        id = it.jsonObject["media"]!!.jsonObject["id"].toString().toInt(),
                        name = it.jsonObject["media"]!!.jsonObject["title"]!!.jsonObject["romaji"].toString().trim('"'),
                        userPreferredName = it.jsonObject["media"]!!.jsonObject["title"]!!.jsonObject["userPreferred"].toString().trim('"'),
                        cover = it.jsonObject["media"]!!.jsonObject["coverImage"]!!.jsonObject["large"].toString().trim('"'),
                        banner = it.jsonObject["media"]!!.jsonObject["bannerImage"].toString().trim('"'),
                        status = it.jsonObject["media"]!!.jsonObject["status"].toString().trim('"'),
                        meanScore = it.jsonObject["media"]!!.jsonObject["meanScore"].toString().toInt(),

                        totalEpisodes = if (it.jsonObject["media"]!!.jsonObject["episodes"].toString() != "null") it.jsonObject["media"]!!.jsonObject["episodes"].toString().toInt() else null,
                        nextAiringEpisode = if(it.jsonObject["media"]!!.jsonObject["nextAiringEpisode"].toString() != "null") it.jsonObject["media"]!!.jsonObject["nextAiringEpisode"]!!.jsonObject["episode"].toString().toInt()-1 else null,

                        userProgress = it.jsonObject["progress"].toString().toInt(),
                        userScore = it.jsonObject["score"].toString().toInt(),
                        userStatus = it.jsonObject["status"].toString()
                    ))
                    )
                }
                else{
                    returnArray.add(
                        Media(manga= Manga(
                        id = it.jsonObject["media"]!!.jsonObject["id"].toString().toInt(),
                        name = it.jsonObject["media"]!!.jsonObject["title"]!!.jsonObject["romaji"].toString().trim('"'),
                        userPreferredName = it.jsonObject["media"]!!.jsonObject["title"]!!.jsonObject["userPreferred"].toString().trim('"'),
                        cover = it.jsonObject["media"]!!.jsonObject["coverImage"]!!.jsonObject["large"].toString().trim('"'),
                        banner = it.jsonObject["media"]!!.jsonObject["bannerImage"].toString().trim('"'),
                        status = it.jsonObject["media"]!!.jsonObject["status"].toString().trim('"'),
                        meanScore = it.jsonObject["media"]!!.jsonObject["meanScore"].toString().toInt(),

                        totalChapters = if (it.jsonObject["media"]!!.jsonObject["chapters"]!!.toString() != "null") it.jsonObject["media"]!!.jsonObject["chapters"].toString().toInt() else null,

                        userProgress = it.jsonObject["progress"].toString().toInt(),
                        userScore = it.jsonObject["score"].toString().toInt(),
                        userStatus = it.jsonObject["status"].toString()
                    ))
                    )
                }
            }
        }
        return returnArray
    }

    fun recommendations(): ArrayList<Media>? {
        try{
            val response = getQuery(""" { Page(page: 1, perPage:30) { pageInfo { total currentPage hasNextPage } recommendations(sort: RATING_DESC, onList: true) { rating userRating mediaRecommendation { id chapters episodes nextAiringEpisode {episode} meanScore title { romaji userPreferred } type status(version: 2) bannerImage coverImage { large } } } } } """)
            val responseArray = arrayListOf<Media>()
            Json.decodeFromString<JsonObject>(response)["data"]!!.jsonObject["Page"]!!.jsonObject["recommendations"]!!.jsonArray.reversed().forEach{
                if(it.jsonObject["mediaRecommendation"]!!.jsonObject["type"].toString().trim('"') == "ANIME"){
                    responseArray.add(
                        Media(anime = Anime(
                        id = it.jsonObject["mediaRecommendation"]!!.jsonObject["id"].toString().toInt(),
                        name = it.jsonObject["mediaRecommendation"]!!.jsonObject["title"]!!.jsonObject["romaji"].toString().trim('"'),
                        userPreferredName = it.jsonObject["mediaRecommendation"]!!.jsonObject["title"]!!.jsonObject["userPreferred"].toString().trim('"'),
                        status = it.jsonObject["mediaRecommendation"]!!.jsonObject["status"].toString().trim('"'),
                        cover = it.jsonObject["mediaRecommendation"]!!.jsonObject["coverImage"]!!.jsonObject["large"].toString().trim('"'),
                        banner = it.jsonObject["mediaRecommendation"]!!.jsonObject["bannerImage"].toString().trim('"'),
                        meanScore = it.jsonObject["mediaRecommendation"]!!.jsonObject["meanScore"].toString().toInt(),
                        userScore = 0,

                        totalEpisodes = if(it.jsonObject["mediaRecommendation"]!!.jsonObject["episodes"]!!.toString() != "null") it.jsonObject["mediaRecommendation"]!!.jsonObject["episodes"].toString().toInt() else null,
                        nextAiringEpisode = if (it.jsonObject["mediaRecommendation"]!!.jsonObject["nextAiringEpisode"].toString() != "null") it.jsonObject["mediaRecommendation"]!!.jsonObject["nextAiringEpisode"]!!.jsonObject["episode"].toString().toInt()-1 else null,
                        )
                    )
                    )
                }
                else {
                    responseArray.add(
                        Media(manga = Manga(
                        id = it.jsonObject["mediaRecommendation"]!!.jsonObject["id"].toString().toInt(),
                        name = it.jsonObject["mediaRecommendation"]!!.jsonObject["title"]!!.jsonObject["romaji"].toString().trim('"'),
                        userPreferredName = it.jsonObject["mediaRecommendation"]!!.jsonObject["title"]!!.jsonObject["userPreferred"].toString().trim('"'),
                        status = it.jsonObject["mediaRecommendation"]!!.jsonObject["status"].toString().trim('"'),
                        banner = it.jsonObject["mediaRecommendation"]!!.jsonObject["bannerImage"].toString().trim('"'),
                        cover = it.jsonObject["mediaRecommendation"]!!.jsonObject["coverImage"]!!.jsonObject["large"].toString().trim('"'),
                        meanScore = it.jsonObject["mediaRecommendation"]!!.jsonObject["meanScore"].toString().toInt(),
                        userScore = 0,

                        totalChapters = if (it.jsonObject["mediaRecommendation"]!!.jsonObject["chapters"].toString() != "null") it.jsonObject["mediaRecommendation"]!!.jsonObject["chapters"].toString().toInt() else null,
                        )
                    )
                    )
                }
            }
            return responseArray
        }
        catch (e: Exception){
            return null
        }
    }

    private fun bannerImage(type: String): String? {
        val response = getQuery("""{ MediaListCollection(userId: ${anilist.userid}, type: $type, sort:[SCORE_DESC,UPDATED_TIME_DESC],chunk:1,perChunk:1) { lists { entries{ media { bannerImage } } } } } """)
        val list = Json.decodeFromString<JsonObject>(response)["data"]!!.jsonObject["MediaListCollection"]!!.jsonObject["lists"]!!.jsonArray
        if (list.isNotEmpty()){
            return list[0].jsonObject["entries"]!!.jsonArray[0].jsonObject["media"]!!.jsonObject["bannerImage"].toString().trim('"')
        }
        return null
    }

    fun getBannerImages(): ArrayList<String?> {
        val default = arrayListOf<String?>(null,null)
        default[0]=bannerImage("ANIME")
        default[1]=bannerImage("MANGA")
        return default
    }

    fun mangaList(): MutableList<Manga> {
        val response = getQuery("""{ MediaListCollection(userId: ${anilist.userid}, type: MANGA) { lists { name entries { progress score(format:POINT_100) media { id status chapters episodes nextAiringEpisode {episode} bannerImage meanScore coverImage{large} title { romaji userPreferred } } } } } }""")
        val returnArray = mutableListOf<Manga>()
        Json.decodeFromString<JsonObject>(response)["data"]!!.jsonObject["MediaListCollection"]!!.jsonObject["lists"]!!.jsonArray.forEach { i ->
            i.jsonObject["entries"]!!.jsonArray.forEach {
                returnArray.add(
                    Manga(
                        id = it.jsonObject["media"]!!.jsonObject["id"].toString().toInt(),
                        name = it.jsonObject["media"]!!.jsonObject["title"]!!.jsonObject["romaji"].toString(),
                        userPreferredName = it.jsonObject["media"]!!.jsonObject["title"]!!.jsonObject["userPreferred"].toString(),
                        cover = it.jsonObject["media"]!!.jsonObject["coverImage"]!!.jsonObject["large"].toString(),
                        banner = it.jsonObject["media"]!!.jsonObject["bannerImage"].toString().trim('"'),
                        status = it.jsonObject["media"]!!.jsonObject["status"].toString().trim('"'),
                        meanScore = if(it.jsonObject["media"]!!.jsonObject["meanScore"].toString() != "null") it.jsonObject["media"]!!.jsonObject["meanScore"].toString().toInt() else null,

                        totalChapters = if (it.jsonObject["media"]!!.jsonObject["chapters"].toString() != "null") it.jsonObject["media"]!!.jsonObject["chapters"].toString().toInt() else null,

                        userScore = it.jsonObject["score"].toString().toInt(),
                        userProgress = it.jsonObject["progress"].toString().toInt(),
                    )
                )
            }
        }
        return returnArray
    }

    fun animeList(): ArrayList<Anime> {
        val response = getQuery("""{ MediaListCollection(userId: ${anilist.userid}, type: ANIME) { lists { name entries { status progress score(format:POINT_100) media { id status chapters episodes nextAiringEpisode {episode} meanScore bannerImage coverImage{large} title { romaji userPreferred } } } } } }""")
        val returnArray = arrayListOf<Anime>()
        println(response)
        Json.decodeFromString<JsonObject>(response)["data"]!!.jsonObject["MediaListCollection"]!!.jsonObject["lists"]!!.jsonArray.forEach { i ->
            i.jsonObject["entries"]!!.jsonArray.forEach {
                returnArray.add(
                    Anime(
                        id = it.jsonObject["media"]!!.jsonObject["id"].toString().toInt(),
                        name = it.jsonObject["media"]!!.jsonObject["title"]!!.jsonObject["romaji"].toString(),
                        userPreferredName = it.jsonObject["media"]!!.jsonObject["title"]!!.jsonObject["userPreferred"].toString(),
                        cover = it.jsonObject["media"]!!.jsonObject["coverImage"]!!.jsonObject["large"].toString(),
                        status = it.jsonObject["media"]!!.jsonObject["status"].toString().trim('"'),
                        banner = it.jsonObject["media"]!!.jsonObject["bannerImage"].toString().trim('"'),
                        meanScore = if(it.jsonObject["media"]!!.jsonObject["meanScore"].toString() != "null") it.jsonObject["media"]!!.jsonObject["meanScore"].toString().toInt() else null,

                        totalEpisodes = if(it.jsonObject["media"]!!.jsonObject["episodes"]!!.toString() != "null") it.jsonObject["media"]!!.jsonObject["episodes"].toString().toInt() else null,
                        nextAiringEpisode = if (it.jsonObject["media"]!!.jsonObject["nextAiringEpisode"].toString() != "null") it.jsonObject["media"]!!.jsonObject["nextAiringEpisode"]!!.jsonObject["episode"].toString().toInt()-1 else null,

                        userProgress = it.jsonObject["progress"].toString().toInt(),
                        userStatus = it.jsonObject["status"].toString(),
                        userScore = it.jsonObject["score"].toString().toInt()
                    )
                )
            }
        }
        return returnArray
    }
}