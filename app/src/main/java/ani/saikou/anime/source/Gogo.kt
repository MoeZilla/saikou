package ani.saikou.anime.source

import ani.saikou.anime.Episode

fun getGogoEpisodes(title: String,dub : Boolean = false): ArrayList<Episode> {
    //TODO(GET EPISODE'S LINK)
    return arrayListOf<Episode>()
}

fun getGogoStream(episode: Episode) : Episode{
//    episode.link
    //TODO(GET EPISODE'S STREAMABLE LINKS)
    episode.streamLinks = arrayListOf<Episode.StreamLinks>()
    return episode
}