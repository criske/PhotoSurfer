package com.crskdev.photosurfer.presentation.playwave

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.crskdev.photosurfer.MockActivity
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.util.getDrawableCompat
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.IllegalStateException
import java.util.*


/**
 * Created by Cristian Pela on 19.10.2018.
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class PlayerViewTest {

    @Rule
    @JvmField
    val activityRule = ActivityTestRule<MockActivity>(MockActivity::class.java, true, true)

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val song = SongUI(
            1, "path", "title", "artist", "100", "fullInfo",
            100L, true, null)

    @Before
    fun setup() {
        MockActivity.layout = R.layout.player_view_test_layout
    }

    @Test
    fun testPlayerStates() {

        val activity = activityRule.activity
        val player = activity.findViewById<PlayerView>(R.id.playerViewTest)
        assertView<PlayerView>(R.id.playerViewTest) { !isVisible }

        //start play song
        player.postThis { changeState(PlayingSongState.Started(song)) }
        assertView<PlayerView>(R.id.playerViewTest) { isVisible }
        assertView<TextView>(R.id.textPlayerSongInfo) { text == song.fullInfo }
        assertView<ImageButton>(R.id.imgBtnPlayerPlayStop) {
            val actual = drawable.copy().wash()
            val expected = context.getDrawableCompat(R.drawable.ic_play_arrow_white_24dp)?.wash()
            isVisible && actual.equalsPixels(expected)
        }
        assertView<ImageButton>(R.id.imgBtnPlayerPause) { !isVisible }
        assertView<SeekBar>(R.id.seekBarPlayer) { !isEnabled }

        //song position in time
        player.postThis { changeState(PlayingSongState.Playing(song, 10)) }
        assertView<SeekBar>(R.id.seekBarPlayer) { progress == 10 }
        assertView<ImageButton>(R.id.imgBtnPlayerPause) { isVisible }
        assertView<ImageButton>(R.id.imgBtnPlayerPlayStop) {
            val actual = drawable.copy().wash()
            val expected = context.getDrawableCompat(R.drawable.ic_stop_white_24dp)?.wash()
            actual.equalsPixels(expected)
        }
        assertView<SeekBar>(R.id.seekBarPlayer) { isEnabled }

        //pause
        player.postThis {
            changeState(PlayingSongState.Playing(song, 20))
            changeState(PlayingSongState.Paused(song))
        }
        assertView<SeekBar>(R.id.seekBarPlayer) { progress == 20 }
        assertView<ImageButton>(R.id.imgBtnPlayerPlayStop) {
            val actual = drawable.copy().wash()
            val expected = context.getDrawableCompat(R.drawable.ic_play_arrow_white_24dp)?.wash()
            actual.equalsPixels(expected)
        }

        //pause
        player.postThis { changeState(PlayingSongState.Playing(song, 20)) }
        assertView<SeekBar>(R.id.seekBarPlayer) { progress == 20 }
        assertView<ImageButton>(R.id.imgBtnPlayerPlayStop) {
            val actual = drawable.copy().wash()
            val expected = context.getDrawableCompat(R.drawable.ic_stop_white_24dp)?.wash()
            actual.equalsPixels(expected)
        }

        //stop
        player.postThis { changeState(PlayingSongState.Stopped(song)) }
        assertView<SeekBar>(R.id.seekBarPlayer) { progress == 0 }
        assertView<ImageButton>(R.id.imgBtnPlayerPlayStop) {
            val actual = drawable.copy().wash()
            val expected = context.getDrawableCompat(R.drawable.ic_play_arrow_white_24dp)?.wash()
            actual.equalsPixels(expected)
        }
        assertView<ImageButton>(R.id.imgBtnPlayerPause) { !isVisible }
    }

    @Test
    fun testPlayerActions() {

//        var actions = listOf<PlayerView.Action>()
//
//        val activity = activityRule.activity
//        activity.runOnUiThread {
//            val player = activity.findViewById<PlayerView>(R.id.playerViewTest)
//            player.onActionListener(object : PlayerView.PlayerListener {
//                override fun onAction(action: PlayerView.Action) {
//                    actions += action
//                }
//            })
//            changeState(activity, AndroidTestLiveData<PlayingSongState>(activity.handler()).apply {
//                setValue(PlayingSongState.Playing(song, 10, 100))
//            })
//        }
//
//        onData(withId(R.id.imgBtnAddPlaywavePlay))
    }

}

//todo move into a utility file
private inline fun <reified V : View> assertView(@IdRes id: Int, notMatchedMessage: String? = null, crossinline predicate: V.() -> Boolean) {
    Espresso.onView(ViewMatchers.withId(id)).check(ViewAssertions.matches(boundedMatch(notMatchedMessage, predicate)))
}

private inline fun <reified V : View> boundedMatch(notMatchedMessage: String? = null, crossinline predicate: V.() -> Boolean): Matcher<View> =
        object : BoundedMatcher<View, V>(V::class.java) {
            override fun describeTo(description: Description) {
                notMatchedMessage?.let {
                    description.appendText(notMatchedMessage)
                }

            }

            override fun matchesSafely(item: V): Boolean = predicate(item)
        }

fun Activity.handler(): Handler = window.decorView.handler

class AndroidTestLiveData<T>(private val uiHandler: Handler) : MutableLiveData<T>() {

    override fun setValue(value: T) {
        uiHandler.post {
            super.setValue(value)
        }
    }

}


fun Drawable.wash(): Drawable = apply { clearColorFilter() }

fun Drawable.copy(): Drawable = constantState?.newDrawable()?.mutate()
        ?: throw IllegalStateException("Could not create a copy. Drawable's constant state is null!")

fun Drawable.equalsPixels(other: Drawable?): Boolean {
    if (other == null || intrinsicWidth != other.intrinsicWidth || intrinsicHeight != other.intrinsicHeight)
        return false

    fun Drawable.toBitmap(): Bitmap =
            if (this is BitmapDrawable) {
                bitmap
            } else {
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
                        .let { it to Canvas(it) }
                        .let {
                            setBounds(0, 0, it.second.width, it.second.height)
                            draw(it.second)
                            it.first
                        }
            }

    fun Bitmap.toPixels(): IntArray =
            IntArray(width * height).apply { getPixels(this, 0, width, 0, 0, width, height) }

    val thisPixels = this.toBitmap().toPixels()
    val otherPixels = other.toBitmap().toPixels()

    return Arrays.equals(thisPixels, otherPixels)
}

inline fun <T : View> T.postThis(crossinline block: T.() -> Unit) {
    this.post {
        this@postThis.block()
    }
}