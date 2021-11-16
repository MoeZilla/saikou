
package ani.saikou.anime


import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import ani.saikou.*
import ani.saikou.databinding.ActivityExoplayerBinding
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.TrackSelectionDialogBuilder
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.Util

class ExoplayerView : AppCompatActivity(), Player.Listener {
    private lateinit var binding : ActivityExoplayerBinding
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var dataSourceFactory: DataSource.Factory
    private lateinit var trackSelector: DefaultTrackSelector
    private lateinit var playerView: PlayerView
    private lateinit var exoQuality: ImageButton
    private lateinit var exoSpeed: ImageButton
    private lateinit var mediaItem : MediaItem

    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var isFullscreen = false
    private var isPlayerPlaying = true
    private var trackDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExoplayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val episode:Episode = intent.getSerializableExtra("ep")!! as Episode

        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)

        playerView = findViewById(R.id.player_view)
        exoQuality = playerView.findViewById(R.id.exo_quality)
        exoSpeed = playerView.findViewById(R.id.exo_playback_speed)

        dataSourceFactory = DataSource.Factory {
            val dataSource: HttpDataSource = DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true).createDataSource()
            // Set a custom authentication request header.
            dataSource.setRequestProperty("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.63 Safari/537.36")
            if (episode.streamLinks!![episode.selectedStream]!!.referer!=null) dataSource.setRequestProperty("referer", episode.streamLinks!![episode.selectedStream]!!.referer!!)
            dataSource
        }
        println(episode.streamLinks!![episode.selectedStream]!!.quality[episode.selectedQuality].url)
        mediaItem = MediaItem.Builder()
            .setUri(episode.streamLinks!![episode.selectedStream]!!.quality[episode.selectedQuality].url)
            .build()

        exoQuality.setOnClickListener{
            if(trackDialog == null){
                initPopupQuality()
            }
            trackDialog?.show()
        }
        val speedsName:Array<String> = resources.getStringArray(R.array.exo_playback_speeds)
        val speeds: IntArray = resources.getIntArray(R.array.exo_speed_multiplied_by_100)
        var curSpeed = 3
        var speed: Float
        val speedDialog = AlertDialog.Builder(this).setTitle("Speed")

        exoSpeed.setOnClickListener{
            speedDialog.setSingleChoiceItems(speedsName,curSpeed) { dialog, i ->
                speed = (speeds[i]).toFloat() / 100
                curSpeed = i
                exoPlayer.playbackParameters = PlaybackParameters(speed)
                dialog.dismiss()
            }.show()
        }


        if (savedInstanceState != null) {
            currentWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW)
            playbackPosition = savedInstanceState.getLong(STATE_RESUME_POSITION)
            isFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN)
            isPlayerPlaying = savedInstanceState.getBoolean(STATE_PLAYER_PLAYING)
        }
    }

    private fun initPlayer(){

        trackSelector = DefaultTrackSelector(this)
        // When player is initialized it'll be played with a quality of MaxVideoSize to prevent loading in 1080p from the start
        trackSelector.setParameters(trackSelector.buildUponParameters().setMaxVideoSize(
            MAX_WIDTH,
            MAX_HEIGHT
        ))
        exoPlayer = ExoPlayer.Builder(this).setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory)).setTrackSelector(trackSelector).build().apply {
            playWhenReady = isPlayerPlaying
            seekTo(currentWindow, playbackPosition)
            setMediaItem(mediaItem)
            prepare()
        }
        playerView.player = exoPlayer

        //Listener on player
        exoPlayer.addListener(object: Player.Listener{
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if(playbackState == Player.STATE_READY){
                    exoQuality.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun releasePlayer(){
        isPlayerPlaying = exoPlayer.playWhenReady
        playbackPosition = exoPlayer.currentPosition
        currentWindow = exoPlayer.currentMediaItemIndex
        exoPlayer.release()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_RESUME_WINDOW, exoPlayer.currentMediaItemIndex)
        outState.putLong(STATE_RESUME_POSITION, exoPlayer.currentPosition)
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, isFullscreen)
        outState.putBoolean(STATE_PLAYER_PLAYING, isPlayerPlaying)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initPlayer()
            playerView.onResume()
        }
    }

    override fun onResume() {
        super.onResume()
        playerView.useController = true
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            playerView.onPause()
            releasePlayer()
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        playerView.keepScreenOn = isPlaying
    }

    // QUALITY SELECTOR

    private fun initPopupQuality() {
        val mappedTrackInfo = trackSelector.currentMappedTrackInfo
        var videoRenderer : Int? = null

        if(mappedTrackInfo == null) return else exoQuality.visibility = View.VISIBLE

        for(i in 0 until mappedTrackInfo.rendererCount){
            if(isVideoRenderer(mappedTrackInfo, i)){
                videoRenderer = i
            }
        }

        if(videoRenderer == null){
            exoQuality.visibility = View.GONE
            return
        }

        val trackSelectionDialogBuilder = TrackSelectionDialogBuilder(this, getString(R.string.quality_selector), trackSelector, videoRenderer)
        trackSelectionDialogBuilder.setTrackNameProvider{ it.height.toString()+"p" }
        trackDialog = trackSelectionDialogBuilder.build()
    }

    private fun isVideoRenderer(mappedTrackInfo: MappingTrackSelector.MappedTrackInfo, rendererIndex: Int): Boolean {
        val trackGroupArray = mappedTrackInfo.getTrackGroups(rendererIndex)
        if (trackGroupArray.length == 0) {
            return false
        }
        val trackType = mappedTrackInfo.getRendererType(rendererIndex)
        return C.TRACK_TYPE_VIDEO == trackType
    }

}