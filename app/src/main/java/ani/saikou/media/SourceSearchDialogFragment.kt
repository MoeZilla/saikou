package ani.saikou.media

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import ani.saikou.anime.source.AnimeSourceAdapter
import ani.saikou.anime.source.AnimeSources
import ani.saikou.databinding.BottomSheetSourceSearchBinding
import ani.saikou.manga.source.MangaSourceAdapter
import ani.saikou.manga.source.MangaSources
import ani.saikou.navBarHeight
import ani.saikou.px
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SourceSearchDialogFragment : BottomSheetDialogFragment(){

    private var _binding: BottomSheetSourceSearchBinding? = null
    private val binding get() = _binding!!
    private val scope = CoroutineScope(Dispatchers.Default)
    lateinit var model : MediaDetailsViewModel
    var anime = true
    var i : Int?=null
    var id : Int?=null
    var media : Media? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetSourceSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.mediaListContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> { bottomMargin += navBarHeight }

        val m : MediaDetailsViewModel by activityViewModels()
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        model = m

        model.getMedia().observe(viewLifecycleOwner,{
            media = it
            if (media!=null){
                binding.mediaListProgressBar.visibility = View.GONE
                binding.mediaListLayout.visibility = View.VISIBLE

                binding.searchRecyclerView.visibility = View.GONE
                binding.searchProgress.visibility = View.VISIBLE

                i = media!!.selected!!.source
                if (media!!.anime!=null){
                    val source = AnimeSources[i!!]!!
                    binding.searchSourceTitle.text = source.name
                    binding.searchBarText.setText(media!!.getMangaName())
                    fun search(){
                        binding.searchBarText.clearFocus()
                        imm.hideSoftInputFromWindow(binding.searchBarText.windowToken, 0)
                        scope.launch {
                            model.sources.postValue(source.search(binding.searchBarText.text.toString()))
                        }
                    }
                    binding.searchBarText.setOnEditorActionListener { _, actionId, _ ->
                        return@setOnEditorActionListener when (actionId) {
                            EditorInfo.IME_ACTION_SEARCH -> {
                                search()
                                true
                            }
                            else -> false
                        }
                    }
                    binding.searchBar.setEndIconOnClickListener{ search() }
                    search()
                }else if(media!!.manga!=null){
                    anime = false
                    val source = MangaSources[i!!]!!
                    binding.searchSourceTitle.text = source.name
                    binding.searchBarText.setText(media!!.getMangaName())
                    fun search(){
                        binding.searchBarText.clearFocus()
                        imm.hideSoftInputFromWindow(binding.searchBarText.windowToken, 0)
                        scope.launch {
                            model.sources.postValue(source.search(binding.searchBarText.text.toString()))
                        }
                    }
                    binding.searchBarText.setOnEditorActionListener { _, actionId, _ ->
                        return@setOnEditorActionListener when (actionId) {
                            EditorInfo.IME_ACTION_SEARCH -> {
                                search()
                                true
                            }
                            else -> false
                        }
                    }
                    binding.searchBar.setEndIconOnClickListener{ search() }
                    search()
                }

            }
        })
        model.sources.observe(viewLifecycleOwner,{
            if (it!=null) {
                binding.searchRecyclerView.visibility = View.VISIBLE
                binding.searchProgress.visibility = View.GONE
                binding.searchRecyclerView.adapter = if (anime) AnimeSourceAdapter(it,model,i!!,media!!.id,this,scope) else MangaSourceAdapter(it,model,i!!,media!!.id,this,scope)
                binding.searchRecyclerView.layoutManager = GridLayoutManager(requireActivity(),requireActivity().resources.displayMetrics.widthPixels / 124f.px)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun dismiss() {
        model.sources.value = null
        super.dismiss()
    }
}