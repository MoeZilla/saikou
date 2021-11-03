package ani.saikou.media

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ani.saikou.R
import ani.saikou.databinding.ItemCompactBinding
import com.squareup.picasso.Picasso
import java.io.Serializable

class MediaAdaptor(
    private val mediaList: ArrayList<Media>, private val context: Context
    ) : RecyclerView.Adapter<MediaAdaptor.MediaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemCompactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaViewHolder(binding)
    }
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val b = holder.binding
        val anime = mediaList[position].anime
        val manga = mediaList[position].manga
        if (anime!=null){
            Picasso.get().load(anime.cover).into(b.itemCompactImage)
            b.itemCompactOngoing.visibility = if (anime.status=="RELEASING")  View.VISIBLE else View.GONE
            b.itemCompactTitle.text = anime.userPreferredName
            b.itemCompactScore.text = ((if(anime.userScore==0) (anime.meanScore?:0) else anime.userScore)/10.0).toString()
            b.itemCompactScoreBG.background = ContextCompat.getDrawable(b.root.context,(if (anime.userScore!=0) R.drawable.item_user_score else R.drawable.item_score))
            b.itemCompactUserProgress.text = (anime.userProgress?:"~").toString()
            b.itemCompactTotal.text = " / ${if (anime.nextAiringEpisode!=null) (anime.nextAiringEpisode.toString()+" / "+(anime.totalEpisodes?:"~").toString()) else (anime.totalEpisodes?:"~").toString()}"
        }
        else if(manga!=null){
            Picasso.get().load(manga.cover).into(b.itemCompactImage)
            b.itemCompactOngoing.visibility = if (manga.status=="RELEASING")  View.VISIBLE else View.GONE
            b.itemCompactTitle.text = manga.userPreferredName
            b.itemCompactScore.text = ((if(manga.userScore==0) (manga.meanScore?:0) else manga.userScore)/10.0).toString()
            b.itemCompactScoreBG.background = ContextCompat.getDrawable(b.root.context,(if (manga.userScore!=0) R.drawable.item_user_score else R.drawable.item_score))
            b.itemCompactUserProgress.text = (manga.userProgress?:"~").toString()
            b.itemCompactTotal.text = " / ${manga.totalChapters?:"~"}"
        }
    }

    override fun getItemCount() = mediaList.size

    inner class MediaViewHolder(val binding: ItemCompactBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val media = mediaList[bindingAdapterPosition]
                ContextCompat.startActivity(
                    context,
                    Intent(context, MediaActivity::class.java).putExtra("media",media as Serializable),
                    null
                )
            }
        }
    }
}