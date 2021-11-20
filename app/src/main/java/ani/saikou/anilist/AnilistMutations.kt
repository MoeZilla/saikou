package ani.saikou.anilist

class AnilistMutations {

    fun toggleFav(anime:Boolean=true,id:Int){
        val query = """mutation (${"$"}animeId: Int,${"$"}mangaId:Int) { ToggleFavourite(animeId:${"$"}animeId,mangaId:${"$"}mangaId){ anime { edges { id } } manga { edges { id } } } }"""
        val variables = if(anime) """{\"animeId\":\"$id\"}""" else """{\"mangaId\":\"$id\"}"""
        executeQuery(query,variables)
    }

    fun editList(
        mediaID:Int,
        progress:Int?,
        score: Int?,
        status: String?,
        startedAt:Long?,
        completedAt:Long?
    ){
        val query = """
            mutation ( ${"$"}mediaID: Int, ${"$"}progress: Int, ${"$"}scoreRaw:Int, ${"$"}status:MediaListStatus, ${"$"}start:FuzzyDateInput, ${"$"}completed:FuzzyDateInput ) {
                SaveMediaListEntry( mediaId: ${"$"}mediaID, progress: ${"$"}progress, scoreRaw: ${"$"}scoreRaw, status:${"$"}status, startedAt: ${"$"}start, completedAt: ${"$"}completed ) {
                    score(format:POINT_10_DECIMAL)
                }
            }
        """.replace("\n","").replace("""    ""","")
        val variables = """{\"mediaID\":\"$mediaID\"
            ${if (progress!=null) """,\"progress\":\"$progress\"""" else ""}
            ${if (score!=null) """,\"scoreRaw\":\"$score\"""" else ""}
            ${if (status!=null) """,\"status\":\"${status}\"""" else ""}
            ${if (startedAt!=null) """,\"startedAt\":\"$startedAt\"""" else ""}
            ${if (completedAt!=null) """,\"completedAt\":\"$completedAt\"""" else ""}
            }""".replace("\n","").replace("""    ""","")
        executeQuery(query,variables)
    }
}