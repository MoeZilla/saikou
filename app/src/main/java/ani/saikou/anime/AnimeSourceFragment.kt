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
import androidx.recyclerview.widget.GridLayoutManager
import ani.saikou.R
import ani.saikou.anime.source.parsers.getGogoStream
import ani.saikou.media.Media
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AnimeSourceFragment : Fragment() {
    private var _binding: FragmentAnimeSourceBinding? = null
    private val binding get() = _binding!!
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAnimeSourceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() { super.onDestroyView();_binding = null }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val screenWidth = resources.displayMetrics.run { widthPixels / density }
        super.onViewCreated(view, savedInstanceState)
        val model : MediaDetailsViewModel by activityViewModels()
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
                    scope.launch{
                        model.loadEpisodes(media,i)
                    }
                }
                model.getEpisodes().observe(this,{episodes->
                    println("Ow : $episodes")
                    if (episodes!=null) {
                        println("Episodes Loaded : $episodes")
                        episodes.forEach { (i, episode) ->
                            if (media.anime.kitsuEpisodes!=null) {
                                if (media.anime.kitsuEpisodes!!.containsKey(i)) {
                                    episode.desc = media.anime.kitsuEpisodes!![i]?.desc
                                    episode.title = media.anime.kitsuEpisodes!![i]?.title
                                    episode.thumb = media.anime.kitsuEpisodes!![i]?.thumb?:media.cover
                                }
                            }
                        }
                        println("Episodes Kitsu : $episodes")
                        media.anime.episodes = episodes
                        binding.animeEpisodesRecycler.adapter = episodeAdapter(media, this, 0)
                        binding.animeEpisodesRecycler.layoutManager = GridLayoutManager(requireContext(), (screenWidth/200f).toInt())
                    }
                })
                model.getKitsuEpisodes().observe(this,{ i->
                    if (i!=null) {
                        println("Kitsu Loaded : $i")
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
}