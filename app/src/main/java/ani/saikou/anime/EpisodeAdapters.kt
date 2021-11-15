package ani.saikou.anime

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ani.saikou.databinding.ItemEpisodeCompactBinding
import ani.saikou.databinding.ItemEpisodeGridBinding
import ani.saikou.databinding.ItemEpisodeListBinding
import ani.saikou.media.Media
import com.squareup.picasso.Picasso


fun episodeAdapter(media:Media,fragment: AnimeSourceFragment,style:Int,reversed:Boolean=false,start:Int=0,end:Int?=null): RecyclerView.Adapter<*> {
    return when (style){
        0 -> EpisodeListAdapter(media, fragment,reversed,start,end?:media.anime!!.episodes!!.size)
        1 -> EpisodeGridAdapter(media, fragment,reversed,start,end?:media.anime!!.episodes!!.size)
        2 -> EpisodeCompactAdapter(media, fragment,reversed,start,end?:media.anime!!.episodes!!.size)
        else -> EpisodeGridAdapter(media, fragment,reversed,start,end?:media.anime!!.episodes!!.size)
    }
}

class EpisodeCompactAdapter(
    private val media: Media,
    private val fragment: AnimeSourceFragment,
    private val reversed: Boolean,
    private val start: Int,
    private val end: Int,
): RecyclerView.Adapter<EpisodeCompactAdapter.EpisodeCompactViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeCompactViewHolder {
        val binding = ItemEpisodeCompactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EpisodeCompactViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: EpisodeCompactViewHolder, position: Int) {
        val binding = holder.binding
        val ep = (if (reversed) media.anime!!.episodes!!.values.reversed() else media.anime!!.episodes!!.values).elementAt(start+position)
        binding.itemEpisodeNumber.text = ep.number
    }

    override fun getItemCount(): Int = end-start

    inner class EpisodeCompactViewHolder(val binding: ItemEpisodeCompactBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                fragment.onEpisodeClick(media,(if (reversed) media.anime!!.episodes!!.values.reversed() else media.anime!!.episodes!!.values).elementAt(start+bindingAdapterPosition).number)
            }
        }
    }
}

class EpisodeGridAdapter(
    private val media: Media,
    private val fragment: AnimeSourceFragment,
    private val reversed: Boolean,
    private val start: Int,
    private val end: Int,
): RecyclerView.Adapter<EpisodeGridAdapter.EpisodeGridViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeGridViewHolder {
        val binding = ItemEpisodeGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EpisodeGridViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: EpisodeGridViewHolder, position: Int) {
        val binding = holder.binding
        val ep = (if (reversed) media.anime!!.episodes!!.values.reversed() else media.anime!!.episodes!!.values).elementAt(start+position)
        Picasso.get().load(ep.thumb?:media.cover).into(binding.itemEpisodeImage)
        binding.itemEpisodeNumber.text = ep.number
        binding.itemEpisodeTitle.text = ep.title?:media.name
    }

    override fun getItemCount(): Int = end-start

    inner class EpisodeGridViewHolder(val binding: ItemEpisodeGridBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                fragment.onEpisodeClick(media,(if (reversed) media.anime!!.episodes!!.values.reversed() else media.anime!!.episodes!!.values).elementAt(start+bindingAdapterPosition).number)
            }
        }
    }
}

class EpisodeListAdapter(
    private val media: Media,
    private val fragment: AnimeSourceFragment,
    private val reversed: Boolean,
    private val start: Int,
    private val end: Int,
): RecyclerView.Adapter<EpisodeListAdapter.EpisodeListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeListViewHolder {
        val binding = ItemEpisodeListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EpisodeListViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: EpisodeListViewHolder, position: Int) {
        val binding = holder.binding
        val ep = (if (reversed) media.anime!!.episodes!!.values.reversed() else media.anime!!.episodes!!.values).elementAt(start+position)
        Picasso.get().load(ep.thumb?:media.cover).into(binding.itemEpisodeImage)
        binding.itemEpisodeNumber.text = ep.number
        binding.itemEpisodeDesc.text = ep.desc?:""
        binding.itemEpisodeTitle.text = ep.title?:media.name
    }

    override fun getItemCount(): Int = end-start

    inner class EpisodeListViewHolder(val binding: ItemEpisodeListBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                fragment.onEpisodeClick(media,(if (reversed) media.anime!!.episodes!!.values.reversed() else media.anime!!.episodes!!.values).elementAt(start+bindingAdapterPosition).number)
            }
        }
    }
}

