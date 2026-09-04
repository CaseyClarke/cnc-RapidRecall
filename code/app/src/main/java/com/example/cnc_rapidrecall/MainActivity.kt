package com.example.cnc_rapidrecall

import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.cnc_rapidrecall.ui.theme.Charcoal
import com.example.cnc_rapidrecall.ui.theme.PastelBlue
import com.example.cnc_rapidrecall.ui.theme.PastelGreen
import com.example.cnc_rapidrecall.ui.theme.PastelMauve
import com.example.cnc_rapidrecall.ui.theme.PastelOrange
import com.example.cnc_rapidrecall.ui.theme.PastelPink
import com.example.cnc_rapidrecall.ui.theme.PastelYellow
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
private data object HomeScreen : NavKey

@Serializable
private data object GameScreen : NavKey

@Serializable
private data object ResultsScreen : NavKey

@Serializable
private data object LevelSelectScreen : NavKey


fun timerFactory(
    tick: (Long) -> Unit,
    finish: () -> Unit,
    len: Long,
    interval: Long
): CountDownTimer {
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
    var displaySequence by remember { mutableStateOf(false) }
    val colorList = listOf(PastelYellow, PastelGreen, PastelOrange, PastelBlue, PastelPink)
    var sequenceColor by remember { mutableStateOf(colorList[0]) }
    var sequence by remember { mutableStateOf("") }
    var answerRequest by remember { mutableStateOf(false) }
    var sequenceAnswer by remember { mutableStateOf("") }


    var n = 5

    val sequenceTimer = timerFactory({ millisUntilFinished: Long ->
        sequenceText = Random.nextInt(0, 10).toString()
        sequence += sequenceText
        sequenceColor = colorList[(colorList.indexOf(sequenceColor) + 1) % colorList.size]
    }, {
        displaySequence = false
        answerRequest = true
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
        modifier = Modifier
            .background(PastelPink)
            .safeDrawingPadding()
            .fillMaxSize()
            .clip(RoundedCornerShape(48.dp))

    ) {
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp)
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
            Text(
                sequenceText,
                color = sequenceColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(48.dp))
                    .background(Charcoal),
                style = TextStyle(fontSize = 10.sp),
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 1.sp,
                    maxFontSize = 200.sp,
                    stepSize = 1.sp
                )
            )
        } else if (answerRequest) {
            if (sequenceAnswer.length == n) {
                backStack.add(ResultsScreen)
            }

            Column(
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                Text(
                    "Enter the digits in the order they appeared",
                    modifier = Modifier.padding(20.dp)
                )

                Row {
                    Button(
                        onClick = { sequenceAnswer += "1" },
                        modifier = Modifier
                            .size(80.dp)
                            .padding(10.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("1") }
                    Button(
                        onClick = { sequenceAnswer += "2" },
                        modifier = Modifier
                            .size(80.dp)
                            .padding(10.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("2") }
                    Button(
                        onClick = { sequenceAnswer += "3" },
                        modifier = Modifier
                            .size(80.dp)
                            .padding(10.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("3") }
                }
                Row {
                    Button(
                        onClick = { sequenceAnswer += "4" },
                        modifier = Modifier
                            .size(80.dp)
                            .padding(10.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("4") }
                    Button(
                        onClick = { sequenceAnswer += "5" },
                        modifier = Modifier
                            .size(80.dp)
                            .padding(10.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("5") }
                    Button(
                        onClick = { sequenceAnswer += "6" },
                        modifier = Modifier
                            .size(80.dp)
                            .padding(10.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("6") }
                }
                Row {
                    Button(
                        onClick = { sequenceAnswer += "7" },
                        modifier = Modifier
                            .size(80.dp)
                            .padding(10.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("7") }
                    Button(
                        onClick = { sequenceAnswer += "8" },
                        modifier = Modifier
                            .size(80.dp)
                            .padding(10.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("8") }
                    Button(
                        onClick = { sequenceAnswer += "9" },
                        modifier = Modifier
                            .size(80.dp)
                            .padding(10.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("9") }
                }
                Row {
                    Button(
                        onClick = { sequenceAnswer += "0" },
                        modifier = Modifier
                            .size(80.dp)
                            .padding(10.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("0") }
                }
            }
        }
    }

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
                    entry<LevelSelectScreen>(
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
                    entry<GameScreen>(
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
                                onClick = dropUnlessResumed {
                                    backStack.clear(); backStack.add(
                                    HomeScreen
                                )
                                }
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
