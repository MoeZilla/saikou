package ani.saikou.anilist

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import java.io.File


var anilist : Anilist = Anilist()

class Anilist {
    val query : AnilistQueries = AnilistQueries()

    var token : String? = null
    var username : String? = null
    var userid : Int? = null
    var avatar : String? = null
    var episodesWatched : Int? = null
    var chapterRead : Int? = null
    var genres:Map<String,String>?=null

    fun loginIntent(context: Context){
        val clientID = 6818

        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse("https://anilist.co/api/v2/oauth/authorize?client_id=$clientID&response_type=token"))
    }

    fun getSavedToken(context: Context):Boolean{
        if ("anilistToken" in context.fileList()){
            token = File(context.filesDir, "anilistToken").readText()
            return true
        }
        return false
    }
}
