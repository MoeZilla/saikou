package ani.saikou.media

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ani.saikou.databinding.ItemCharacterBinding
import com.squareup.picasso.Picasso

class CharacterAdapter(
    private val characterList: ArrayList<Character>
): RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val binding = ItemCharacterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CharacterViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        val binding = holder.binding
        val character = characterList[position]
        binding.itemCompactRelation.text = character.role+"\t"
        Picasso.get().load(character.image).into(binding.itemCompactImage)
        binding.itemCompactTitle.text = character.name
    }

    override fun getItemCount(): Int = characterList.size
    inner class CharacterViewHolder(val binding: ItemCharacterBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                println(characterList[bindingAdapterPosition].toString())
            }
        }
    }
}