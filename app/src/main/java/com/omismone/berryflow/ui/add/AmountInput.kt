package com.omismone.berryflow.ui.add

// The amount is typed on the custom keypad as a string. These pure functions
// hold its rules: at most 2 decimals, a single dot, no redundant leading zero
// and a bounded integer part (so the value always parses to a finite number).
private const val MaxIntegerDigits = 9

fun appendDigit(input: String, digit: String): String {
    val dotIndex = input.indexOf('.')
    if (dotIndex != -1) {
        val decimalsTyped = input.length - dotIndex - 1
        return if (decimalsTyped >= 2) input else input + digit
    }
    if (input == "0") return digit // "0" then "5" is 5, not "05"
    return if (input.length >= MaxIntegerDigits) input else input + digit
}

fun appendDot(input: String): String = when {
    input.contains('.') -> input
    input.isEmpty() -> "0."
    else -> "$input."
}

fun deleteLast(input: String): String = input.dropLast(1)

// Empty or "5." style input is valid; anything unparseable is 0 (rejected on save).
fun parseAmount(input: String): Double =
    input.trimEnd('.').toDoubleOrNull() ?: 0.0

// State of the amount field while typing.
// - replaceOnNextInput: set when an existing amount is shown (editing a
//   transaction). The first digit/dot typed REPLACES it instead of appending,
//   so the user doesn't have to clear the old amount first. Backspace instead
//   edits the existing amount, and ends the replace mode.
// - cleared: the user tapped the amount to clear it, so the field is shown
//   completely empty (rather than the "0.00" placeholder) until they type.
data class AmountEntry(
    val text: String = "",
    val replaceOnNextInput: Boolean = false,
    val cleared: Boolean = false
) {
    private val base: String get() = if (replaceOnNextInput) "" else text

    fun digit(digit: String) = AmountEntry(appendDigit(base, digit))

    fun dot() = AmountEntry(appendDot(base))

    fun backspace() = AmountEntry(deleteLast(text), cleared = cleared)

    fun clear() = AmountEntry(text = "", cleared = true)

    // What the field shows: the typed text, an empty field after an explicit
    // clear, or the "0.00" placeholder when nothing was typed yet.
    val display: String
        get() = when {
            text.isNotEmpty() -> text
            cleared -> ""
            else -> "0.00"
        }

    companion object {
        // Opening an existing amount: shown as is, replaced by the first key.
        fun existing(formattedAmount: String) =
            AmountEntry(text = formattedAmount, replaceOnNextInput = formattedAmount.isNotEmpty())

        val Saver = androidx.compose.runtime.saveable.listSaver<AmountEntry, Any>(
            save = { listOf(it.text, it.replaceOnNextInput, it.cleared) },
            restore = { AmountEntry(it[0] as String, it[1] as Boolean, it[2] as Boolean) }
        )
    }
}