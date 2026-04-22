package net.canvoki.shared.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.math.roundToInt

/**
 * Holds a stack-based navigation state.
 *
 * This class represents a simple navigation model based on a stack of screens.
 * Each new screen is pushed on top of the stack, and back navigation pops it.
 *
 * This is designed to be used together with [StackNavigator] in Jetpack Compose.
 *
 * @param T The type representing a screen in the navigation stack.
 */
class StackNavigatorState<T>(initial: T) {

    /**
     * Secondary constructor for restoring a full stack from saved state.
     *
     * For internal serialization use only.
     */
    constructor(fullStack: List<T>) : this(fullStack.last()) {
        stack = fullStack
    }

    /**
     * The underlying navigation stack.
     *
     * For internal use. Use [current], [push], and [back] instead.
     */
    var stack by mutableStateOf(listOf(initial))
        internal set

    /**
     * Transient screen being pushed (during animation).
     */
    internal var pushed: T? by mutableStateOf(null)

    /**
     * Transient screen being popped (during animation).
     */
    internal var backed: T? by mutableStateOf(null)

    /**
     * The current (top) screen in the navigation stack.
     */
    val current: T get() = stack.last()

    /**
     * Returns true if there is more than one screen in the stack.
     */
    val canGoBack: Boolean get() = stack.size > 1

    /**
     * Requests navigation to a new screen.
     */
    fun push(screen: T) {
        if (pushed != null || backed != null) return
        pushed = screen
    }

    /**
     * Requests back navigation.
     */
    fun back() {
        if (!canGoBack || pushed != null || backed != null) return
        backed = current
        stack = stack.dropLast(1)
    }

    /**
     * Commits a pending push operation after animation completes.
     */
    internal fun endPush() {
        pushed?.let { stack = stack + it }
        pushed = null
    }

    /**
     * Commits a pending back operation after animation completes.
     */
    internal fun endBack() {
        backed = null
    }
}

/**
 * Creates and remembers a [StackNavigatorState] instance.
 *
 * This should be used instead of manually instantiating the state
 * to ensure proper state saving and restoration across recompositions.
 *
 * @param initial The initial screen of the navigation stack.
 */
@OptIn(InternalSerializationApi::class)
@Composable
inline fun <reified T : Any> rememberStackNavigatorState(initial: T): StackNavigatorState<T> {
    val serializer = serializer<T>()
    val listSerializer = ListSerializer(serializer)
    val json = Json { ignoreUnknownKeys = true }

    return rememberSaveable(
        saver = Saver(
            save = { json.encodeToString(listSerializer, it.stack) },
            restore = { StackNavigatorState(json.decodeFromString(listSerializer, it)) }
        )
    ) {
        StackNavigatorState(initial)
    }
}

/**
 * Internal representation of a slide and fade transition.
 */
private data class SlideFade(
    val startOffset: Float,
    val endOffset: Float,
    val startAlpha: Float,
    val endAlpha: Float
)

/**
 * Transition definitions:
 *
 * - fromRightIn: new screen enters from the right
 * - toLeftOut: current screen exits to the left (push)
 * - fromLeftIn: previous screen re-enters from the left (back)
 * - toRightOut: current screen exits to the right (back)
 */
private fun fromRightIn() = SlideFade(1f, 0f, 0f, 1f)
private fun toLeftOut() = SlideFade(0f, -1f, 1f, 0f)
private fun fromLeftIn() = SlideFade(-1f, 0f, 0f, 1f)
private fun toRightOut() = SlideFade(0f, 1f, 1f, 0f)

/**
 * Internal role of a screen during a navigation transition.
 */
private enum class ScreenRole {
    ENTER_PUSH,
    EXIT_PUSH,
    ENTER_BACK,
    EXIT_BACK,
    IDLE_TOP,
    IDLE_BACKGROUND
}

/**
 * Stack-based navigation container.
 *
 * This composable renders all screens involved in the current navigation state,
 * including transitional screens, and applies animated transitions between them.
 *
 * It behaves similarly to Android activity transitions:
 * - Push: current screen exits left, new screen enters from right
 * - Back: current screen exits right, previous screen enters from left
 *
 * Screens remain composed during transitions to preserve internal state.
 *
 * @param state The navigation state controlling the stack.
 * @param content Composable content for rendering each screen.
 */
@Composable
fun <T> StackNavigator(
    state: StackNavigatorState<T>,
    content: @Composable (T) -> Unit,
) {
    BackHandler(enabled = state.canGoBack) {
        state.back()
    }

    var widthPx by remember { mutableStateOf(-1f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { widthPx = it.width.toFloat() }
    ) {

        val screens = buildList {
            addAll(state.stack)
            state.pushed?.let { add(it) }
            state.backed?.let { add(it) }
        }

        screens.forEach { screen ->

            val role = when {
                screen == state.pushed -> ScreenRole.ENTER_PUSH
                screen == state.current && state.pushed != null -> ScreenRole.EXIT_PUSH
                screen == state.backed -> ScreenRole.EXIT_BACK
                screen == state.current && state.backed != null -> ScreenRole.ENTER_BACK
                screen == state.current -> ScreenRole.IDLE_TOP
                else -> ScreenRole.IDLE_BACKGROUND
            }

            val transition = when (role) {
                ScreenRole.ENTER_PUSH -> fromRightIn()
                ScreenRole.EXIT_PUSH -> toLeftOut()
                ScreenRole.ENTER_BACK -> fromLeftIn()
                ScreenRole.EXIT_BACK -> toRightOut()
                else -> null
            }

            val anim = remember(screen, state.pushed, state.backed) {
                Animatable(0f)
            }

            LaunchedEffect(screen, state.pushed, state.backed) {
                transition?.let {
                    anim.snapTo(0f)
                    anim.animateTo(
                        1f,
                        tween(
                            durationMillis = 300,
                            easing = FastOutSlowInEasing
                        )
                    )

                    if (role == ScreenRole.ENTER_PUSH) state.endPush()
                    if (role == ScreenRole.EXIT_BACK) state.endBack()
                }
            }

            val progress = anim.value

            val offsetX = when {
                widthPx < 0f -> 0f

                transition != null -> {
                    val t = transition.startOffset +
                        (transition.endOffset - transition.startOffset) * progress
                    t * widthPx
                }

                role == ScreenRole.IDLE_BACKGROUND -> -widthPx

                else -> 0f
            }

            val alpha = when {
                transition != null -> {
                    transition.startAlpha +
                        (transition.endAlpha - transition.startAlpha) * progress
                }

                role == ScreenRole.IDLE_BACKGROUND -> 0f

                else -> 1f
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.alpha = alpha
                    }
                    .offset {
                        IntOffset(offsetX.roundToInt(), 0)
                    }
            ) {
                content(screen)
            }
        }
    }
}
