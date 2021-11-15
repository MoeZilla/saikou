package ani.saikou.anime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ani.saikou.databinding.FragmentAnimeSourceBinding
import ani.saikou.media.MediaDetailsViewModel
import android.content.Intent
import android.net.Uri
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.GridLayoutManager
import ani.saikou.R
import ani.saikou.anime.source.parsers.getGogoStream
import ani.saikou.media.Media
import ani.saikou.navBarHeight
import com.google.android.material.chip.Chip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.ceil


class AnimeSourceFragment : Fragment() {
    private var _binding: FragmentAnimeSourceBinding? = null
    private val binding get() = _binding!!
    private val scope = CoroutineScope(Dispatchers.Default)
    private var style = 0
    private var gridCount = 1
    private var reversed = false
    private var selected:ImageView?=null
    private var selectedChip:Chip?= null
    private var start = 0
    private var end:Int?=null
    private var loading = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAnimeSourceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() { super.onDestroyView();_binding = null }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val screenWidth = resources.displayMetrics.run { widthPixels / density }
        binding.animeSourceContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> { bottomMargin += navBarHeight }
        super.onViewCreated(view, savedInstanceState)
        val model : MediaDetailsViewModel by activityViewModels()
        if (selected==null){
            selected = binding.animeSourceList
        }
        model.getMedia().observe(this,{
            val media = it
            if (media?.anime != null){
                if (media.anime.youtube!=null) {
                    binding.animeSourceYT.visibility = View.VISIBLE
                    binding.animeSourceYT.setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(media.anime.youtube))
                        context!!.startActivity(intent)
                    }
                }
                val sources : Array<String> = resources.getStringArray(R.array.anime_sources)
                binding.animeSource.setText("GOGO")
                binding.animeSource.setAdapter(ArrayAdapter(requireContext(), R.layout.item_dropdown,sources))
                binding.animeSource.setOnItemClickListener { _, _, i, _ ->
                    loading=true
                    binding.animeSourceProgressBar.visibility=View.VISIBLE
                    scope.launch{
                        model.loadEpisodes(media,i)
                    }
                }

                binding.animeSourceTop.setOnClickListener {
                    binding.animeSourceTop.rotation = if (reversed) 90f else -90f
                    reversed=!reversed
                    updateRecycler(media)
                }
                binding.animeSourceList.setOnClickListener {
                    style=0
                    gridCount = 1
                    selected?.alpha = 0.33f
                    selected = binding.animeSourceList
                    selected?.alpha = 1f
                    updateRecycler(media)
                }
                binding.animeSourceGrid.setOnClickListener {
                    style=1
                    gridCount = (screenWidth/200f).toInt()
                    selected?.alpha = 0.33f
                    selected = binding.animeSourceGrid
                    selected?.alpha = 1f
                    updateRecycler(media)
                }
                binding.animeSourceCompact.setOnClickListener {
                    style=2
                    gridCount = (screenWidth/80f).toInt()
                    selected?.alpha = 0.33f
                    selected = binding.animeSourceCompact
                    selected?.alpha = 1f
                    updateRecycler(media)
                }

                model.getEpisodes().observe(this,{episodes->
//                    println("Ow : $episodes")
                    binding.animeSouceChipGroup.removeAllViews()

                    if (episodes!=null) {
//                        println("Episodes Loaded : $episodes")
                        episodes.forEach { (i, episode) ->
                            if (media.anime.kitsuEpisodes!=null) {
                                if (media.anime.kitsuEpisodes!!.containsKey(i)) {
                                    episode.desc = media.anime.kitsuEpisodes!![i]?.desc
                                    episode.title = media.anime.kitsuEpisodes!![i]?.title
                                    episode.thumb = media.anime.kitsuEpisodes!![i]?.thumb?:media.cover
                                }
                            }
                        }
//                        println("Episodes Kitsu : $episodes")
                        media.anime.episodes = episodes
                        //CHIP GROUP
                        addPageChips(media,episodes.size)
                        updateRecycler(media)
                    }
                })
                model.getKitsuEpisodes().observe(this,{ i->
                    if (i!=null) {
//                        println("Kitsu Loaded : $i")
                        media.anime.kitsuEpisodes = i
                    }
                })
                scope.launch{
                    model.loadKitsuEpisodes(media.nameRomaji)
                    model.loadEpisodes(media,0)
                }

            }
        })
    }
    private fun updateRecycler(media: Media){
        if(media.anime?.episodes!=null) {
            binding.animeEpisodesRecycler.adapter = episodeAdapter(media, this, style, reversed, start, end)
            binding.animeEpisodesRecycler.layoutManager = GridLayoutManager(requireContext(), gridCount)
            loading = false
            binding.animeSourceProgressBar.visibility = View.GONE
        }
    }
    fun onEpisodeClick(media: Media, i:String){
        if (media.anime?.episodes?.get(i)!=null) {
            media.anime.episodes!![i] = when (media.anime.source) {
                0 -> getGogoStream(media.anime.episodes!![i]!!)
                1 -> getGogoStream(media.anime.episodes!![i]!!)
                else -> media.anime.episodes!![i]!!
            }
        }
        println("Episode $i : ${media.anime?.episodes!![i]}")
    }


    private fun addPageChips(media: Media, episode: Int){
        val divisions = episode.toDouble() / 10
        val limit = when{
            (divisions < 25) -> 25
            (divisions < 50) -> 50
            else -> 100
        }
        if (episode>limit) {
            val stored = ceil((episode).toDouble() / limit).toInt()
            println(stored)
            (1..stored).forEach {
                val chip = Chip(requireContext())
                chip.isCheckable = true

                if(it==1 && selectedChip==null){
                    selectedChip=chip
                    chip.isChecked = true
                }
                if (end == null) { end = limit * it - 1 }
                if (it == stored) {
                    chip.text = "${limit * (it - 1) + 1} - $episode"
                    chip.setOnClickListener { _ ->
                        selectedChip?.isChecked = false
                        selectedChip = chip
                        selectedChip!!.isChecked = true
                        start = limit * (it - 1)
                        end = episode - 1
                        updateRecycler(media)
                    }
                } else {
                    chip.text = "${limit * (it - 1) + 1} - ${limit * it}"
                    chip.setOnClickListener { _ ->
                        selectedChip?.isChecked = false
                        selectedChip = chip
                        selectedChip!!.isChecked = true
                        start = limit * (it - 1)
                        end = limit * it - 1
                        updateRecycler(media)
                    }
                }
                binding.animeSouceChipGroup.addView(chip)
            }
        }
        else{
            updateRecycler(media)
        }
    }

}