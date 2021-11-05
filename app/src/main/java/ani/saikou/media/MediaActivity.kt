package ani.saikou.media

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.math.MathUtils
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import ani.saikou.R
import ani.saikou.anilist.anilist
import ani.saikou.anime.AnimeSourceFragment
import ani.saikou.databinding.ActivityMediaBinding
import ani.saikou.initActivity
import ani.saikou.manga.MangaSourceFragment
import ani.saikou.statusBarHeight
import com.google.android.material.appbar.AppBarLayout
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import nl.joery.animatedbottombar.AnimatedBottomBar
import kotlin.math.abs
import androidx.lifecycle.ViewModelProvider
import ani.saikou.anilist.AnilistHomeViewModel


class MediaActivity : AppCompatActivity(), AppBarLayout.OnOffsetChangedListener {
    
    private var isCollapsed = false
    private val percent = 50
    private var mMaxScrollSize = 0
    private var screenWidth:Float = 0f

    private lateinit var binding: ActivityMediaBinding
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var tabLayout: AnimatedBottomBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        screenWidth = resources.displayMetrics.widthPixels.toFloat()

        //Ui init
        initActivity(window)

        binding.mediaBanner.updateLayoutParams{ height += statusBarHeight }
        binding.mediaBannerStatus.updateLayoutParams{ height += statusBarHeight }
        binding.mediaBanner.translationY = -statusBarHeight.toFloat()
        binding.mediaClose.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin += statusBarHeight }
        binding.mediaAppBar.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin += statusBarHeight }
        binding.mediaCover.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin += statusBarHeight }
        binding.mediaTitle.isSelected = true
        binding.mediaTitleCollapse.isSelected = true
        binding.mediaStatus.isSelected = true
        binding.mediaAddToList.isSelected = true
        mMaxScrollSize = binding.mediaAppBar.totalScrollRange
        binding.mediaFAB.hide()
        binding.mediaAppBar.addOnOffsetChangedListener(this)

        binding.mediaClose.setOnClickListener{
            finish()
        }
        val viewPager = binding.mediaViewPager
        viewPager.isUserInputEnabled = false

        val media: Media = intent.getSerializableExtra("media") as Media
        if (media.anime!=null){
            Picasso.get().load(media.anime.cover).into(binding.mediaCoverImage)
            Picasso.get().load(media.anime.banner).into(binding.mediaBanner)
            Picasso.get().load(media.anime.banner).into(binding.mediaBannerStatus)
            binding.mediaTitle.text=media.anime.userPreferredName
            binding.mediaTitleCollapse.text=media.anime.userPreferredName
            tabLayout = binding.mediaAnimeTab
            viewPager.adapter = ViewPagerAdapter(supportFragmentManager, lifecycle,true)
        }
        else if (media.manga!=null){
            Picasso.get().load(media.manga.cover).into(binding.mediaCoverImage)
            Picasso.get().load(media.manga.banner).into(binding.mediaBanner)
            Picasso.get().load(media.manga.banner).into(binding.mediaBannerStatus)
            binding.mediaTitleCollapse.text=media.manga.userPreferredName
            binding.mediaTitle.text=media.manga.userPreferredName
            tabLayout = binding.mediaMangaTab
            viewPager.adapter = ViewPagerAdapter(supportFragmentManager, lifecycle,false)
        }
        binding.mediaTitle.translationX = -screenWidth
        tabLayout.visibility = View.VISIBLE
        tabLayout.setupWithViewPager2(viewPager)

        val model: MediaDetailsViewModel by viewModels()

        scope.launch {
            model.loadMedia(media)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
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
//        appBar.post{appBar.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = ((1f - cap) * statusBarHeight).toInt() }}


        if (percentage >= percent && !isCollapsed) {
            isCollapsed = true
            binding.mediaFAB.show()
            ObjectAnimator.ofFloat(binding.mediaTitle,"translationX",0f).setDuration(200).start()
            ObjectAnimator.ofFloat(binding.mediaAccessContainer,"translationX",screenWidth).setDuration(200).start()
            ObjectAnimator.ofFloat(binding.mediaTitleCollapse,"translationX",screenWidth).setDuration(200).start()
            binding.mediaBannerStatus.visibility=View.GONE
            this.window.statusBarColor = ContextCompat.getColor(this, R.color.nav_bg)
        }
        if (percentage <= percent && isCollapsed) {
            isCollapsed = false
            binding.mediaFAB.hide()
            ObjectAnimator.ofFloat(binding.mediaTitle,"translationX",-screenWidth).setDuration(200).start()
            ObjectAnimator.ofFloat(binding.mediaAccessContainer,"translationX",0f).setDuration(200).start()
            ObjectAnimator.ofFloat(binding.mediaTitleCollapse,"translationX",0f).setDuration(200).start()
            binding.mediaBannerStatus.visibility=View.VISIBLE
            this.window.statusBarColor = ContextCompat.getColor(this, R.color.status)
        }
    }


}