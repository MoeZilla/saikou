package ani.saikou.media

import ani.saikou.anime.Anime
import ani.saikou.manga.Manga
import java.io.Serializable

data class Media(
    val anime: Anime? = null,
    val manga: Manga? = null,
) : Serializable