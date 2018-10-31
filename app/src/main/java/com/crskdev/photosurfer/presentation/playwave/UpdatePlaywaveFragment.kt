package com.crskdev.photosurfer.presentation.playwave


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.repository.photo.PhotoRepository
import com.crskdev.photosurfer.data.repository.playwave.PlaywaveRepository
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.entities.ImageType
import com.crskdev.photosurfer.entities.PlaywavePhoto
import com.crskdev.photosurfer.presentation.photo.PhotoInfoLiveDataHelper
import com.crskdev.photosurfer.presentation.photo.listadapter.PhotoInfoSheetDisplayHelper
import com.crskdev.photosurfer.util.*
import com.crskdev.photosurfer.util.glide.GlideApp
import com.crskdev.photosurfer.util.livedata.filter
import com.crskdev.photosurfer.util.livedata.viewModelFromProvider
import com.crskdev.photosurfer.util.recyclerview.BindViewHolder
import com.crskdev.photosurfer.util.recyclerview.GridDivider
import com.crskdev.photosurfer.util.recyclerview.getSpanCountByScreenWidth
import kotlinx.android.synthetic.main.fragment_update_playwave.*
import kotlinx.android.synthetic.main.item_list_playwave_photos.view.*

class UpdatePlaywaveFragment : Fragment() {

    private lateinit var ownViewModel: UpdatePlaywaveViewModel

    private lateinit var sharedViewModel: UpsertPlaywaveViewModel

