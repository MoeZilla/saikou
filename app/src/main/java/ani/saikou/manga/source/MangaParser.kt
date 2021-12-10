package ani.saikou.manga.source

import ani.saikou.manga.MangaChapter
import ani.saikou.media.Media
import ani.saikou.media.Source

abstract class MangaParser {
    abstract fun getChapter(chapter: MangaChapter): MangaChapter
    abstract fun getChapters(media: Media):MutableMap<String, MangaChapter>
    abstract fun search(string: String):ArrayList<Source>
}