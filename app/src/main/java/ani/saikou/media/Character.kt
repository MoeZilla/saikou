package ani.saikou.media

import java.util.*
import kotlin.collections.ArrayList

data class Character(
    val id:Int,
    val name:String,
    val image:String,
    val role:String,

    var description:String?=null,
    var age:String?=null,
    var gender:String?=null,
    var dateOfBirth:Date?=null,
    var roles:ArrayList<Media>?=null
)