package com.omismone.berryflow.ui.dashboard

import java.util.Locale

// What the Dashboard shows for the balance. Hiding it only changes the text:
// the balance itself is still calculated and stored as usual.
fun balanceText(balance: Double, hidden: Boolean): String =
    if (hidden) "€ ••••" else "€ ${String.format(Locale.US, "%.2f", balance)}"