package ani.saikou

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ani.saikou.anilist.AnilistViewModel
import ani.saikou.anilist.Media
import ani.saikou.anilist.MediaAdaptor
import ani.saikou.anilist.anilist
import ani.saikou.databinding.FragmentHomeBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@OptIn(DelicateCoroutinesApi::class)
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView();_binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val model: AnilistViewModel by viewModels()
        var listImagesLoaded = false
        var watchingLoaded = false
        var readingLoaded = false
        var recommendedLoaded = false

        viewInset(binding.fragmentHome)

        //UserData
        binding.homeUserDataProgressBar.visibility = View.VISIBLE
        binding.homeUserDataContainer.visibility = View.GONE
        model.getUserData().observe(viewLifecycleOwner, {
            if (it) {
                binding.homeUserName.text = anilist.username
                binding.homeUserEpisodesWatched.text = anilist.episodesWatched.toString()
                binding.homeUserChaptersRead.text = anilist.chapterRead.toString()
                Picasso.get().load(anilist.avatar).into(binding.homeUserAvatar)

                binding.homeUserDataProgressBar.visibility = View.GONE
                binding.homeUserDataContainer.visibility = View.VISIBLE
            }
        })

        GlobalScope.launch {
            //Get userData First
            if (anilist.userid == null) model.loadUserData()
            //get List Images in new Thread
            launch {
                if (!listImagesLoaded) model.setListImages()
            }
            //get Continue in new Thread
            launch {
                if (!watchingLoaded) model.setAnimeContinue()
                if (!readingLoaded) model.setMangaContinue()
            }
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
                        recyclerView.adapter = MediaAdaptor(it)
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