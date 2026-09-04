package com.example.cnc_rapidrecall

import android.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.cnc_rapidrecall.ui.theme.PastelOrange
import com.example.cnc_rapidrecall.ui.theme.PastelPink
import com.example.cnc_rapidrecall.ui.theme.PastelGreen
import kotlinx.serialization.Serializable
import android.os.CountDownTimer
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.NavBackStack

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.cnc_rapidrecall.ui.theme.PastelMauve
import com.example.cnc_rapidrecall.ui.theme.PastelRed
import com.example.cnc_rapidrecall.ui.theme.PastelYellow
import kotlin.random.Random

@Serializable
private data object HomeScreen : NavKey

@Serializable
private data object GameScreen : NavKey

@Serializable
private data object ResultsScreen : NavKey

@Serializable
private data object LevelSelectScreen : NavKey



fun timerFactory(tick: (Long) -> Unit, finish: () -> Unit, len : Long, interval: Long): CountDownTimer {
    val timer = object : CountDownTimer(len, interval) {
        override fun onTick(millisUntilFinished: Long) {
            tick(millisUntilFinished)
        }

        override fun onFinish() {
            finish()
        }
    }
    return timer
}


@Composable
fun GameScreen(backStack: NavBackStack<NavKey>) {
    var timerText by remember { mutableStateOf("") }
    var sequenceText by remember { mutableStateOf("") }
    var countdownVisible by remember { mutableStateOf(false) }
    var displaySequence by remember { mutableStateOf( false)}
    val colorList = listOf(PastelYellow, PastelYellow, PastelOrange)
    var sequenceColor = colorList[0]

    var n = 10

    val sequenceTimer = timerFactory({ millisUntilFinished: Long ->
        sequenceText = Random.nextInt(0,10).toString()
    }, {
        displaySequence = false
    }, (300 * n).toLong(), 300)


    val countdownTimer = timerFactory({ millisUntilFinished: Long ->
        val secondsRemaining = (millisUntilFinished / 1000) + 1
        timerText = if (secondsRemaining.toInt() > 3) {
            "Ready?"
        } else {
            "$secondsRemaining"
        }
    }, {
        countdownVisible = false
        displaySequence = true
        sequenceTimer.start()
    }, 4000, 1000)


    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            countdownTimer.start()
            countdownVisible = true
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .background(PastelPink)
            .safeDrawingPadding()
            .fillMaxSize()
            .clip(RoundedCornerShape(48.dp))

    ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(48.dp)
                    .clip(RoundedCornerShape(48.dp))

            ) {
                if (countdownVisible) {
                    Text(
                        text = timerText,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(),
                        style = TextStyle(fontSize = 10.sp),
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = 1.sp,
                            maxFontSize = 200.sp,
                            stepSize = 1.sp
                        )
                    )
                } else if (displaySequence) {
                    Text(sequenceText,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(sequenceColor),
                        style = TextStyle(fontSize = 10.sp),
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = 1.sp,
                            maxFontSize = 200.sp,
                            stepSize = 1.sp
                        )
                    )
                }
            }


    }


//        backStack.add(ResultsScreen)


}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {

            val backStack = rememberNavBackStack(HomeScreen)

            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<HomeScreen> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .background(PastelGreen)
                                .safeDrawingPadding()
                                .fillMaxSize()
                                .clip(RoundedCornerShape(48.dp))

                        ) {
                            Text("Home")

                            Button(
                                onClick = dropUnlessResumed { backStack.add(LevelSelectScreen) }
                            ) {
                                Text("Play")
                            }
                        }
                    }
                    entry<LevelSelectScreen> (
                        metadata = metadata {
//                             Slide new content up, keeping the old content in place underneath
                            put(NavDisplay.TransitionKey) {
                                slideInVertically(
                                    initialOffsetY = { it },
                                    animationSpec = tween(1000)
                                ) togetherWith ExitTransition.KeepUntilTransitionsFinished
                            }

                            // Slide old content down, revealing the new content in place underneath
                            put(NavDisplay.PopTransitionKey) {
                                EnterTransition.None togetherWith
                                        slideOutVertically(
                                            targetOffsetY = { it },
                                            animationSpec = tween(1000)
                                        )
                            }

                            // Slide old content down, revealing the new content in place underneath
                            put(NavDisplay.PredictivePopTransitionKey) {
                                EnterTransition.None togetherWith
                                        slideOutVertically(
                                            targetOffsetY = { it },
                                            animationSpec = tween(1000)
                                        )
                            }
                        }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .background(PastelMauve)
                                .safeDrawingPadding()
                                .fillMaxSize()
                                .clip(RoundedCornerShape(48.dp))

                        ) {
                            Text("Level Select")

                            Button(
                                onClick = dropUnlessResumed { backStack.add(GameScreen) }
                            ) {
                                Text("Level 1")
                            }
                        }
                    }
                    entry<GameScreen> (
                        metadata = metadata {
                            put(NavDisplay.TransitionKey) {
                                slideInVertically(
                                    initialOffsetY = { -it },
                                    animationSpec = tween(1000)
                                ) togetherWith ExitTransition.KeepUntilTransitionsFinished
                            }
                        }
                    ) {
                       GameScreen(backStack)
                    }
                    entry<ResultsScreen> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .background(PastelOrange)
                                .safeDrawingPadding()
                                .fillMaxSize()
                                .clip(RoundedCornerShape(48.dp))

                        ) {
                            Text("Results")

                            Button(
                                onClick = dropUnlessResumed { backStack.clear(); backStack.add(HomeScreen) }
                            ) {
                                Text("Return to Home Page")
                            }
                        }

                    }
                },
                transitionSpec = {
                    // Slide in from right when navigating forward
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(1000)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(1000)
                    )
                },
                popTransitionSpec = {
                    // Slide in from left when navigating back
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(1000)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(1000)
                    )
                },
                predictivePopTransitionSpec = {
                    // Slide in from left when navigating back
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(1000)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(1000)
                    )
                }
            )
        }
    }
}
