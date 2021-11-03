package ani.saikou.media

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.math.MathUtils
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import ani.saikou.R
import ani.saikou.anime.AnimeSourceFragment
import ani.saikou.databinding.ActivityMediaBinding
import ani.saikou.initActivity
import ani.saikou.manga.MangaSourceFragment
import ani.saikou.statusBarHeight
import ani.saikou.toPx
import com.bartoszlipinski.viewpropertyobjectanimator.ViewPropertyObjectAnimator
import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs

class MediaActivity : AppCompatActivity(), AppBarLayout.OnOffsetChangedListener {
    
    private var isCollapsed = false
    private var bannerHeight :Int = 0
    private val percent = 50
    private var mMaxScrollSize = 0
    
    private lateinit var binding: ActivityMediaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initActivity(window)
        bannerHeight = binding.mediaBanner.height
        binding.mediaBanner.updateLayoutParams{ height += statusBarHeight }
        binding.mediaClose.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = statusBarHeight }
        binding.mediaCover.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin += statusBarHeight }
        binding.MediaTitle.isSelected = true
        mMaxScrollSize = binding.mediaAppBar.totalScrollRange
        binding.mediaAppBar.addOnOffsetChangedListener(this)

        val media: Media = intent.getSerializableExtra("Media") as Media
        val tabLayout = binding.mediaTab
        val viewPager = binding.mediaViewPager
        viewPager.isUserInputEnabled = false
        viewPager.adapter = ViewPagerAdapter(supportFragmentManager, lifecycle,media.anime!=null)
        tabLayout.setupWithViewPager2(viewPager)
    }

    //ViewPager
    private class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle,private val anime:Boolean=true) :
        FragmentStateAdapter(fragmentManager, lifecycle) {

        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            if (anime){
                when (position) {
                    0 -> return MediaInfoFragment()
                    1 -> return AnimeSourceFragment()
                }
            }
            else{
                when (position) {
                    0 -> return MediaInfoFragment()
                    1 -> return MangaSourceFragment()
                }
            }
            return MediaInfoFragment()
        }
    }
    //Collapsing UI Stuff
    override fun onOffsetChanged(appBar: AppBarLayout, i: Int) {
        if (mMaxScrollSize == 0) mMaxScrollSize = appBar.totalScrollRange
        val percentage = abs(i) * 100 / mMaxScrollSize
        val cap = MathUtils.clamp((percent - percentage) / percent.toFloat(), 0f, 1f)

        binding.mediaCover.scaleX = 1f*cap
        binding.mediaCover.scaleY = 1f*cap
        binding.mediaCover.cardElevation = 32f*cap
        appBar.post{appBar.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = ((1f - cap) * statusBarHeight).toInt() }}


        if (percentage >= percent && !isCollapsed) {
            isCollapsed = true
            ObjectAnimator.ofArgb(binding.mediaClose,"ColorFilter", ContextCompat.getColor(this, R.color.bg_opp)).setDuration(200).start()
            binding.MediaTitle.post{
                ViewPropertyObjectAnimator.animate(binding.MediaTitle).leftMargin(24.toPx.toInt()).setDuration(200).start()
                ViewPropertyObjectAnimator.animate(binding.MediaTitle).rightMargin(64.toPx.toInt()).setDuration(200).start()
            }
            this.window.statusBarColor = ContextCompat.getColor(this, R.color.nav_bg)
        }
        if (percentage <= percent && isCollapsed) {
            isCollapsed = false
            ObjectAnimator.ofArgb(binding.mediaClose,"ColorFilter", ContextCompat.getColor(this, R.color.nav_bg)).setDuration(200).start()
            binding.MediaTitle.post{
                ViewPropertyObjectAnimator.animate(binding.MediaTitle).leftMargin(148.toPx.toInt()).setDuration(200).start()
                ViewPropertyObjectAnimator.animate(binding.MediaTitle).rightMargin(32.toPx.toInt()).setDuration(200).start()
            }
            this.window.statusBarColor = ContextCompat.getColor(this, R.color.status)
        }
    }
}