package com.omismone.berryflow.ui.adjustbalance

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omismone.berryflow.ui.theme.TopBarButtonPadding
import java.util.Locale

private val SecondaryTextColor = Color(0xFF9E9E9E)
private val KeyBackgroundColor = Color(0xFFECECEC)
private val OkKeyBackgroundColor = Color(0xFF424242)

@Composable
fun AdjustBalanceScreen(
    currentBalance: Double,
    isOnboarding: Boolean = false,
    onDiscardClick: () -> Unit,
    onSaveClick: (Double) -> Unit
) {
    val context = LocalContext.current

    // Blocks the system/gesture back action while onboarding, so the user
    // can't leave without setting a balance first.
    BackHandler(enabled = isOnboarding) { }

    var amountInput by remember { mutableStateOf(formatPlainAmount(currentBalance)) }
    var hasBeenEdited by remember { mutableStateOf(false) }

    fun onDigitPress(digit: String) {
        if (!hasBeenEdited) {
            amountInput = digit
            hasBeenEdited = true
            return
        }
        val dotIndex = amountInput.indexOf('.')
        if (dotIndex != -1) {
            val decimalsTyped = amountInput.length - dotIndex - 1
            if (decimalsTyped >= 2) return
        }
        amountInput += digit
    }

    fun onDotPress() {
        if (!hasBeenEdited) {
            amountInput = "0."
            hasBeenEdited = true
            return
        }
        if (amountInput.contains('.')) return
        amountInput = if (amountInput.isEmpty()) "0." else "$amountInput."
    }

    fun onBackspacePress() {
        if (!hasBeenEdited) {
            amountInput = ""
            hasBeenEdited = true
            return
        }
        if (amountInput.isEmpty()) return
        amountInput = amountInput.dropLast(1)
    }

    fun performSave() {
        onSaveClick(parseAmount(amountInput))
        Toast.makeText(context, "Balance updated", Toast.LENGTH_SHORT).show()
    }

    val displayAmount = amountInput.ifEmpty { "0.00" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = TopBarButtonPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isOnboarding) {
                Spacer(modifier = Modifier.size(48.dp)) // keeps Save aligned to the right
            } else {
                IconButton(onClick = onDiscardClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Discard and go back",
                        tint = SecondaryTextColor,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
            IconButton(onClick = { performSave() }) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save",
                    tint = SecondaryTextColor,
                    modifier = Modifier.size(25.dp)
                )
            }
        }

        if (isOnboarding) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Welcome! Set your starting balance to get started.",
                color = SecondaryTextColor,
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(46.dp))
        } else {
            Spacer(modifier = Modifier.height(70.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "€",
                color = SecondaryTextColor,
                fontSize = 28.sp,
                modifier = Modifier.padding(end = 18.dp)
            )
            Text(
                text = displayAmount,
                color = Color.Black,
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = { onBackspacePress() },
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Backspace",
                    tint = SecondaryTextColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(150.dp))

        NumericKeypad(
            onDigitPress = { onDigitPress(it) },
            onDotPress = { onDotPress() },
            onOkPress = { performSave() }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun NumericKeypad(
    onDigitPress: (String) -> Unit,
    onDotPress: () -> Unit,
    onOkPress: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        KeypadRow {
            KeypadKey("1", modifier = Modifier.weight(1f)) { onDigitPress("1") }
            KeypadKey("2", modifier = Modifier.weight(1f)) { onDigitPress("2") }
            KeypadKey("3", modifier = Modifier.weight(1f)) { onDigitPress("3") }
        }
        KeypadRow {
            KeypadKey("4", modifier = Modifier.weight(1f)) { onDigitPress("4") }
            KeypadKey("5", modifier = Modifier.weight(1f)) { onDigitPress("5") }
            KeypadKey("6", modifier = Modifier.weight(1f)) { onDigitPress("6") }
        }
        KeypadRow {
            KeypadKey("7", modifier = Modifier.weight(1f)) { onDigitPress("7") }
            KeypadKey("8", modifier = Modifier.weight(1f)) { onDigitPress("8") }
            KeypadKey("9", modifier = Modifier.weight(1f)) { onDigitPress("9") }
        }
        KeypadRow {
            KeypadKey(".", modifier = Modifier.weight(1f)) { onDotPress() }
            KeypadKey("0", modifier = Modifier.weight(1f)) { onDigitPress("0") }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OkKeyBackgroundColor)
                    .clickable { onOkPress() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "OK",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun KeypadRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
private fun KeypadKey(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(KeyBackgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 24.sp, color = Color.Black)
    }
}

private fun formatPlainAmount(amount: Double): String {
    return String.format(Locale.US, "%.2f", amount)
}

private fun parseAmount(input: String): Double {
    val cleaned = input.trimEnd('.')
    return cleaned.toDoubleOrNull() ?: 0.0
}