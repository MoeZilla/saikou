package ani.saikou.manga

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.recyclerview.widget.RecyclerView
import ani.saikou.currActivity
import ani.saikou.databinding.ItemImageBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class ImageAdapter(
private val arr: ArrayList<String>,
private val referer:String?=null
): RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    private val sizes = mutableMapOf<Int,Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val binding = holder.binding
//        if (sizes[holder.bindingAdapterPosition]!=null) binding.root.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, sizes[holder.bindingAdapterPosition]!!)
        val a = currActivity()
        if (a!=null) Glide.with(a)
            .load(GlideUrl(
                arr[position],
                if (referer!=null) LazyHeaders.Builder().addHeader("referer", referer).build() else LazyHeaders.Builder().build()
            )).dontTransform()
            .listener(object:RequestListener<Drawable>{
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean = false
                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    binding.imgProgProgress.visibility = View.GONE
                    sizes[holder.bindingAdapterPosition] = binding.root.height
                    return false
                }
            })
            .into(binding.imgProgImage)
    }

    override fun getItemCount(): Int = arr.size

    inner class ImageViewHolder(val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root)
}