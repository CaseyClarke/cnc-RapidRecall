package com.example.cnc_rapidrecall

import android.os.CountDownTimer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.lifecycle.repeatOnLifecycle
import com.example.cnc_rapidrecall.ui.theme.Charcoal
import com.example.cnc_rapidrecall.ui.theme.PastelBlue
import com.example.cnc_rapidrecall.ui.theme.PastelGreen
import com.example.cnc_rapidrecall.ui.theme.PastelOrange
import com.example.cnc_rapidrecall.ui.theme.PastelPink
import com.example.cnc_rapidrecall.ui.theme.PastelYellow
import kotlin.random.Random

class GameScreenState(
    val level: Int,
    val onGameFinished: (Attempt) -> Unit
) {
    var timerText by mutableStateOf("")
    var sequenceText by mutableStateOf("")
    var countdownVisible by mutableStateOf(false)
    var displaySequence by mutableStateOf(false)

    val colorList = listOf(PastelYellow, PastelGreen, PastelOrange, PastelBlue, PastelPink)
    var sequenceColor by mutableStateOf(colorList[0])

    var sequence by mutableStateOf("")
    var answerRequest by mutableStateOf(false)
    var sequenceAnswer by mutableStateOf("")

    var countdownTimer: CountDownTimer? = null
    var sequenceTimer: CountDownTimer? = null

    fun startCountdown() {
        stopTimers()

        sequence = ""
        sequenceAnswer = ""
        sequenceColor = colorList[0]

        sequenceTimer = timerFactory({
            val nextDigit = Random.nextInt(0, 10).toString()
            sequenceText = nextDigit
            sequence += nextDigit
            sequenceColor = colorList[(colorList.indexOf(sequenceColor) + 1) % colorList.size]
        }, {
            displaySequence = false
            answerRequest = true
        }, (300 * level).toLong(), 300)

        countdownTimer = timerFactory({ millisUntilFinished ->
            val secondsRemaining = (millisUntilFinished / 1000) + 1
            timerText = if (secondsRemaining.toInt() > 3) {
                "Ready?"
            } else {
                "$secondsRemaining"
            }
        }, {
            countdownVisible = false
            displaySequence = true
            sequenceTimer?.start()
        }, 4000, 1000)

        countdownTimer?.start()
        countdownVisible = true
    }

    fun onDigitEntered(digit: String) {
        if (!answerRequest) return
        sequenceAnswer += digit
        if (sequenceAnswer.length == level) {
            val attempt = Attempt(
                sequence = sequence,
                answer = sequenceAnswer
            )
            onGameFinished(attempt)
        }
    }

    fun stopTimers() {
        countdownTimer?.cancel()
        sequenceTimer?.cancel()
    }

    fun timerFactory(
        tick: (Long) -> Unit,
        finish: () -> Unit,
        len: Long,
        interval: Long
    ): CountDownTimer {
        return object : CountDownTimer(len, interval) {
            override fun onTick(millisUntilFinished: Long) {
                tick(millisUntilFinished)
            }

            override fun onFinish() {
                finish()
            }
        }
    }
}

@Composable
fun GameScreen(
    level: Int,
    onGameFinished: (Attempt) -> Unit
) {
    val gameState = remember(level) {
        GameScreenState(level, onGameFinished)
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            gameState.startCountdown()
        }
    }

    DisposableEffect(gameState) {
        onDispose {
            gameState.stopTimers()
        }
    }

    ScreenColumn(backgroundColor = PastelPink)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp)
    ) {
        if (gameState.countdownVisible) {
            Text(
                text = gameState.timerText,
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
        } else if (gameState.displaySequence) {
            Text(
                gameState.sequenceText,
                color = gameState.sequenceColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .offset(
                        x = Random.nextInt(-50, 50).dp,
                        y = Random.nextInt(-50, 50).dp
                    )
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
        } else if (gameState.answerRequest) {
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

                @Composable
                fun PhoneButton(digit: String) {
                    Button(
                        onClick = { gameState.onDigitEntered(digit) },
                        modifier = Modifier
                            .size(80.dp)
                            .padding(10.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) { Text(digit) }
                }

                Row {
                    PhoneButton("1")
                    PhoneButton("2")
                    PhoneButton("3")
                }
                Row {
                    PhoneButton("4")
                    PhoneButton("5")
                    PhoneButton("6")
                }
                Row {
                    PhoneButton("7")
                    PhoneButton("8")
                    PhoneButton("9")
                }
                Row {
                    PhoneButton("0")
                }
            }
        }
    }
}
