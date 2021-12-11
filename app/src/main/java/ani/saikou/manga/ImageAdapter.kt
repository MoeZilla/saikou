package ani.saikou.manga

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ani.saikou.databinding.ItemImageBinding
import com.squareup.picasso.Picasso

class ImageAdapter(
private val arr: ArrayList<String>
): RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        val binding = holder.binding
        Picasso.get()
            .load(arr[position])
            .into(binding.imgProgImage,object: com.squareup.picasso.Callback {
                override fun onSuccess() {
                    binding.imgProgProgress.visibility = View.GONE
                }
                override fun onError(e: Exception) {}
            })
    }

    override fun getItemCount(): Int = arr.size

    inner class ImageViewHolder(val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root)
}