    private val infoSheetDisplayHelper = PhotoInfoSheetDisplayHelper(object : PhotoInfoSheetDisplayHelper.ActionsListener {
        override fun onClose() {
            ownViewModel.clearShowInfo()
        }

        override fun onRemoveFromCollection(collectionId: Int, photoId: String) {
            Toast.makeText(context, "Unsupported Yet!", Toast.LENGTH_SHORT).show()
        }

        override fun displayCollection(collectionId: Int) {
            Toast.makeText(context, "Unsupported Yet!", Toast.LENGTH_SHORT).show()
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ownViewModel = viewModelFromProvider(this) {
            val id = arguments?.getInt("playwaveId", -1) ?: -1
            val graph = context!!.dependencyGraph()
            val playwaveRepository = graph
            UpdatePlaywaveViewModel(id,
                    graph.playwaveRepository,
                    graph.photoRepository)
        }
        sharedViewModel = viewModelFromProvider(parentFragment!!) {
            val id = arguments?.getInt("playwaveId", -1) ?: -1
            val graph = context!!.dependencyGraph()
            UpsertPlaywaveViewModel(
                    graph.diskThreadExecutor,
                    graph.playwaveRepository,
                    graph.playwaveSoundPlayerProvider,
                    true
            ).apply {
                selectPlaywave(id)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_playwave, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        toolbarUpdatePlaywave.apply {
            inflateTintedMenu(R.menu.menu_single_confirm) {
                if (it.itemId == R.id.menu_action_confirm) {
                    activity?.hideSoftKeyboard()
                    sharedViewModel.upsertPlaywave(editUpdatePlaywaveTitle.text.toString())
                }
                true
            }
            setNavigationOnClickListener {
                navigateUp()
            }
        }
        btnUpdatePlaywaveSearch.setOnClickListener {
            findNavController().navigate(UpdatePlaywaveFragmentDirections
                    .actionUpdatePlaywaveFragmentToSearchSongFragment(),
                    defaultTransitionNavOptions())
        }
        btnUpdatePlaywavePlay.setOnClickListener {
            (it.tag as SongUI?)?.let { song -> sharedViewModel.selectSongToPlay(song) }
        }

        val adapter = PlaywavePhotosAdapter(LayoutInflater.from(context),
                GlideApp.with(this)) {
            when (it) {
                is PlaywavePhotosAdapter.Action.Remove -> ownViewModel.removePhotoFromPlaywave(it.id)
                is PlaywavePhotosAdapter.Action.Info -> ownViewModel.showInfo(it.id)
            }
        }
        recyclerUpdatePlaywave.apply {
            val spacingPx = resources.getDimensionPixelSize(R.dimen.item_photo_text_margin)
            val spans = getSpanCountByScreenWidth(resources.getDimensionPixelSize(R.dimen.item_photo_thumb_size), spacingPx)
            layoutManager = GridLayoutManager(context, spans)
            addItemDecoration(GridDivider(spacingPx, spans))
            this.adapter = adapter
        }

        playerUpdatePlaywave.setOnPlayerListener(object : PlayerView.PlayerListener {
            override fun onAction(action: PlayerView.Action) {
                when (action) {
                    is PlayerView.Action.Close -> sharedViewModel.justStop()
                    is PlayerView.Action.PlayOrStop -> sharedViewModel.playOrStopSong()
                    is PlayerView.Action.Pause -> sharedViewModel.pausePlayingSong()
                    is PlayerView.Action.SeekTo -> sharedViewModel.seekTo(action.position, action.confirmedToPlay)
                }
            }
        })

        sharedViewModel.playwaveLiveData.observe(this, Observer {
            editUpdatePlaywaveTitle.setText(it.title)
            textUpdatePlaywaveSongInfo.text = it.song?.fullInfo
            btnUpdatePlaywavePlay.apply {
                isEnabled = it.song != null
                tag = it.song
            }
            adapter.submitList(it.photos)
        })
        sharedViewModel.playingSongStateLiveData.observe(this, Observer {
            playerUpdatePlaywave.changeState(it)
        })
        sharedViewModel.messageLiveData.observe(this, Observer {
            when (it) {
                UpsertPlaywaveViewModel.Message.Updated -> {
                    Toast.makeText(context, "Playwave Updated", Toast.LENGTH_SHORT).show()
                }
                is UpsertPlaywaveViewModel.Message.Error -> {
                    Toast.makeText(context, it.err.message, Toast.LENGTH_SHORT).show()
                }
            }
        })

        ownViewModel.photoInfoLiveData.observe(this, Observer {
            infoSheetDisplayHelper.displayInfoBottomSheet(view.context, it)
        })
    }
}

class UpdatePlaywaveViewModel(private val playwaveId: Int,
                              private val playwaveRepository: PlaywaveRepository,
                              private val photoRepository: PhotoRepository) : ViewModel() {

    private val photoInfoLiveDataHelper = PhotoInfoLiveDataHelper {
        photoRepository.getPhotoLiveData(it)
    }

    val photoInfoLiveData = photoInfoLiveDataHelper.getLiveData()

    fun showInfo(photoId: String) {
        photoInfoLiveDataHelper.setPhotoId(photoId)
    }

    fun clearShowInfo() {
        photoInfoLiveDataHelper.setPhotoId(null)
    }

    fun removePhotoFromPlaywave(photoId: String) {
        playwaveRepository.removePhotoFromPlaywave(playwaveId, photoId)
    }

}

class PlaywavePhotosAdapter(private val inflater: LayoutInflater,
                            private val glide: RequestManager,
                            private val action: (Action) -> Unit) : ListAdapter<PlaywavePhoto, PlaywavePhotoVH>(
        object : DiffUtil.ItemCallback<PlaywavePhoto>() {
            override fun areItemsTheSame(oldItem: PlaywavePhoto, newItem: PlaywavePhoto): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: PlaywavePhoto, newItem: PlaywavePhoto): Boolean = oldItem == newItem
        }) {

    sealed class Action {
        class Remove(val id: String) : Action()
        class Info(val id: String) : Action()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaywavePhotoVH =
            PlaywavePhotoVH(inflater.inflate(R.layout.item_list_playwave_photos, parent, false), glide, action)

    override fun onBindViewHolder(holder: PlaywavePhotoVH, position: Int) = holder.bind(getItem(position))
}

class PlaywavePhotoVH(v: View,
                      private val glide: RequestManager,
                      private val action: (PlaywavePhotosAdapter.Action) -> Unit) : BindViewHolder<PlaywavePhoto>(v) {

    init {
        with(itemView) {
            btnPlaywavePhotoRemove.setOnClickListener { _ ->
                model?.let {
                    action(PlaywavePhotosAdapter.Action.Remove(it.id))
                }
            }
            imagePlaywavePhoto.setOnLongClickListener { _ ->
                model?.let {
                    action(PlaywavePhotosAdapter.Action.Info(it.id))
                }
                true
            }
        }
    }

    override fun onBindModel(model: PlaywavePhoto) {
        with(itemView) {
            model.urls[ImageType.SMALL]
                    ?.let {
                        glide.asDrawable()
                                .load(it)
                                .apply(RequestOptions()
                                        .transforms(CenterCrop(), RoundedCorners(8))
                                        .placeholder(R.drawable.ic_logo))
                                .into(imagePlaywavePhoto)

                    }
        }
    }

    override fun unBind() {
        glide.clear(itemView.imagePlaywavePhoto)
    }

}