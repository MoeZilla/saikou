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
import ani.saikou.R
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
                binding.mediaListStatus.setText("GOGO")
                binding.mediaListStatus.setAdapter(ArrayAdapter(requireContext(), R.layout.item_dropdown,sources))
                binding.mediaListStatus.setOnItemClickListener { _, _, i, _ ->
                    scope.launch{
                        model.loadEpisodes(media,i)
                    }
                }
                model.getEpisodes().observe(this,{i->
                    media.anime.episodes = i
                })
                model.getKitsuEpisodes().observe(this,{ i->
                    media.anime.kitsuEpisodes = i
                })
                scope.launch{
                    model.loadKitsuEpisodes(media.nameRomaji)
                }
            }
        })
    }
}