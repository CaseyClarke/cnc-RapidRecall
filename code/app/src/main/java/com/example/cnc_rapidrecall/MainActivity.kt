package com.example.cnc_rapidrecall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.cnc_rapidrecall.ui.theme.Charcoal
import com.example.cnc_rapidrecall.ui.theme.PastelGreen
import com.example.cnc_rapidrecall.ui.theme.PastelMauve
import com.example.cnc_rapidrecall.ui.theme.PastelOrange
import com.example.cnc_rapidrecall.ui.theme.PastelPurple
import com.example.cnc_rapidrecall.ui.theme.PastelRed
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// sources
// ----------------------------------------------------------------------------------
// some of the basic nav3 code and transitions boilerplate was based off of this repo, just examples of how to use nav3 cause it's new
// https://github.com/android/nav3-recipes
// ----------------------------------------------------------------------------------

@Serializable
data class Attempt(
    val sequence: String,
    val answer: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val won get() = sequence == answer
    val level get() = sequence.length

}

@Serializable
private data object HomeScreen : NavKey

@Serializable
private data class GameScreen(
    val level: Int
) : NavKey

@Serializable
private data class ResultsScreen(
    val attempt: Attempt
) : NavKey


@Serializable
private data object LevelSelectScreen : NavKey

@Serializable
private data object LogScreen : NavKey

@Serializable
private data object AttemptSummaryScreen : NavKey


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {

            val backStack = rememberNavBackStack(HomeScreen)

            var attempts by remember {
                mutableStateOf(listOf<Attempt>())
            }

            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<HomeScreen> {
                        ScreenColumn(backgroundColor = PastelGreen) {
                            Text("Home")
                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = dropUnlessResumed { backStack.add(LevelSelectScreen) }
                            ) {
                                Text("Play")
                            }

                            Button(
                                onClick = dropUnlessResumed { backStack.add(AttemptSummaryScreen) }
                            ) {
                                Text("Gameplay Summary")
                            }

                            Button(
                                onClick = dropUnlessResumed { backStack.add(LogScreen) }
                            ) {
                                Text("Game Log")
                            }
                        }
                    }
                    entry<LevelSelectScreen>(
                        metadata = metadata {
                            put(NavDisplay.TransitionKey) { slideUpTransition() }
                            put(NavDisplay.PopTransitionKey) { slideDownExitTransition() }
                            put(NavDisplay.PredictivePopTransitionKey) { slideDownExitTransition() }
                        }
                    ) {
                        ScreenColumn(backgroundColor = PastelMauve) {
                            Text("Level Select")
                            Spacer(modifier = Modifier.height(8.dp))

                            for (i in 1..10) {
                                Button(
                                    onClick = dropUnlessResumed { backStack.add(GameScreen(level = i)) }
                                ) {
                                    Text("Level $i")
                                }
                            }
                        }
                    }
                    entry<LogScreen> {
                        ScreenColumn(
                            backgroundColor = PastelRed,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text("Log")
                            Spacer(modifier = Modifier.height(8.dp))

                            HorizontalDivider(thickness = 3.dp, color = Charcoal)
                            LazyColumn {
                                items(attempts) { attempt ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,

                                        ) {
                                        Text(
                                            "Timestamp: " + SimpleDateFormat(
                                                "MMM d, yyyy h:mm a",
                                                Locale.getDefault()
                                            ).format(Date(attempt.timestamp))
                                        )
                                        Text(if (attempt.won) "Win" else "Loss")
                                        Text("Level: " + attempt.level)
                                        Text("Correct Answer: " + attempt.sequence)
                                        Text("Your Answer: " + attempt.answer)
                                    }

                                    HorizontalDivider(thickness = 3.dp, color = Charcoal)
                                }
                            }

                        }

                    }
                    entry<AttemptSummaryScreen> {
                        ScreenColumn(backgroundColor = PastelPurple) {
                            Text("Gameplay Summary")
                            Spacer(modifier = Modifier.height(8.dp))

                            val totalGames = attempts.size
                            val totalWins = attempts.count { it.won }
                            val totalLosses = totalGames - totalWins
                            val winPercentage =
                                if (totalGames > 0) totalWins.toFloat() / totalGames.toFloat() else 0f

                            Text("Total Games: $totalGames")
                            Text("Number of Wins: $totalWins")
                            Text("Number of Losses: $totalLosses")
                            Text("Percentage of Wins: ${winPercentage * 100}%")

                        }

                    }

                    entry<GameScreen>(
                        metadata = metadata {
                            put(NavDisplay.TransitionKey) { slideDownFromTopTransition() }
                        }
                    ) { gameScreen ->
                        GameScreen(
                            level = gameScreen.level,
                            onGameFinished = { attempt ->
                                attempts = attempts + attempt
                                backStack.clear()
                                backStack.add(HomeScreen)
                                backStack.add(ResultsScreen(attempt))
                            }
                        )
                    }
                    entry<ResultsScreen> { resultsScreen ->
                        val attempt = resultsScreen.attempt
                        ScreenColumn(backgroundColor = PastelOrange) {
                            Text("Results")

                            Spacer(modifier = Modifier.height(8.dp))


                            if (attempt.won) {
                                Text("You Won!")
                                Text("\uD83C\uDF89", fontSize = 64.em)
                            } else {
                                Text("You Lost!")
                                Text("\uD83D\uDE22", fontSize = 64.em)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Level: ${attempt.level}")
                            Text("Correct Answer: ${attempt.sequence}")
                            Text("Your Answer: ${attempt.answer}")
                            var squares = ""
                            for (i in attempt.sequence.indices) {
                                squares += if (attempt.sequence[i] == attempt.answer[i]) {
                                    "\uD83D\uDFE9"
                                } else {
                                    "\uD83D\uDFE5"
                                }
                            }
                            Text(
                                squares,
                                maxLines = 1,
                                autoSize = TextAutoSize.StepBased(
                                    minFontSize = 1.sp,
                                    maxFontSize = 32.sp,
                                    stepSize = 1.sp
                                )
                            )

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
                transitionSpec = { slideInFromRightTransition() },
                popTransitionSpec = { slideInFromLeftTransition() },
                predictivePopTransitionSpec = { slideInFromLeftTransition() }
            )
        }
    }
}

fun slideUpTransition(): ContentTransform =
    slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(1000)
    ) togetherWith ExitTransition.None

fun slideDownExitTransition(): ContentTransform =
    EnterTransition.None togetherWith slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(1000)
    )

fun slideDownFromTopTransition(): ContentTransform =
    slideInVertically(
        initialOffsetY = { -it },
        animationSpec = tween(1000)
    ) togetherWith ExitTransition.None

fun slideInFromRightTransition(): ContentTransform =
    slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(1000)
    ) togetherWith slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(1000)
    )

fun slideInFromLeftTransition(): ContentTransform =
    slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec = tween(1000)
    ) togetherWith slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(1000)
    )
