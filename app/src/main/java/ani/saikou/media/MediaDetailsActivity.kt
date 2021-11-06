package ani.saikou.media

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.ViewGroup

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
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

import com.google.android.material.appbar.AppBarLayout
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import nl.joery.animatedbottombar.AnimatedBottomBar
import kotlin.math.abs

class MediaDetailsActivity : AppCompatActivity(), AppBarLayout.OnOffsetChangedListener {

    private lateinit var binding: ActivityMediaBinding
    private val scope = CoroutineScope(Dispatchers.Default)
    private val model: MediaDetailsViewModel by viewModels()

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
        var tabLayout : AnimatedBottomBar? = null

        val media: Media = intent.getSerializableExtra("media") as Media
        Picasso.get().load(media.cover).into(binding.mediaCoverImage)
        Picasso.get().load(media.banner).into(binding.mediaBanner)
        Picasso.get().load(media.banner).into(binding.mediaBannerStatus)
        binding.mediaTitle.text=media.userPreferredName
        binding.mediaTitleCollapse.text=media.userPreferredName


        //Buttons could be done in a better way by making a class but uhh
        //Fav Button
        if (media.isFav){
            binding.mediaFav.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.ic_round_favorite_24))
        }
        var favPressable = true
        binding.mediaFav.setOnClickListener {
            if (favPressable){
                favPressable = false
                media.isFav = !media.isFav
                ObjectAnimator.ofFloat(binding.mediaFav,"scaleX",1f,0f).setDuration(69).start()
                ObjectAnimator.ofFloat(binding.mediaFav,"scaleY",1f,0f).setDuration(100).start()
                scope.launch {
                    delay(100)
                    runOnUiThread {
                        if (media.isFav) {
                            ObjectAnimator.ofArgb(binding.mediaFav,"ColorFilter",ContextCompat.getColor(this@MediaDetailsActivity, R.color.nav_tab),ContextCompat.getColor(this@MediaDetailsActivity, R.color.fav)).setDuration(120).start()
                            binding.mediaFav.setImageDrawable(AppCompatResources.getDrawable(this@MediaDetailsActivity,R.drawable.ic_round_favorite_24))
                        }
                        else{
                            binding.mediaFav.setImageDrawable(AppCompatResources.getDrawable(this@MediaDetailsActivity,R.drawable.ic_round_favorite_border_24))
                        }
                        ObjectAnimator.ofFloat(binding.mediaFav,"scaleX",0f,1.5f).setDuration(120).start()
                        ObjectAnimator.ofFloat(binding.mediaFav,"scaleY",0f,1.5f).setDuration(100).start()
                    }
                    delay(120)
                    runOnUiThread {
                        ObjectAnimator.ofFloat(binding.mediaFav,"scaleX",1.5f,1f).setDuration(100).start()
                        ObjectAnimator.ofFloat(binding.mediaFav,"scaleY",1.5f,1f).setDuration(100).start()
                    }
                    delay(200)
                    runOnUiThread{
                        if (media.isFav) {
                            ObjectAnimator.ofArgb(binding.mediaFav,"ColorFilter", ContextCompat.getColor(this@MediaDetailsActivity, R.color.fav), ContextCompat.getColor(this@MediaDetailsActivity, R.color.nav_tab)).setDuration(200).start()
                        }
                    }
                    favPressable = true
                }
            }
        }

        //Notify Button
        var notify = false
        if (notify){
            binding.mediaNotify.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.ic_round_notifications_active_24))
        }
        var notifyPressable = true
        binding.mediaNotify.setOnClickListener {
            if (notifyPressable){
                notifyPressable = false
                notify = !notify
                ObjectAnimator.ofFloat(binding.mediaNotify,"scaleX",1f,0f).setDuration(69).start()
                ObjectAnimator.ofFloat(binding.mediaNotify,"scaleY",1f,0f).setDuration(100).start()
                scope.launch {
                    delay(100)
                    runOnUiThread {
                        if (notify) {
                            ObjectAnimator.ofArgb(binding.mediaNotify,"ColorFilter",ContextCompat.getColor(this@MediaDetailsActivity, R.color.nav_tab),ContextCompat.getColor(this@MediaDetailsActivity, R.color.violet_400)).setDuration(120).start()
                            binding.mediaNotify.setImageDrawable(AppCompatResources.getDrawable(this@MediaDetailsActivity, R.drawable.ic_round_notifications_active_24))
                        }
                        else{
                            binding.mediaNotify.setImageDrawable(AppCompatResources.getDrawable(this@MediaDetailsActivity, R.drawable.ic_round_notifications_none_24))
                        }
                        ObjectAnimator.ofFloat(binding.mediaNotify,"scaleX",0f,1.5f).setDuration(120).start()
                        ObjectAnimator.ofFloat(binding.mediaNotify,"scaleY",0f,1.5f).setDuration(100).start()
                    }
                    delay(120)
                    runOnUiThread {
                        ObjectAnimator.ofFloat(binding.mediaNotify,"scaleX",1.5f,1f).setDuration(100).start()
                        ObjectAnimator.ofFloat(binding.mediaNotify,"scaleY",1.5f,1f).setDuration(100).start()
                    }
                    delay(200)
                    runOnUiThread{
                        if (notify) {
                            ObjectAnimator.ofArgb(binding.mediaNotify,"ColorFilter", ContextCompat.getColor(this@MediaDetailsActivity, R.color.violet_400), ContextCompat.getColor(this@MediaDetailsActivity, R.color.nav_tab)).setDuration(200).start()
                        }
                    }
                    notifyPressable = true
                }
            }
        }

        if (media.anime!=null){
            tabLayout = binding.mediaAnimeTab
            viewPager.adapter = ViewPagerAdapter(supportFragmentManager, lifecycle,true)
        }
        else if (media.manga!=null){
            tabLayout = binding.mediaMangaTab
            viewPager.adapter = ViewPagerAdapter(supportFragmentManager, lifecycle,false)
        }
        binding.mediaTitle.translationX = -screenWidth
        tabLayout!!.visibility = View.VISIBLE
        tabLayout.setupWithViewPager2(viewPager)

        scope.launch {
            delay(2000)
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

        override fun getItemCount(): Int = 2

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
    private var isCollapsed = false
    private val percent = 50
    private var mMaxScrollSize = 0
    private var screenWidth:Float = 0f

    override fun onOffsetChanged(appBar: AppBarLayout, i: Int) {
        if (mMaxScrollSize == 0) mMaxScrollSize = appBar.totalScrollRange
        val percentage = abs(i) * 100 / mMaxScrollSize
        val cap = MathUtils.clamp((percent - percentage) / percent.toFloat(), 0f, 1f)

        binding.mediaCover.scaleX = 1f*cap
        binding.mediaCover.scaleY = 1f*cap
        binding.mediaCover.cardElevation = 32f*cap

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