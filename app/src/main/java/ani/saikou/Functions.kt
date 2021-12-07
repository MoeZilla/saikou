package ani.saikou

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources.getSystem
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.InputFilter
import android.text.Spanned
import android.util.AttributeSet
import android.view.View
import android.view.ViewAnimationUtils
import android.view.Window
import android.widget.AutoCompleteTextView
import android.widget.DatePicker
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import nl.joery.animatedbottombar.AnimatedBottomBar
import org.jsoup.Jsoup
import java.io.*
import java.util.*
import kotlin.math.max

const val STATE_RESUME_WINDOW = "resumeWindow"
const val STATE_RESUME_POSITION = "resumePosition"
const val STATE_PLAYER_FULLSCREEN = "playerFullscreen"
const val STATE_PLAYER_PLAYING = "playerOnPlay"
const val MAX_HEIGHT = 500
const val MAX_WIDTH = 900
const val buildDebug = true

var statusBarHeight  = 0
var navBarHeight = 0
val Int.dp: Float get() = (this / getSystem().displayMetrics.density)
val Float.px: Int get() = (this * getSystem().displayMetrics.density).toInt()

lateinit var bottomBar: AnimatedBottomBar
var selectedOption = 1


var homeRefresh = MutableLiveData(true)
var animeRefresh = MutableLiveData(true)
var mangaRefresh = MutableLiveData(true)

fun logger(e:Any?,print:Boolean=true){
    if(buildDebug && print)
        println(e)
}

fun saveData(context: Context,fileName:String,data:Any){
    val fos: FileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
    val os = ObjectOutputStream(fos)
    os.writeObject(data)
    os.close()
    fos.close()
}

@Suppress("UNCHECKED_CAST")
fun <T> loadData(context: Context,fileName:String): T? {
    if (fileName in context.fileList()){
        val fileIS: FileInputStream = context.openFileInput(fileName)
        val objIS = ObjectInputStream(fileIS)
        val data = objIS.readObject() as T
        objIS.close()
        fileIS.close()
        return data
    }
    return null
}

fun initActivity(window:Window,view:View?=null) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    if (view != null) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            statusBarHeight = insets.top
            navBarHeight = insets.bottom
            WindowInsetsCompat.CONSUMED
        }
    }
}


fun isOnline(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    if (capabilities != null) {
        when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                logger("Device on Cellular")
                return true
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                logger("Device on Wifi")
                return true
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                logger("Device on Ethernet, TF man?")
                return true
            }
        }
    }
    return false
}

fun startMainActivity(activity: Activity){
    activity.finishAffinity()
    activity.startActivity(Intent(activity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
}

data class FuzzyDate(
    val year: Int?=null,
    val month: Int?=null,
    val day: Int?=null,
): Serializable{
    override fun toString():String{
        return (if (day!=null) "$day / " else "")+(if (month!=null) "$month / " else "")+(year?.toString() ?: "")
    }
    fun getToday():FuzzyDate{
        val cal = Calendar.getInstance()
        return FuzzyDate(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH))
    }
    fun getEpoch():Long{
        val cal = Calendar.getInstance()
        cal.set(year?:cal.get(Calendar.YEAR),month?:cal.get(Calendar.MONTH),day?:cal.get(Calendar.DAY_OF_MONTH))
        return cal.timeInMillis
    }
}

class DatePickerFragment(activity: Activity, var date: FuzzyDate=FuzzyDate().getToday()) : DialogFragment(), DatePickerDialog.OnDateSetListener {
    var dialog :DatePickerDialog
    init{
        val c = Calendar.getInstance()
        val year = date.year?:c.get(Calendar.YEAR)
        val month= if (date.month!=null) date.month!! -1 else c.get(Calendar.MONTH)
        val day = date.day?:c.get(Calendar.DAY_OF_MONTH)
        dialog = DatePickerDialog(activity, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        date = FuzzyDate(year,month+1,day)
    }
}

class InputFilterMinMax(private val min: Double, private val max: Double,private val status:AutoCompleteTextView?=null) : InputFilter {
    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        try {
            val input = (dest.toString() + source.toString()).toDouble()
            if (isInRange(min, max, input)) return null
        } catch (nfe: NumberFormatException) {
            nfe.printStackTrace()
        }
        return ""
    }

    @SuppressLint("SetTextI18n")
    private fun isInRange(a: Double, b: Double, c: Double): Boolean {
        if (c==b) {
            status?.setText("COMPLETED",false)
            status?.parent?.requestLayout()
        }
        return if (b > a) c in a..b else c in b..a
    }
}

fun getMalTitle(id:Int) : String{
    return Jsoup.connect("https://myanimelist.net/anime/$id").ignoreHttpErrors(true).get().select(".title-name").text()
}

class ZoomOutPageTransformer(private val bottom:Boolean=false) : ViewPager2.PageTransformer {
    override fun transformPage(view: View, position: Float) {
        if (position == 0.0f) {
            var cy = 0
            if (bottom) cy = view.height
            val a = ViewAnimationUtils.createCircularReveal(view, view.width / 2, cy, 0f, max(view.height, view.width).toFloat())
            a.interpolator = LinearOutSlowInInterpolator()
            a.setDuration(400).start()
        }
    }
}

class FadingEdgeRecyclerView : RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun isPaddingOffsetRequired(): Boolean {
        return !clipToPadding
    }

    override fun getLeftPaddingOffset(): Int {
        return if (clipToPadding) 0 else -paddingLeft
    }

    override fun getTopPaddingOffset(): Int {
        return if (clipToPadding) 0 else -paddingTop
    }

    override fun getRightPaddingOffset(): Int {
        return if (clipToPadding) 0 else paddingRight
    }

    override fun getBottomPaddingOffset(): Int {
        return if (clipToPadding) 0 else paddingBottom
    }
}