package ani.saikou

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ani.saikou.databinding.ActivityNoInternetBinding

class NoInternet : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityNoInternetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewInset(binding.refreshButtonContainer)
        binding.refreshButton.setOnClickListener {
            if (isOnline(this)) {
                startMainActivity(this)
            }
        }
    }
}