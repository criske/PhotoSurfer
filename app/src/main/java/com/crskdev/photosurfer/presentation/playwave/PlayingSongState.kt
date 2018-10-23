package com.crskdev.photosurfer.presentation.playwave

sealed class PlayingSongState(val song: SongUI?, val position: Long, val positionDisplay: String) {
    //static states
    object None : PlayingSongState(null, 0, "")
    class Prepare(song: SongUI, position: Long, positionDisplay: String) : PlayingSongState(song, position, positionDisplay)
    class Ready(song: SongUI, position: Long, positionDisplay: String) : PlayingSongState(song, position, positionDisplay)
    class Stopped(song: SongUI, position: Long, positionDisplay: String) : PlayingSongState(song, position, positionDisplay)


    //dynamic states
    abstract class Dynamic(song: SongUI, position: Long, positionDisplay: String): PlayingSongState(song, position, positionDisplay)
    class Playing(song: SongUI, position: Long, positionDisplay: String) : Dynamic(song, position, positionDisplay)
    class Seeking(song: SongUI, position: Long,  positionDisplay: String, val stateBeforeSeek: PlayingSongState,
                  val confirmedToPlayAt: Boolean) :  Dynamic(song, position, positionDisplay)
    class Paused(song: SongUI,  position: Long,positionDisplay: String) : Dynamic(song, position, positionDisplay)
    class Completed(song: SongUI,  position: Long,  positionDisplay: String) : Dynamic(song, position, positionDisplay)
}