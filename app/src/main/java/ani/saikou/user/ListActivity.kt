package ani.saikou.user

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import ani.saikou.R
import ani.saikou.databinding.ActivityListBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListBinding
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var load = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.nav_bg)
        val anime = intent.getBooleanExtra("anime",true)
        binding.listTitle.text = intent.getStringExtra("username")+"'s "+(if(anime) "Anime" else "Manga")+" List"

        val model : ListViewModel by viewModels()
        model.getLists().observe(this,{
            if(it!=null){
                binding.listProgressBar.visibility = View.GONE
                binding.listViewPager.adapter = ListViewPagerAdapter(it.size, this)
                TabLayoutMediator(binding.listTabLayout, binding.listViewPager) { tab, position ->
                    tab.text = it.keys.toList()[position]
                }.attach()
            }
        })
        if(!load){
            scope.launch {
                model.loadLists(anime,intent.getIntExtra("userId",0))
            }
            load = true
        }
    }
}