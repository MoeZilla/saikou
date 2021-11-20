package ani.saikou.anilist

class AnilistMutations {

    fun toggleFav(id:Int){
        val query = """mutation (${"$"}id: Int) { ToggleFavourite(animeId:${"$"}id){ anime { edges { id } } } }"""
        val variables = """{\"id\":\"$id\"}"""
        executeQuery(query,variables)
    }

    fun editList(
        mediaID:Int,
        progress:Int,
        score:String,
        status: String,
        startedAt:Long,
        completedAt:Long
    ){
        println("editLIST STARTED")
        val query = """mutation (${"$"}mediaID: Int, ${"$"}progress: Int,${"$"}score:Float,${"$"}status:MediaListStatus,${"$"}start:FuzzyDateInput,${"$"}completed:FuzzyDateInput) {SaveMediaListEntry(mediaId: ${"$"}mediaID, progress: ${"$"}progress, score: ${"$"}score, status:${"$"}status, startedAt: ${"$"}start, completedAt: ${"$"}completed) {id}}"""
        val variables = """{\"mediaID\":\"$mediaID\",\"progress\":\"$progress\",\"score\":\"$score\",\"status\":\"${status.uppercase()}\",\"startedAt\":\"$startedAt\",\"completedAt\":\"$completedAt\"}"""

        println(executeQuery(query,variables))
        println("editLIST Finished")
    }

    fun testMutation(){
        editList(1,24,"9.2","completed",1637280000,1637366400)
    }

}