package ani.saikou.manga.source

import ani.saikou.manga.source.parsers.MangaPill

object MangaSources {
    private val mangaParsers:MutableMap<Int,MangaParser> = mutableMapOf()
    operator fun get(i:Int):MangaParser?{
        val a = when(i){
            0->mangaParsers.getOrPut(i, { MangaPill() })
            else -> null
        }
        return a
    }
    fun flushLive(){
        mangaParsers.forEach{
            it.value.live.value=null
        }
    }
}