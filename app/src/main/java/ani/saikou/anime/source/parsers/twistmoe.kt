package ani.saikou.anime.source.parsers


import ani.saikou.anime.Episode
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import org.jsoup.Jsoup
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets
import java.util.*

val twistSecret = "267041df55ca2b36f2e322d05ee2c9cf".toByteArray()

object DecodeTwistSources{
    private fun base64decode(oriString:String): ByteArray {
//        return Base64.getDecoder().decode(oriString)
        return android.util.Base64.decode(oriString, android.util.Base64.DEFAULT)
    }

    private fun md5(input:ByteArray): ByteArray {
        return MessageDigest.getInstance("MD5").digest(input)
    }

    private fun generateKey(salt:ByteArray): ByteArray {
        var key = md5(twistSecret +salt)
        var currentKey = key
        while (currentKey.size < 48){
            key = md5(key + twistSecret + salt)
            currentKey += key
        }
        return currentKey
    }

    private fun decryptSourceUrl(decryptionKey:ByteArray, sourceUrl: String): String {
        val cipherData = base64decode(sourceUrl)
        val encrypted = cipherData.copyOfRange(16, cipherData.size)
        val aesCBC = Cipher.getInstance("AES/CBC/PKCS5Padding")

        Objects.requireNonNull(aesCBC).init(Cipher.DECRYPT_MODE, SecretKeySpec(
            decryptionKey.copyOfRange(0,32),
            "AES"),
            IvParameterSpec(decryptionKey.copyOfRange(32,decryptionKey.size))
        )
        val decryptedData = aesCBC!!.doFinal(encrypted)
        return String(decryptedData, StandardCharsets.UTF_8)
    }

    fun decryptSource(input:String): String {
        return decryptSourceUrl(generateKey(base64decode(input).copyOfRange(8,16)),input)
    }
}

fun twistSearchQuery(malId:String,ep:String): List<Episode.StreamLinks?> {
    val animeJson = Jsoup.connect("https://api.twist.moe/api/anime").ignoreContentType(true).get().body().text()
    val answer = Regex(""""mal_id": $malId,(.|\n)+?"slug": "(.+?)"""").find(animeJson)?.destructured?.component2()
        ?: return listOf(null)
    val episode = Json.decodeFromString<JsonArray>(
        Jsoup.connect("https://api.twist.moe/api/anime/$answer/sources").ignoreContentType(true).get().body().text()
    )[ep.toInt()].jsonObject["source"].toString().trim('"')

    return mutableListOf(
        Episode.StreamLinks(
            "twist",
            listOf(
                Episode.Quality(
                    url = "https://cdn.twist.moe${DecodeTwistSources.decryptSource(episode)}",
                    quality = "unknown",
                    size = 0
                )
            ),
            "https://twist.moe/"
        )
    )
}

fun allEpisdoesTwist(malId:String): MutableList<Episode> {
    val responseList = mutableListOf<Episode>()
    val animeJson = Jsoup.connect("https://api.twist.moe/api/anime").ignoreContentType(true).get().body().text()
    val slug = Regex(""""mal_id": $malId,(.|\n)+?"slug": "(.+?)"""").find(animeJson)?.destructured?.component2()

    val slugURL = "https://api.twist.moe/api/anime/$slug/sources"

    (1 .. Json.decodeFromString<JsonArray>(
        Jsoup.connect(slugURL).ignoreContentType(true).get().body().text()
    ).size).forEach{
        responseList.add(Episode(
            number = it.toString(),
            link = slugURL
        ))
    }
    return responseList
}

//fun testTwist(){
//    println(twistSearchQuery("5525","3"))
//}