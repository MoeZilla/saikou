package ani.saikou.manga

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ani.saikou.databinding.ActivityMangaReaderBinding
import ani.saikou.initActivity
import ani.saikou.media.Media
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MangaReaderActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMangaReaderBinding
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMangaReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initActivity(this)

        var media = intent.getSerializableExtra("media") as Media
        val model : MangaChapterViewModel by viewModels()
        model.getMangaChapMedia().observe(this,{
            if(it!=null){
                media=it
                val chapImages = media.manga!!.chapters!![media.manga!!.selectedChapter]?.images
                val referer = media.manga!!.chapters!![media.manga!!.selectedChapter]?.referer
                if(chapImages!=null){
                    binding.mangaReaderRecyclerView.setHasFixedSize(true)
                    binding.mangaReaderRecyclerView.adapter = ImageAdapter(chapImages,referer)
                    binding.mangaReaderRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
                }
            }
        })
        scope.launch { model.loadChapMedia(media) }
    }
}