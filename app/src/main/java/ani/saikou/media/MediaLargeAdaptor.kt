package ani.saikou.media

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT

import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

import ani.saikou.R
import ani.saikou.databinding.ItemMediaLargeBinding
import com.squareup.picasso.Picasso

import java.io.Serializable

class MediaLargeAdaptor(
    private val mediaList: ArrayList<Media>, val activity: Activity,private val viewPager: ViewPager2?=null
    ) : RecyclerView.Adapter<MediaLargeAdaptor.MediaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaLargeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        if (viewPager!=null) binding.itemContainer.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        return MediaViewHolder(binding)
    }
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val b = holder.binding
        val media = mediaList[position]
        Picasso.get().load(media.cover).into(b.itemCompactImage)
        Picasso.get().load(media.banner).into(b.itemCompactBanner)
        b.itemCompactOngoing.visibility = if (media.status=="RELEASING")  View.VISIBLE else View.GONE
        b.itemCompactTitle.text = media.userPreferredName
        b.itemCompactScore.text = ((if(media.userScore==0) (media.meanScore?:0) else media.userScore)/10.0).toString()
        b.itemCompactScoreBG.background = ContextCompat.getDrawable(b.root.context,(if (media.userScore!=0) R.drawable.item_user_score else R.drawable.item_score))
        if (media.anime!=null){
            b.itemCompactTotal.text = if (media.anime.nextAiringEpisode!=null) (media.anime.nextAiringEpisode.toString()+" / "+(media.anime.totalEpisodes?:"~").toString()) else (media.anime.totalEpisodes?:"~").toString()
        }
        else if(media.manga!=null){
            b.itemCompactTotal.text = "${media.manga.totalChapters?:"~"}"
        }
        @SuppressLint("NotifyDataSetChanged")
        if (position == mediaList.size-2 && viewPager!=null) viewPager.post {
            mediaList.addAll(mediaList)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = mediaList.size

    inner class MediaViewHolder(val binding: ItemMediaLargeBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val media = mediaList[bindingAdapterPosition]
                ContextCompat.startActivity(
                   activity,
                    Intent(activity, MediaDetailsActivity::class.java).putExtra("media",media as Serializable),
                        ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                        Pair.create(binding.itemCompactImage,ViewCompat.getTransitionName(binding.itemCompactImage)!!)
                    ).toBundle()
                )
            }
        }
    }
}