package com.omismone.berryflow.ui.data

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omismone.berryflow.ui.theme.TopBarButtonPadding

private val SecondaryTextColor = Color(0xFF9E9E9E)
private val NeutralButtonColor = Color(0xFFECECEC)
private val DangerColor = Color(0xFFE53935)

// Which confirmation/feedback dialog is currently shown, if any.
private enum class DataAction { IMPORT, EXPORT, ERASE }
private enum class DialogStage { CONFIRM, SUCCESS }

@Composable
fun DataScreen(
    onHomeClick: () -> Unit
) {
    var pendingAction by remember { mutableStateOf<DataAction?>(null) }
    var dialogStage by remember { mutableStateOf(DialogStage.CONFIRM) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = TopBarButtonPadding, end = 12.dp)
        ) {
            IconButton(
                onClick = onHomeClick,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Back to Dashboard",
                    tint = SecondaryTextColor,
                    modifier = Modifier.size(25.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(70.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            DataActionButton(
                label = "import data",
                onClick = {
                    pendingAction = DataAction.IMPORT
                    dialogStage = DialogStage.CONFIRM
                }
            )
            Spacer(modifier = Modifier.height(14.dp))
            DataActionButton(
                label = "export data",
                onClick = {
                    pendingAction = DataAction.EXPORT
                    dialogStage = DialogStage.CONFIRM
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
            DataActionButton(
                label = "erase data",
                onClick = {
                    pendingAction = DataAction.ERASE
                    dialogStage = DialogStage.CONFIRM
                },
                danger = true
            )
        }
    }

    val action = pendingAction
    if (action != null) {
        when (dialogStage) {
            DialogStage.CONFIRM -> ConfirmDialog(
                action = action,
                onDismiss = { pendingAction = null },
                onConfirm = { dialogStage = DialogStage.SUCCESS }
            )
            DialogStage.SUCCESS -> SuccessDialog(
                action = action,
                onDismiss = {
                    pendingAction = null
                    onHomeClick()
                }
            )
        }
    }
}

@Composable
private fun ConfirmDialog(
    action: DataAction,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val (title, body, confirmLabel) = when (action) {
        DataAction.IMPORT -> Triple(
            "Import data?",
            "This will replace all your current data. This cannot be undone.",
            "Import"
        )
        DataAction.EXPORT -> Triple(
            "Export data?",
            "This will save a file with all your data.",
            "Export"
        )
        DataAction.ERASE -> Triple(
            "Erase all data?",
            "This will permanently delete all your transactions, categories, and balance. This cannot be undone.",
            "Erase"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SuccessDialog(
    action: DataAction,
    onDismiss: () -> Unit
) {
    val message = when (action) {
        DataAction.IMPORT -> "Data imported successfully."
        DataAction.EXPORT -> "Data exported successfully."
        DataAction.ERASE -> "All data has been erased."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Done") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}

@Composable
private fun DataActionButton(
    label: String,
    onClick: () -> Unit,
    danger: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (danger) DangerColor else NeutralButtonColor)
            .clickable { onClick() }
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = if (danger) Color.White else Color.Black
        )
    }
}