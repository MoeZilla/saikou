package ani.saikou

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ani.saikou.anilist.AnilistHomeViewModel
import ani.saikou.anilist.anilist
import ani.saikou.databinding.FragmentHomeBinding
import ani.saikou.media.Media
import ani.saikou.media.MediaAdaptor
import com.squareup.picasso.Picasso
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@OptIn(DelicateCoroutinesApi::class)
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView();_binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val model: AnilistHomeViewModel by viewModels()
        var listImagesLoaded = false
        var watchingLoaded = false
        var readingLoaded = false
        var recommendedLoaded = false

        binding.fragmentHome.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = statusBarHeight
            bottomMargin = navBarHeight
        }

        var reached = false
        binding.homeScroll.setOnScrollChangeListener { _, _, _, _, _ ->
            if (!binding.homeScroll.canScrollVertically(1)) {
                reached = true
                bottomBar.animate().translationZ(0f).setDuration(200).start()
                ObjectAnimator.ofFloat(bottomBar, "elevation", 4f, 0f).setDuration(200).start()
            }
            else{
                if (reached){
                    bottomBar.animate().translationZ(12f).setDuration(200).start()
                    ObjectAnimator.ofFloat(bottomBar, "elevation", 0f, 4f).setDuration(200).start()
                }
            }
        }

        //UserData
        binding.homeUserDataProgressBar.visibility = View.VISIBLE
        binding.homeUserDataContainer.visibility = View.GONE
        fun load(){
            requireActivity().runOnUiThread {
                binding.homeUserName.text = anilist.username
                binding.homeUserEpisodesWatched.text = anilist.episodesWatched.toString()
                binding.homeUserChaptersRead.text = anilist.chapterRead.toString()
                Picasso.get().load(anilist.avatar).into(binding.homeUserAvatar)

                binding.homeUserDataProgressBar.visibility = View.GONE
                binding.homeUserDataContainer.visibility = View.VISIBLE
            }
        }
        GlobalScope.launch {
            //Get userData First
            if (anilist.userid == null) {
                if(anilist.query.getUserData()){
                    load()
                }
                else{
                    println("Error loading data")
                }
            }
            else{load()}
            //get Watching in new Thread
            launch {
                if (!watchingLoaded) model.setAnimeContinue()
            }
            //get Reading in new Thread
            launch {
                if (!readingLoaded) model.setMangaContinue()
            }
            //get List Images in current Thread(idle)
            if (!listImagesLoaded) model.setListImages()

            //get Recommended in current Thread(idle)
            if (!recommendedLoaded) model.setRecommendation()
        }

        //List Images
        model.getListImages().observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                listImagesLoaded = true
                Picasso.get().load(it[0] ?: "https://bit.ly/31bsIHq")
                    .into(binding.homeAnimeListImage)
                Picasso.get().load(it[1] ?: "https://bit.ly/2ZGfcuG")
                    .into(binding.homeMangaListImage)
            }
        })

        //Function For Recycler Views
        fun initRecyclerView(mode: Int, recyclerView: RecyclerView, progress: View, empty: View) {
            lateinit var modelFunc: LiveData<ArrayList<Media>>
            when (mode) {
                0 -> modelFunc = model.getAnimeContinue();1 -> modelFunc =
                model.getMangaContinue();2 -> modelFunc = model.getRecommendation()
            }
            progress.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE

            modelFunc.observe(viewLifecycleOwner, {
                if (it != null) {
                    when (mode) {
                        0 -> watchingLoaded = true
                        1 -> readingLoaded = true
                        2 -> recommendedLoaded = true
                    }
                    if (it.isNotEmpty()) {
                        recyclerView.adapter = MediaAdaptor(it,requireActivity())
                        recyclerView.layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        recyclerView.visibility = View.VISIBLE
                    } else {
                        empty.visibility = View.VISIBLE
                    }
                    progress.visibility = View.GONE
                }
            })
        }

        // Recycler Views
        initRecyclerView(
            0,
            binding.homeWatchingRecyclerView,
            binding.homeWatchingProgressBar,
            binding.homeWatchingEmpty
        )
        initRecyclerView(
            1,
            binding.homeReadingRecyclerView,
            binding.homeReadingProgressBar,
            binding.homeReadingEmpty
        )
        initRecyclerView(
            2,
            binding.homeRecommendedRecyclerView,
            binding.homeRecommendedProgressBar,
            binding.homeRecommendedEmpty
        )
    }
}