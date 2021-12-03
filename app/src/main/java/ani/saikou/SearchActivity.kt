package ani.saikou

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import ani.saikou.anilist.AnilistSearch
import ani.saikou.databinding.ActivitySearchBinding
import ani.saikou.media.MediaAdaptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySearchBinding
    private val scope = CoroutineScope(Dispatchers.Default)
    private var screenWidth:Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initActivity(window)

        screenWidth = resources.displayMetrics.run { widthPixels / density }
        binding.searchBar.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin += statusBarHeight }
        binding.searchScrollContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin += statusBarHeight;bottomMargin += navBarHeight }

        val type:String = intent.getStringExtra("type")!!
        val model: AnilistSearch by viewModels()
        model.getSearch().observe(this,{
            if(it!=null){
                binding.searchRecyclerView.adapter = MediaAdaptor(it.results,this,true)
                binding.searchRecyclerView.layoutManager = GridLayoutManager(this, (screenWidth/124f).toInt())
                binding.searchProgress.visibility = View.GONE
                binding.searchRecyclerView.visibility = View.VISIBLE
                if(it.hasNextPage) {
                    binding.searchProgress.visibility = View.VISIBLE
                    binding.searchScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, _, _, _ ->
                        if (!v.canScrollVertically(1)) {
                            if (it.hasNextPage) scope.launch { model.loadNextPage(it) }
                            else binding.searchProgress.visibility = View.GONE
                        }
                    })
                }
            }
        })
        binding.searchBarText.requestFocusFromTouch()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        fun searchTitle(){
            binding.searchRecyclerView.adapter=null
            binding.searchProgress.visibility = View.VISIBLE
            binding.searchRecyclerView.visibility = View.GONE
            binding.searchBarText.clearFocus()
            imm.hideSoftInputFromWindow(binding.searchBarText.windowToken, 0)
            val search = binding.searchBarText.text.toString()
            scope.launch {
                model.loadSearch(type,search)
            }
        }

        binding.searchBarText.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    searchTitle()
                    true
                }
                else -> false
            }
        }
        binding.searchBar.setEndIconOnClickListener{searchTitle()}


    }
}