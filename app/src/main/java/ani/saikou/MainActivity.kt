package ani.saikou

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ani.saikou.anilist.anilist
import ani.saikou.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initActivity(window, binding.navbarContainer)

        if (!isOnline(this)) {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            startActivity(
                Intent(
                    this,
                    NoInternet::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }

        if (anilist.getSavedToken(this)) {

            //Load Data
            val navbar = binding.navbar
            bottomBar = navbar
            val mainViewPager = binding.viewpager
            mainViewPager.isUserInputEnabled = false
            mainViewPager.adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
            navbar.setupWithViewPager2(mainViewPager)
            navbar.selectTabAt(1)
            mainViewPager.post { mainViewPager.setCurrentItem(1, false) }
        } else {
            //Login
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, LoginFragment()).addToBackStack(null).commit()
        }
    }

    //Double Tap Back
    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

}