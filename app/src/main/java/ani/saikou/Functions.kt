package ani.saikou

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.InputFilter
import android.text.Spanned
import android.view.View
import android.view.Window
import android.widget.DatePicker
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import nl.joery.animatedbottombar.AnimatedBottomBar
import java.io.Serializable
import java.util.*


const val buildDebug = true
var statusBarHeight  = 0
var navBarHeight = 0
//val Number.toPx get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics)
lateinit var bottomBar: AnimatedBottomBar

fun logger(e:Any?,print:Boolean=true){
    if(buildDebug && print)
        println(e)
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
                logger("NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                logger("NetworkCapabilities.TRANSPORT_WIFI")
                return true
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                logger("NetworkCapabilities.TRANSPORT_ETHERNET")
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
}

class DatePickerFragment( activity: Activity, defaultDate: FuzzyDate) : DialogFragment(), DatePickerDialog.OnDateSetListener {
    var date:FuzzyDate?=null
    var dialog :DatePickerDialog
    init{
        val c = Calendar.getInstance()
        val year = defaultDate.year?:c.get(Calendar.YEAR)
        val month= if (defaultDate.month!=null) defaultDate.month-1 else c.get(Calendar.MONTH)
        val day = defaultDate.day?:c.get(Calendar.DAY_OF_MONTH)
        dialog = DatePickerDialog(activity, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        date = FuzzyDate(year,month+1,day)
    }
}

class InputFilterMinMax(private val min: Double, private val max: Double) : InputFilter {
    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        try {
            val input = (dest.toString() + source.toString()).toDouble()
            if (isInRange(min, max, input)) return null
        } catch (nfe: NumberFormatException) {
            nfe.printStackTrace()
        }
        return ""
    }

    private fun isInRange(a: Double, b: Double, c: Double): Boolean {
        return if (b > a) c in a..b else c in b..a
    }
}