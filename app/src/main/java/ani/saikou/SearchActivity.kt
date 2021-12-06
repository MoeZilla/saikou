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
import androidx.core.view.updatePaddingRelative
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        binding.searchScrollContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin += statusBarHeight }
        binding.searchRecyclerView.updateLayoutParams{ height=resources.displayMetrics.heightPixels+navBarHeight }
        binding.searchProgress.updateLayoutParams<ViewGroup.MarginLayoutParams> { bottomMargin += navBarHeight }
        binding.searchRecyclerView.updatePaddingRelative(bottom = navBarHeight+80f.px)

        binding.searchScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, _, _, _ ->
            if(!v.canScrollVertically(1))
                binding.searchRecyclerView.suppressLayout(false)
            if(!v.canScrollVertically(-1))
                binding.searchRecyclerView.suppressLayout(true)
        })

        val type:String = intent.getStringExtra("type")!!
        val model: AnilistSearch by viewModels()
        model.getSearch().observe(this,{
            if(it!=null){
                val adapter = MediaAdaptor(it.results,this,true)
                var loading = false
                binding.searchRecyclerView.adapter = adapter
                binding.searchRecyclerView.layoutManager = GridLayoutManager(this, (screenWidth/124f).toInt())
                binding.searchProgress.visibility = View.GONE
                binding.searchRecyclerView.visibility = View.VISIBLE
                if(it.hasNextPage) {
                    binding.searchProgress.visibility = View.VISIBLE
                    binding.searchRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(v: RecyclerView, dx: Int, dy: Int) {
                            if (!v.canScrollVertically(1)) {
                                if (it.hasNextPage && !loading) scope.launch {
                                    if (!loading) {
                                        loading=true
                                        val get = model.loadNextPage(it)
                                        val a = it.results.size
                                        it.results.addAll(get.results)
                                        runOnUiThread {
                                            adapter.notifyItemRangeInserted(a,get.results.size)
                                            binding.searchProgress.visibility = View.GONE
                                        }
                                        it.page = get.page
                                        it.hasNextPage = get.hasNextPage
                                        loading=false
                                    }
                                }
                                else binding.searchProgress.visibility = View.GONE
                            }
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