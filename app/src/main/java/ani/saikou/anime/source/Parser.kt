package ani.saikou.anime.source

import ani.saikou.anime.Episode
import ani.saikou.media.Media

abstract class Parser {
    abstract fun getStream(episode: Episode):Episode
    abstract fun getEpisodes(media: Media):MutableMap<String,Episode>
    abstract fun search(string: String):ArrayList<SourceAnime>
}