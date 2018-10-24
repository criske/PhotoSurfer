package com.crskdev.photosurfer.presentation.playwave

sealed class PlayingSongState(val song: SongUI?) {
    //static states
    object None : PlayingSongState(null)
    class Prepare(song: SongUI) : PlayingSongState(song)
    class Ready(song: SongUI) : PlayingSongState(song)
    class Stopped(song: SongUI) : PlayingSongState(song)

    //dynamic states
    abstract class Dynamic(song: SongUI, val position: Int, val positionDisplay: String): PlayingSongState(song)
    class Playing(song: SongUI, position: Int, positionDisplay: String) : Dynamic(song, position, positionDisplay)
    class Seeking(song: SongUI, position: Int,  positionDisplay: String, val stateBeforeSeek: PlayingSongState,
                  val confirmedToPlayAt: Boolean) :  Dynamic(song, position, positionDisplay)
    class Paused(song: SongUI,  position: Int,positionDisplay: String) : Dynamic(song, position, positionDisplay)
    class Completed(song: SongUI,  position: Int,  positionDisplay: String) : Dynamic(song, position, positionDisplay)
}