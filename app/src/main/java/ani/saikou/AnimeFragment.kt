package ani.saikou

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import ani.saikou.anilist.AnilistAnimeViewModel
import ani.saikou.databinding.FragmentAnimeBinding
import ani.saikou.media.MediaLargeAdaptor
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.abs


class AnimeFragment : Fragment() {
    private var _binding: FragmentAnimeBinding? = null
    private val binding get() = _binding!!
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var trendHandler :Handler?=null
    private lateinit var trendRun : Runnable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAnimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() { super.onDestroyView();_binding = null }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val model: AnilistAnimeViewModel by viewModels()
        binding.animeContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = statusBarHeight
            bottomMargin = navBarHeight
        }
        binding.animeRefresh.setSlingshotDistance(statusBarHeight+128)
        binding.animeRefresh.setProgressViewEndTarget(false, statusBarHeight+128)
        binding.animeRefresh.setOnRefreshListener {
            animeRefresh.postValue(true)
        }
        if(anilist.avatar!=null){
            Picasso.get().load(anilist.avatar).into(binding.homeUserAvatar)
            binding.homeUserAvatar.scaleType = ImageView.ScaleType.FIT_CENTER
        }
        model.getTrending().observe(viewLifecycleOwner,{
            if(it!=null){
                binding.animeTrendingViewPager.adapter = MediaLargeAdaptor(it,binding.animeTrendingViewPager,requireActivity())
                binding.animeTrendingViewPager.offscreenPageLimit = 3
                binding.animeTrendingViewPager.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

                val a = CompositePageTransformer()
                a.addTransformer(MarginPageTransformer(40))
                a.addTransformer { page, position ->
                    page.scaleY = 0.85f + (1 - abs(position))*0.15f
                }
                binding.animeTrendingViewPager.setPageTransformer(a)
                trendHandler = Handler(Looper.getMainLooper())
                trendRun = Runnable {
                    binding.animeTrendingViewPager.currentItem = binding.animeTrendingViewPager.currentItem+1
                }
                binding.animeTrendingViewPager.registerOnPageChangeCallback(
                    object : ViewPager2.OnPageChangeCallback(){
                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                            trendHandler!!.removeCallbacks(trendRun)
                            trendHandler!!.postDelayed(trendRun,4000)
                        }
                    }
                )
                println("$it")
            }
        })
        animeRefresh.observe(viewLifecycleOwner,{
            if(it) {
                scope.launch {
                    model.loadTrending()
                    requireActivity().runOnUiThread {
                        animeRefresh.postValue(false)
                        binding.animeRefresh.isRefreshing = false
                    }
                }
            }
        })

    }

    override fun onPause() {
        super.onPause()
        trendHandler?.removeCallbacks(trendRun)
    }

    override fun onResume() {
        super.onResume()
        trendHandler?.postDelayed(trendRun,4000)
    }
}