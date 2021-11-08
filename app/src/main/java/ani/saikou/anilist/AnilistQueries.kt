package ani.saikou.anilist

import ani.saikou.FuzzyDate
import ani.saikou.anime.Anime
import ani.saikou.logger
import ani.saikou.manga.Manga
import ani.saikou.media.Character
import ani.saikou.media.Media
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

class AnilistQueries{

    private fun getQuery(query:String): String = runBlocking{
        withContext(Dispatchers.Default){
            val json = Jsoup.connect("https://graphql.anilist.co/")
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            .header("Authorization", "Bearer ${anilist.token}")
                            .requestBody("""{"query":"$query"}""")
                            .ignoreContentType(true).ignoreHttpErrors(true)
                            .post().body().text()
            logger("JSON : $json",false)
            return@withContext json
        }
    }

    fun getUserData():Boolean{
        return try{
            val response = Json.decodeFromString<JsonObject>(getQuery("""{Viewer {name avatar{medium}id statistics{anime{episodesWatched}manga{chaptersRead}}}}"""))["data"]!!.jsonObject["Viewer"]!!

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

    fun mediaDetails(media:Media):Media?{
        val response = getQuery("""{Media(id:${media.id}){mediaListEntry{id status score progress repeat updatedAt startedAt{year month day}completedAt{year month day}}isFavourite nextAiringEpisode{episode airingAt}source duration season seasonYear startDate{year month day}endDate{year month day}genres studios(isMain:true){nodes{id name siteUrl}}description characters(sort:FAVOURITES_DESC,perPage:25,page:1){edges{role node{id image{medium}name{userPreferred}}}}relations{edges{relationType(version:2)node{id chapters episodes episodes chapters nextAiringEpisode{episode}meanScore isFavourite title{english romaji userPreferred}type status(version:2)bannerImage coverImage{large}}}}recommendations{nodes{mediaRecommendation{id chapters episodes chapters nextAiringEpisode{episode}meanScore isFavourite title{english romaji userPreferred}type status(version:2)bannerImage coverImage{large}}}}streamingEpisodes{title thumbnail}externalLinks{url}}}""")
        println(response)
        val json = Json.decodeFromString<JsonObject>(response)["data"]!!
        try {
            val it = json.jsonObject["Media"]!!
            media.source = it.jsonObject["source"]!!.toString().trim('"')
            media.startDate = FuzzyDate(
                if(it.jsonObject["endDate"]!!.jsonObject["year"].toString()!="null") it.jsonObject["endDate"]!!.jsonObject["year"].toString().toInt() else null,
                if(it.jsonObject["endDate"]!!.jsonObject["month"].toString()!="null") it.jsonObject["endDate"]!!.jsonObject["month"].toString().toInt() else null,
                if(it.jsonObject["endDate"]!!.jsonObject["day"].toString()!="null") it.jsonObject["endDate"]!!.jsonObject["day"].toString().toInt() else null
            )
            media.endDate = FuzzyDate(
                if(it.jsonObject["endDate"]!!.jsonObject["year"].toString()!="null") it.jsonObject["endDate"]!!.jsonObject["year"].toString().toInt() else null,
                if(it.jsonObject["endDate"]!!.jsonObject["month"].toString()!="null") it.jsonObject["endDate"]!!.jsonObject["month"].toString().toInt() else null,
                if(it.jsonObject["endDate"]!!.jsonObject["day"].toString()!="null") it.jsonObject["endDate"]!!.jsonObject["day"].toString().toInt() else null
            )
            if (it.jsonObject["genres"]!!.jsonArray.isNotEmpty()){
                media.genres = arrayListOf()
                it.jsonObject["genres"]!!.jsonArray.forEach { i ->
                    media.genres!!.add(i.toString().trim('"'))
                }
            }
            media.description = it.jsonObject["description"]!!.toString().trim('"')

            if(it.jsonObject["characters"]!!.jsonObject["edges"]!!.jsonArray.isNotEmpty()){
                media.characters = arrayListOf()
                it.jsonObject["characters"]!!.jsonObject["edges"]!!.jsonArray.forEach { i->
                    media.characters!!.add(Character(
                        id = i.jsonObject["node"]!!.jsonObject["id"]!!.toString().toInt(),
                        name = i.jsonObject["node"]!!.jsonObject["name"]!!.toString().trim('"'),
                        image = i.jsonObject["node"]!!.jsonObject["image"]!!.toString().trim('"'),
                        role = i.jsonObject["role"]!!.toString().trim('"')
                    ))
                }
            }
            if(it.jsonObject["relations"]!!.jsonObject["edges"]!!.jsonArray.isNotEmpty()){
                media.relations = arrayListOf()
                it.jsonObject["relations"]!!.jsonObject["edges"]!!.jsonArray.forEach { i->
                    media.relations!!.add(
                        Media(
                            id = i.jsonObject["node"]!!.jsonObject["id"].toString().toInt(),
                            name = i.jsonObject["node"]!!.jsonObject["title"]!!.jsonObject["english"].toString().trim('"'),
                            nameRomaji = i.jsonObject["node"]!!.jsonObject["title"]!!.jsonObject["romaji"].toString().trim('"'),
                            userPreferredName = i.jsonObject["node"]!!.jsonObject["title"]!!.jsonObject["userPreferred"].toString().trim('"'),
                            cover = i.jsonObject["node"]!!.jsonObject["coverImage"]!!.jsonObject["large"].toString().trim('"'),
                            banner = i.jsonObject["node"]!!.jsonObject["bannerImage"].toString().trim('"'),
                            status = i.jsonObject["node"]!!.jsonObject["status"].toString().trim('"'),
                            isFav = i.jsonObject["node"]!!.jsonObject["isFavourite"].toString()=="true",
                            meanScore = if (i.jsonObject["node"]!!.jsonObject["meanScore"].toString().trim('"')!="null") i.jsonObject["node"]!!.jsonObject["meanScore"].toString().toInt() else null,
                            relation = i.jsonObject["relationType"].toString().trim('"'),
                            anime = if (i.jsonObject["node"]!!.jsonObject["id"].toString().trim('"')=="ANIME") Anime(totalEpisodes = if (i.jsonObject["node"]!!.jsonObject["episodes"].toString() != "null") i.jsonObject["node"]!!.jsonObject["episodes"].toString().toInt() else null, nextAiringEpisode = if(i.jsonObject["node"]!!.jsonObject["nextAiringEpisode"].toString() != "null") i.jsonObject["node"]!!.jsonObject["nextAiringEpisode"]!!.jsonObject["episode"].toString().toInt()-1 else null) else null,
                            manga = if (i.jsonObject["node"]!!.jsonObject["id"].toString().trim('"')=="MANGA") Manga(totalChapters = if (i.jsonObject["node"]!!.jsonObject["chapters"]!!.toString() != "null") i.jsonObject["node"]!!.jsonObject["chapters"].toString().toInt() else null) else null,
                        )
                    )
                }
            }
            if(it.jsonObject["recommendations"]!!.jsonObject["nodes"]!!.jsonArray.isNotEmpty()) {
                media.recommendations = arrayListOf()
                it.jsonObject["recommendations"]!!.jsonObject["nodes"]!!.jsonArray.forEach { i ->
                    media.recommendations!!.add(
                        Media(
                            id = i.jsonObject["mediaRecommendation"]!!.jsonObject["id"].toString().toInt(),
                            name = i.jsonObject["mediaRecommendation"]!!.jsonObject["title"]!!.jsonObject["english"].toString().trim('"'),
                            nameRomaji = i.jsonObject["mediaRecommendation"]!!.jsonObject["title"]!!.jsonObject["romaji"].toString().trim('"'),
                            userPreferredName = i.jsonObject["mediaRecommendation"]!!.jsonObject["title"]!!.jsonObject["userPreferred"].toString().trim('"'),
                            status = i.jsonObject["mediaRecommendation"]!!.jsonObject["status"].toString().trim('"'),
                            cover = i.jsonObject["mediaRecommendation"]!!.jsonObject["coverImage"]!!.jsonObject["large"].toString().trim('"'),
                            banner = i.jsonObject["mediaRecommendation"]!!.jsonObject["bannerImage"].toString().trim('"'),
                            meanScore = if (i.jsonObject["mediaRecommendation"]!!.jsonObject["meanScore"].toString().trim('"')!="null") i.jsonObject["mediaRecommendation"]!!.jsonObject["meanScore"].toString().toInt() else null,
                            isFav = i.jsonObject["mediaRecommendation"]!!.jsonObject["isFavourite"].toString()!="true",
                            anime = if (i.jsonObject["mediaRecommendation"]!!.jsonObject["type"].toString().trim('"') == "ANIME") Anime(totalEpisodes = if (i.jsonObject["mediaRecommendation"]!!.jsonObject["episodes"]!!.toString() != "null") i.jsonObject["mediaRecommendation"]!!.jsonObject["episodes"].toString().toInt() else null, nextAiringEpisode = if (i.jsonObject["mediaRecommendation"]!!.jsonObject["nextAiringEpisode"].toString() != "null") i.jsonObject["mediaRecommendation"]!!.jsonObject["nextAiringEpisode"]!!.jsonObject["episode"].toString().toInt() - 1 else null) else null,
                            manga = if (i.jsonObject["mediaRecommendation"]!!.jsonObject["type"].toString().trim('"') == "MANGA") Manga(totalChapters = if (i.jsonObject["mediaRecommendation"]!!.jsonObject["chapters"].toString() != "null") i.jsonObject["mediaRecommendation"]!!.jsonObject["chapters"].toString().toInt() else null) else null
                        )
                    )
                }
            }

            if (it.jsonObject["mediaListEntry"].toString()!="null") {
                media.userRepeat =
                    if (it.jsonObject["mediaListEntry"]!!.jsonObject["repeat"].toString() == "null") it.jsonObject["mediaListEntry"]!!.jsonObject["repeat"]!!.toString().toInt() else 0
                media.userUpdatedAt = Date(it.jsonObject["mediaListEntry"]!!.jsonObject["repeat"]!!.toString().toLong()*1000)
                media.userCompletedAt = FuzzyDate(
                    if (it.jsonObject["mediaListEntry"]!!.jsonObject["completedAt"]!!.jsonObject["year"].toString() != "null") it.jsonObject["mediaListEntry"]!!.jsonObject["completedAt"]!!.jsonObject["year"].toString().toInt() else null,
                    if (it.jsonObject["mediaListEntry"]!!.jsonObject["completedAt"]!!.jsonObject["month"].toString() != "null") it.jsonObject["mediaListEntry"]!!.jsonObject["completedAt"]!!.jsonObject["month"].toString().toInt() else null,
                    if (it.jsonObject["mediaListEntry"]!!.jsonObject["completedAt"]!!.jsonObject["day"].toString() != "null") it.jsonObject["mediaListEntry"]!!.jsonObject["completedAt"]!!.jsonObject["day"].toString().toInt() else null
                )
                media.userStartedAt = FuzzyDate(
                    if (it.jsonObject["mediaListEntry"]!!.jsonObject["startedAt"]!!.jsonObject["year"].toString() != "null") it.jsonObject["mediaListEntry"]!!.jsonObject["startedAt"]!!.jsonObject["year"].toString().toInt() else null,
                    if (it.jsonObject["mediaListEntry"]!!.jsonObject["startedAt"]!!.jsonObject["month"].toString() != "null") it.jsonObject["mediaListEntry"]!!.jsonObject["startedAt"]!!.jsonObject["month"].toString().toInt() else null,
                    if (it.jsonObject["mediaListEntry"]!!.jsonObject["startedAt"]!!.jsonObject["day"].toString() != "null") it.jsonObject["mediaListEntry"]!!.jsonObject["startedAt"]!!.jsonObject["day"].toString().toInt() else null
                )
            }

            if (media.anime != null) {
                media.anime.episodeDuration = it.jsonObject["duration"]!!.toString().toInt()
                media.anime.season = it.jsonObject["season"]!!.toString().trim('"')
                media.anime.seasonYear = it.jsonObject["seasonYear"]!!.toString().toInt()

                media.anime.mainStudioID = it.jsonObject["studios"]!!.jsonObject["nodes"]!!.jsonArray[0].jsonObject["id"].toString().toInt()
                media.anime.mainStudioName = it.jsonObject["studios"]!!.jsonObject["nodes"]!!.jsonArray[0].jsonObject["name"].toString().trim('"')
                media.anime.nextAiringEpisodeTime = if (it.jsonObject["nextAiringEpisode"].toString().trim('"')!="null") Date(it.jsonObject["nextAiringEpisode"]!!.jsonObject["airingAt"].toString().toLong()*1000) else null

                it.jsonObject["externalLinks"]!!.jsonArray.forEach{ i->
                    val url = i.jsonObject["url"].toString().trim('"')
                    if(url.startsWith("https://www.youtube.com") ){
                        media.anime.youtube = url
                    }
                }
            }
            else if (media.manga != null) {
                logger ("Nothing Here lmao",false)
            }
            return media
        }
        catch (e:Exception){
            logger ("Error : $e")
        }
        return null
    }


    fun continueMedia(type:String): ArrayList<Media> {
        val response = getQuery(""" { MediaListCollection(userId: ${anilist.userid}, type: $type, status: CURRENT) { lists { entries { progress score(format:POINT_100) status media { id status chapters episodes nextAiringEpisode {episode} meanScore isFavourite bannerImage coverImage{large} title { english romaji userPreferred } } } } } } """)
        val returnArray = arrayListOf<Media>()
        val list = Json.decodeFromString<JsonObject>(response)["data"]!!.jsonObject["MediaListCollection"]!!.jsonObject["lists"]!!.jsonArray
        if (list.isNotEmpty()){
            list[0].jsonObject["entries"]!!.jsonArray.reversed().forEach {
                returnArray.add(
                    Media(
                        id = it.jsonObject["media"]!!.jsonObject["id"].toString().toInt(),
                        name = it.jsonObject["media"]!!.jsonObject["title"]!!.jsonObject["english"].toString().trim('"'),
                        nameRomaji = it.jsonObject["media"]!!.jsonObject["title"]!!.jsonObject["romaji"].toString().trim('"'),
                        userPreferredName = it.jsonObject["media"]!!.jsonObject["title"]!!.jsonObject["userPreferred"].toString().trim('"'),
                        cover = it.jsonObject["media"]!!.jsonObject["coverImage"]!!.jsonObject["large"].toString().trim('"'),
                        banner = it.jsonObject["media"]!!.jsonObject["bannerImage"].toString().trim('"'),
                        status = it.jsonObject["media"]!!.jsonObject["status"].toString().trim('"'),
                        meanScore = it.jsonObject["media"]!!.jsonObject["meanScore"].toString().toInt(),
                        isFav = it.jsonObject["media"]!!.jsonObject["isFavourite"].toString() == "true",
                        userProgress = it.jsonObject["progress"].toString().toInt(),
                        userScore = it.jsonObject["score"].toString().toInt(),
                        userStatus = it.jsonObject["status"].toString().trim('"'),
                        anime = if (type == "ANIME") Anime(totalEpisodes = if (it.jsonObject["media"]!!.jsonObject["episodes"].toString() != "null") it.jsonObject["media"]!!.jsonObject["episodes"].toString().toInt() else null, nextAiringEpisode = if(it.jsonObject["media"]!!.jsonObject["nextAiringEpisode"].toString() != "null") it.jsonObject["media"]!!.jsonObject["nextAiringEpisode"]!!.jsonObject["episode"].toString().toInt()-1 else null) else null,
                        manga = if (type == "MANGA") Manga(totalChapters = if (it.jsonObject["media"]!!.jsonObject["chapters"]!!.toString() != "null") it.jsonObject["media"]!!.jsonObject["chapters"].toString().toInt() else null) else null,
                    )
                )
            }
        }
        return returnArray
    }

    fun recommendations(): ArrayList<Media> {
        val response = getQuery(""" { Page(page: 1, perPage:30) { pageInfo { total currentPage hasNextPage } recommendations(sort: RATING_DESC, onList: true) { rating userRating mediaRecommendation { id chapters isFavourite episodes nextAiringEpisode {episode} meanScore isFavourite title {english romaji userPreferred } type status(version: 2) bannerImage coverImage { large } } } } } """)
        val responseArray = arrayListOf<Media>()
        val ids = arrayListOf<Int>()
        Json.decodeFromString<JsonObject>(response)["data"]!!.jsonObject["Page"]!!.jsonObject["recommendations"]!!.jsonArray.reversed().forEach{
            val id =  it.jsonObject["mediaRecommendation"]!!.jsonObject["id"].toString().toInt()
            if (id !in ids) {
                ids.add(id)
                responseArray.add(
                    Media(
                        id = id,
                        name = it.jsonObject["mediaRecommendation"]!!.jsonObject["title"]!!.jsonObject["english"].toString().trim('"'),
                        nameRomaji = it.jsonObject["mediaRecommendation"]!!.jsonObject["title"]!!.jsonObject["romaji"].toString().trim('"'),
                        userPreferredName = it.jsonObject["mediaRecommendation"]!!.jsonObject["title"]!!.jsonObject["userPreferred"].toString().trim('"'),
                        status = it.jsonObject["mediaRecommendation"]!!.jsonObject["status"].toString().trim('"'),
                        cover = it.jsonObject["mediaRecommendation"]!!.jsonObject["coverImage"]!!.jsonObject["large"].toString().trim('"'),
                        banner = it.jsonObject["mediaRecommendation"]!!.jsonObject["bannerImage"].toString().trim('"'),
                        meanScore = it.jsonObject["mediaRecommendation"]!!.jsonObject["meanScore"].toString().toInt(),
                        isFav = it.jsonObject["mediaRecommendation"]!!.jsonObject["isFavourite"].toString()=="true",
                        userScore = 0,
                        anime = if(it.jsonObject["mediaRecommendation"]!!.jsonObject["type"].toString().trim('"') == "ANIME") Anime(totalEpisodes = if (it.jsonObject["mediaRecommendation"]!!.jsonObject["episodes"]!!.toString() != "null") it.jsonObject["mediaRecommendation"]!!.jsonObject["episodes"].toString().toInt() else null, nextAiringEpisode = if (it.jsonObject["mediaRecommendation"]!!.jsonObject["nextAiringEpisode"].toString() != "null") it.jsonObject["mediaRecommendation"]!!.jsonObject["nextAiringEpisode"]!!.jsonObject["episode"].toString().toInt() - 1 else null) else null,
                        manga = if(it.jsonObject["mediaRecommendation"]!!.jsonObject["type"].toString().trim('"') == "MANGA") Manga(totalChapters = if (it.jsonObject["mediaRecommendation"]!!.jsonObject["chapters"].toString() != "null") it.jsonObject["mediaRecommendation"]!!.jsonObject["chapters"].toString().toInt() else null) else null,
                    )
                )
            }
        }
        return responseArray
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

    fun mangaList(): MutableList<Media> {
        val response = getQuery("""{ MediaListCollection(userId: ${anilist.userid}, type: MANGA) { lists { name entries { progress score(format:POINT_100) media { id status chapters episodes nextAiringEpisode {episode} bannerImage meanScore isFavourite coverImage{large} title {english romaji userPreferred } } } } } }""")
        val returnArray = mutableListOf<Media>()
        Json.decodeFromString<JsonObject>(response)["data"]!!.jsonObject["MediaListCollection"]!!.jsonObject["lists"]!!.jsonArray.forEach { i ->
            i.jsonObject["entries"]!!.jsonArray.forEach {
                returnArray.add(Media(
                    id = it.jsonObject["media"]!!.jsonObject["id"].toString().toInt(),
                    name = it.jsonObject["media"]!!.jsonObject["title"]!!.jsonObject["english"].toString(),
                    nameRomaji = it.jsonObject["media"]!!.jsonObject["title"]!!.jsonObject["romaji"].toString(),
                    userPreferredName = it.jsonObject["media"]!!.jsonObject["title"]!!.jsonObject["userPreferred"].toString(),
                    cover = it.jsonObject["media"]!!.jsonObject["coverImage"]!!.jsonObject["large"].toString(),
                    banner = it.jsonObject["media"]!!.jsonObject["bannerImage"].toString().trim('"'),
                    status = it.jsonObject["media"]!!.jsonObject["status"].toString().trim('"'),
                    meanScore = if(it.jsonObject["media"]!!.jsonObject["meanScore"].toString() != "null") it.jsonObject["media"]!!.jsonObject["meanScore"].toString().toInt() else null,
                    isFav = it.jsonObject["media"]!!.jsonObject["isFavourite"].toString()=="true",
                    userScore = it.jsonObject["score"].toString().toInt(),
                    userProgress = it.jsonObject["progress"].toString().toInt(),
                    manga = Manga(totalChapters = if (it.jsonObject["media"]!!.jsonObject["chapters"].toString() != "null") it.jsonObject["media"]!!.jsonObject["chapters"].toString().toInt() else null)
                ))
            }
        }
        return returnArray
    }

    fun animeList(): ArrayList<Media> {
        val response = getQuery("""{ MediaListCollection(userId: ${anilist.userid}, type: ANIME) { lists { name entries { status progress score(format:POINT_100) media { id status chapters episodes nextAiringEpisode {episode} meanScore bannerImage coverImage{large} title {english romaji userPreferred } } } } } }""")
        val returnArray = arrayListOf<Media>()
        println(response)
        Json.decodeFromString<JsonObject>(response)["data"]!!.jsonObject["MediaListCollection"]!!.jsonObject["lists"]!!.jsonArray.forEach { i ->
            i.jsonObject["entries"]!!.jsonArray.forEach {
                returnArray.add(
                    Media(
                        id = it.jsonObject["media"]!!.jsonObject["id"].toString().toInt(),
                        name = it.jsonObject["media"]!!.jsonObject["title"]!!.jsonObject["english"].toString(),
                        nameRomaji = it.jsonObject["media"]!!.jsonObject["title"]!!.jsonObject["romaji"].toString(),
                        userPreferredName = it.jsonObject["media"]!!.jsonObject["title"]!!.jsonObject["userPreferred"].toString(),
                        cover = it.jsonObject["media"]!!.jsonObject["coverImage"]!!.jsonObject["large"].toString(),
                        status = it.jsonObject["media"]!!.jsonObject["status"].toString().trim('"'),
                        banner = it.jsonObject["media"]!!.jsonObject["bannerImage"].toString().trim('"'),
                        meanScore = if(it.jsonObject["media"]!!.jsonObject["meanScore"].toString() != "null") it.jsonObject["media"]!!.jsonObject["meanScore"].toString().toInt() else null,
                        isFav = it.jsonObject["media"]!!.jsonObject["isFavourite"].toString()=="true",
                        userProgress = it.jsonObject["progress"].toString().toInt(),
                        userStatus = it.jsonObject["status"].toString(),
                        userScore = it.jsonObject["score"].toString().toInt(),
                        anime = Anime(totalEpisodes = if(it.jsonObject["media"]!!.jsonObject["episodes"]!!.toString() != "null") it.jsonObject["media"]!!.jsonObject["episodes"].toString().toInt() else null, nextAiringEpisode = if (it.jsonObject["media"]!!.jsonObject["nextAiringEpisode"].toString() != "null") it.jsonObject["media"]!!.jsonObject["nextAiringEpisode"]!!.jsonObject["episode"].toString().toInt()-1 else null),
                    )
                )
            }
        }
        return returnArray
    }
}
